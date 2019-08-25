package org.cyclops.cyclopscore.fluid;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidTank;

/**
 * A simple fluid tank.
 * Based on the Buildcraft Tank
 * @author rubensworks
 *
 */
public class Tank extends FluidTank {

    /**
     * Make a new fluid tank.
     * @param capacity The capacity (mB) for the tank.
     * @param tile The {@link TileEntity} that uses this tank.
     */
    public Tank(int capacity, TileEntity tile) {
        super(capacity);
        this.tile = tile;
    }
    
    /**
     * Check if this tank is empty.
     * @return If the tank is empty; no fluid is inside of it.
     */
    public boolean isEmpty() {
        return getFluid() == null || getFluid().amount <= 0;
    }

    /**
     * Check if this tank is full; the capacity is reached.
     * @return If this tank is full.
     */
    public boolean isFull() {
        return getFluid() != null && getFluid().amount >= getCapacity();
    }

    /**
     * Get the fluid that currently occupies this tank, will return null if there is no fluid.
     * @return The inner fluid.
     */
    public Fluid getFluidType() {
        return getFluid() != null ? getFluid().getFluid() : null;
    }

    protected boolean replaceInnerFluid() {
        return true;
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT nbt) {
        CompoundNBT tankData = new CompoundNBT();
        if(replaceInnerFluid()) {
            super.writeToNBT(tankData);
        }
        writeTankToNBT(tankData);
        return nbt;
    }

    @Override
    public FluidTank readFromNBT(CompoundNBT nbt) {
        if(replaceInnerFluid()) {
            if (nbt.contains("Empty")) {
                setFluid(null);
            }
            super.readFromNBT(nbt);
        }
        readTankFromNBT(nbt);
        return this;
    }

    /**
     * Write the tank contents to NBT.
     * @param nbt The NBT tag to write to.
     */
    public void writeTankToNBT(CompoundNBT nbt) {
    	nbt.putInt("capacity", getCapacity());
    }

    /**
     * Read the tank contents from NBT.
     * @param nbt The NBT tag to write from.
     */
    public void readTankFromNBT(CompoundNBT nbt) {
    	if(nbt.contains("capacity")) { // Backwards compatibility.
    		setCapacity(nbt.getInt("capacity"));
    	}
    }

}