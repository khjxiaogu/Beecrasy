/** 
* Copyright (c) 2026 khjxiaogu
*
* This file is part of Beecrasy.
*
* Beecrasy is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, version 3.
* 
* Beecrasy is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with Beecrasy. If not, see <https://www.gnu.org/licenses/>.
*/
package com.khjxiaogu.beecrasy.blocks;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.utils.RecipeHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class PressBlockEntity extends BeecrasyBlockEntity {
	public int ticks;
	public static final int ACCESSIBLE_SLOTS=10;
	private ItemStacksResourceHandler internInv = new ItemStacksResourceHandler(ACCESSIBLE_SLOTS) {
		@Override
		protected void onContentsChanged(int slot, ItemStack stack) {
			setChanged();
			super.onContentsChanged(slot, stack);
		}
		
	};
	RecipeHandler recipeHandler;
public final FluidStacksResourceHandler tank = new FluidStacksResourceHandler(1,1250) {

	@Override
	public boolean isValid(int index, FluidResource resource) {
		return !resource.getFluid().getFluidType().isLighterThanAir();
	}

	@Override
	protected void onContentsChanged(int slot,FluidStack stackBefore) {
		super.onContentsChanged(slot,stackBefore);
		recipeHandler.onContainerChanged();
		recipeHandler.resetProgress();
		sendUpdated();
	}

};
	public PressBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(Blocks.PRESS_BLOCKENTITY.get(), pWorldPosition, pBlockState);
	}

	@Override
	public void readCustomNBT(ValueInput nbt, boolean isClient) {
		
	}

	@Override
	public void writeCustomNBT(ValueOutput nbt, boolean isClient) {
		
	}
	@Override
	public void tick() {
		if(ticks<320) {
			ticks++;
		}else {
			ticks=0;
		}
	}

}
