package org.cyclops.cyclopscore.config;

import org.cyclops.cyclopscore.config.configurabletypeaction.*;
import org.cyclops.cyclopscore.config.extendedconfig.*;

/**
 * The different types of configurable.
 * @author rubensworks
 *
 */
public class ConfigurableType {
    
    /**
     * Item type.
     */
    public static final ConfigurableType ITEM = new ConfigurableType(true, ItemConfig.class, new ItemAction(), "item");
    /**
     * Block type.
     */
    public static final ConfigurableType BLOCK = new ConfigurableType(true, BlockConfig.class, new BlockAction(), "block");
    /**
     * Regular entity type.
     */
    public static final ConfigurableType ENTITY = new ConfigurableType(false, EntityConfig.class, new EntityAction(), "entity");
    /**
     * Fluid type.
     */
    public static final ConfigurableType FLUID = new ConfigurableType(true, FluidConfig.class, new FluidAction(), "fluid");
    /**
     * Enchantment type.
     */
    public static final ConfigurableType ENCHANTMENT = new ConfigurableType(true, EnchantmentConfig.class, new EnchantmentAction(), "enchantment");
    /**
     * Villager type.
     */
    public static final ConfigurableType VILLAGER = new ConfigurableType(true, VillagerConfig.class, new VillagerAction(), "mob");
    /**
     * Biome type.
     */
    public static final ConfigurableType BIOME = new ConfigurableType(true, BiomeConfig.class, new BiomeAction(), "biome");
    /**
     * Potion effect type.
     */
    public static final ConfigurableType EFFECT = new ConfigurableType(true, EffectConfig.class, new EffectAction(), "potion");
    /**
     * Capability type.
     */
    public static final ConfigurableType CAPABILITY = new ConfigurableType(false, CapabilityConfig.class, new CapabilityAction(), "capability");

    /**
     * Dummy type, only used for configs that refer to nothing.
     */
    public static final ConfigurableType DUMMY = new ConfigurableType(false, DummyConfig.class, new DummyAction(), "general");
    
    private final boolean uniqueInstance;
    @SuppressWarnings("rawtypes")
    private final Class<? extends ExtendedConfig> configClass;
    @SuppressWarnings("rawtypes")
    private final ConfigurableTypeAction action;
    private final String category;
    
    /**
     * Make a new instance.
     * @param uniqueInstance If this type has a unique instance for each config.
     * @param configClass The config class.
     * @param action The action instance that helps with saving of the config and optional instance.
     * @param category The category in which the configs should be saved.
     */
    @SuppressWarnings("rawtypes")
    public ConfigurableType(boolean uniqueInstance, Class<? extends ExtendedConfig> configClass,
                            ConfigurableTypeAction action, String category) {
        this.uniqueInstance = uniqueInstance;
        this.configClass = configClass;
        this.action = action;
        this.category = category;
    }
    
    /**
     * If this type should refer to a configurable with a unique instance.
     * If this is true, the configurable should have a public static void
     * initInstance(ExtendedConfig eConfig) method and also a public static
     * (? extends IConfigurable) getInstance() method.
     * @return If it has a unique instance.
     */
    public boolean hasUniqueInstance() {
        return uniqueInstance;
    }
    
    /**
     * Get the class that extends {@link ExtendedConfig} this type can hold.
     * @return The class that extends {@link ExtendedConfig} this type can hold.
     */
    @SuppressWarnings("rawtypes")
    public Class<? extends ExtendedConfig> getConfigClass() {
        return configClass;
    }
    
    /**
     * The action for this type after the the configurable has configured so it can be registered.
     * @return The action for this type.
     */
    @SuppressWarnings("rawtypes")
    public ConfigurableTypeAction getConfigurableTypeAction() {
        return action;
    }
    
    /**
     * The category of this type.
     * @return The category.
     */
    public String getCategory() {
        return category;
    }
}