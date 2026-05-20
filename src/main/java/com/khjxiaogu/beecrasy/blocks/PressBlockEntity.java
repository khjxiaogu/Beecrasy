package com.khjxiaogu.beecrasy.blocks;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class PressBlockEntity extends BeecrasyBlockEntity {
	public int ticks;
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
