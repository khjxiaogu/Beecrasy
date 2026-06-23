/*
 *
 * Copyright (C) 2026 khjxiaogu
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

package com.khjxiaogu.beecrasy.blocks.bee.beecity;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.beehive.BeeCityComponent;
import com.khjxiaogu.beecrasy.blocks.BeecrasyBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BeeCityCoreBlockEntity extends BeecrasyBlockEntity {
	public final BeeCityComponent component;
	public BeeCityCoreBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(Blocks.BEE_CITY_CORE_BLOCKENTITY.get(), pWorldPosition, pBlockState);
		this.component=new BeeCityComponent(1,2,2,1);
	}

	@Override
	public void readCustomNBT(ValueInput nbt, boolean isClient) {
		component.readCustomNBT(nbt, isClient);
	}

	@Override
	public void writeCustomNBT(ValueOutput nbt, boolean isClient) {
		component.writeCustomNBT(nbt, isClient);
	}

	@Override
	public void tick() {
		if(level instanceof ServerLevel serverLevel) {
			component.tick(serverLevel, worldPosition, 1, level.hasNeighborSignal(worldPosition));
			if(component.isChanged()) {
				setChanged();
				component.setChanged(false);
			}
			boolean oldstate=this.getBlockState().getValue(BlockStateProperties.LIT);
			boolean newstate=component.hiveInfo.isWorking();
			if(oldstate!=newstate) {
				BlockState nextstate=getBlockState().setValue(BlockStateProperties.LIT, newstate);
				this.level.setBlockAndUpdate(worldPosition, nextstate);
			}
		}
	}

}
