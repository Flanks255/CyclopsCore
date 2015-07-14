package org.cyclops.cyclopscore.inventory;

import lombok.Getter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Iterator;

/**
 * Iterate over a player's inventory.
 * @author rubensworks
 *
 */
public class PlayerInventoryIterator implements Iterator<ItemStack>{
    
    @Getter private final EntityPlayer player;
    private int i;
    
    /**
     * Create a new HotbarIterator.
     * @param player The player to iterate the hotbar from.
     */
    public PlayerInventoryIterator(EntityPlayer player) {
        this.player = player;
    }

    @Override
    public boolean hasNext() {
        return i < player.inventory.mainInventory.length;
    }

    @Override
    public ItemStack next() {
        return player.inventory.mainInventory[i++];
    }
    
    /**
     * Get the next item indexed.
     * @return The indexed item.
     */
    public Pair<Integer, ItemStack> nextIndexed() {
    	return Pair.of(i, player.inventory.mainInventory[i++]);
    }

    @Override
    public void remove() {
        if(i - 1 >= 0 && i - 1 < player.inventory.mainInventory.length)
            player.inventory.mainInventory[i - 1] = null;
    }

}
