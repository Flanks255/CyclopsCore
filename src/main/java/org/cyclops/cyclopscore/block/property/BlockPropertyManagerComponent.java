package org.cyclops.cyclopscore.block.property;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Implementation of {@link IBlockPropertyManager}.
 * Because of limitations, simply delegating to this using the {@link lombok.experimental.Delegate} annotation
 * will not work, you will also need to override the following method.
 * If {@link net.minecraftforge.common.property.IUnlistedProperty} are detected, an
 * {@link net.minecraftforge.common.property.ExtendedBlockState} will be automatically created instead of a normal
 * {@link net.minecraft.block.state.IBlockState}.
 * <code>
 *     {@literal @}Override
 *     protected BlockState createBlockState() {
 *         return (propertyManager = new BlockPropertyManagerComponent(this)).createDelegatedBlockState();
 *     }
 * </code>
 * @author rubensworks
 */
@Data
public class BlockPropertyManagerComponent implements IBlockPropertyManager {

    private static final Comparator<IProperty> PROPERTY_COMPARATOR = new PropertyComparator();
    private static final Comparator<IUnlistedProperty> UNLISTEDPROPERTY_COMPARATOR = new UnlistedPropertyComparator();

    private final Block block;
    private final IProperty[] properties;
    private final IUnlistedProperty[] unlistedProperties;
    private final IProperty[] propertiesReversed;
    private final Map<IProperty, ArrayList<Comparable>> propertyValues;

    public BlockPropertyManagerComponent(Block block) {
        this.block = block;
        try {
            Pair<IProperty[], IUnlistedProperty[]> allProperties = preprocessProperties();
            this.properties = allProperties.getLeft();
            this.unlistedProperties = allProperties.getRight();
            this.propertiesReversed = Arrays.copyOf(properties, properties.length);
            ArrayUtils.reverse(this.propertiesReversed);
            this.propertyValues = preprocessPropertyValues(this.properties);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Pair<IProperty[], IUnlistedProperty[]> preprocessProperties() throws IllegalAccessException {
        TreeSet<IProperty> sortedProperties = Sets.newTreeSet(PROPERTY_COMPARATOR);
        TreeSet<IUnlistedProperty> sortedUnlistedProperties = Sets.newTreeSet(UNLISTEDPROPERTY_COMPARATOR);
        for(Class<?> clazz = block.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            for(Field field : clazz.getDeclaredFields()) {
                if(field.isAnnotationPresent(BlockProperty.class)) {
                    Object fieldObject = field.get(block);
                    if(fieldObject instanceof IProperty) {
                        sortedProperties.add((IProperty) fieldObject);
                    } else if(fieldObject instanceof IUnlistedProperty) {
                        sortedUnlistedProperties.add((IUnlistedProperty) fieldObject);
                    } else if(fieldObject instanceof IProperty[]) {
                        for(IProperty property : ((IProperty[]) fieldObject)) {
                            sortedProperties.add(property);
                        }
                    } else if(fieldObject instanceof IUnlistedProperty[]) {
                        for(IUnlistedProperty unlistedProperty : ((IUnlistedProperty[]) fieldObject)) {
                            sortedUnlistedProperties.add(unlistedProperty);
                        }
                    } else {
                        throw new IllegalArgumentException(String.format("The field %s in class %s can not be used " +
                                "as block property.", field.getName(), clazz.getCanonicalName()));
                    }
                }
            }
        }
        IProperty[] properties = new IProperty[sortedProperties.size()];
        IUnlistedProperty[] unlistedProperties = new IUnlistedProperty[sortedUnlistedProperties.size()];
        return Pair.of(sortedProperties.toArray(properties), sortedUnlistedProperties.toArray(unlistedProperties));
    }

    private Map<IProperty, ArrayList<Comparable>> preprocessPropertyValues(IProperty[] properties) {
        Map<IProperty, ArrayList<Comparable>> dict = Maps.newHashMap();
        for(IProperty property : properties) {
            ArrayList<Comparable> values = Lists.newArrayList((Collection<Comparable>) property.getAllowedValues());
            Collections.sort(values);
            dict.put(property, values);
        }
        return dict;
    }

    @Override
    public int getMetaFromState(IBlockState blockState) {
        int meta = 0;
        for(IProperty property : properties) {
            int propertySize = property.getAllowedValues().size();
            int propertyValueIndex = propertyValues.get(property).indexOf(blockState.getValue(property));
            if(propertyValueIndex < 0) {
                throw new RuntimeException(String.format("The value %s was not found in the calculated property " +
                        "values for %s.", propertyValueIndex, property));
            }
            meta = meta * propertySize + propertyValueIndex;
        }
        if(meta > Character.MAX_VALUE) {
            throw new RuntimeException(String.format("The metadata for %s was too large (%s) to store.", this, meta));
        }
        return meta;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState blockState = block.getDefaultState();
        int metaLoop = meta;
        for(IProperty property : propertiesReversed) {
            int propertySize = property.getAllowedValues().size();
            int value = metaLoop % propertySize;
            Comparable propertyValue = propertyValues.get(property).get(value);
            if(propertyValue == null) {
                throw new RuntimeException(String.format("The value %s was not found in the calculated property " +
                        "values for %s.", value, property));
            }
            blockState = blockState.withProperty(property, propertyValue);
            metaLoop = (metaLoop - value) / propertySize;
        }
        return blockState;
    }

    @Override
    public BlockState createDelegatedBlockState() {
        if(unlistedProperties.length == 0) {
            return new BlockState(block, properties);
        } else {
            return new ExtendedBlockState(block, properties, unlistedProperties);
        }
    }

    private static class PropertyComparator implements Comparator<IProperty> {
        @Override
        public int compare(IProperty o1, IProperty o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    private static class UnlistedPropertyComparator implements Comparator<IUnlistedProperty> {
        @Override
        public int compare(IUnlistedProperty o1, IUnlistedProperty o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}