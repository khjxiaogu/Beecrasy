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

package com.khjxiaogu.beecrasy.blocks;

import com.khjxiaogu.beecrasy.beehive.BeeHiveBaseComponent;
import com.khjxiaogu.beecrasy.components.BeeHiveArgumentation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BeeHiveBaseBlockEntity extends BeecrasyBlockEntity {
	public final BeeHiveBaseComponent component;
	public BeeHiveBaseBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState,BeeHiveBaseComponent component) {
		super(pType, pWorldPosition, pBlockState);
		this.component=component;
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
			component.tick(serverLevel, worldPosition,1, level.hasNeighborSignal(worldPosition));
			if(component.isChanged()) {
				this.setChanged();
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

	@Override
	public void removeComponentsFromTag(ValueOutput output) {

		output.discard("inv");
		output.discard("hive");
		output.discard("nextWork");
		output.discard("arguments");
		output.discard("work");
		super.removeComponentsFromTag(output);
	}

}
