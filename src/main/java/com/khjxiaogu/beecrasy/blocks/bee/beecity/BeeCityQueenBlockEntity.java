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

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.beehive.BeeCityComponent;
import com.khjxiaogu.beecrasy.beehive.BeeCityQueenComponent;
import com.khjxiaogu.beecrasy.beehive.HiveSlot;
import com.khjxiaogu.beecrasy.blocks.HiveSlotProvider;
import com.khjxiaogu.beecrasy.blocks.bee.BeeHiveBaseBlockEntity;
import com.khjxiaogu.beecrasy.menu.BeeCityQueenMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BeeCityQueenBlockEntity extends BeeHiveBaseBlockEntity implements MenuProvider{
	public final BeeCityQueenComponent component;
	public HiveSlotProvider slots=new HiveSlotProvider() {

		@Override
		public boolean isBindable(BlockPos core) {
			return component.corePos==null||core.equals(component.corePos);
		}

		@Override
		public boolean bind(BlockPos core) {
			if(isBindable(core)) {
				component.corePos=core;
				return true;
			}
			return false;
		}

		@Override
		public boolean unbind(BlockPos core) {
			if(core.equals(component.corePos)) {
				component.corePos=null;
				return true;
			}
			return false;
		}

		@Override
		public int getSlots(HiveSlotType type) {
			if(type==HiveSlotType.QUEEN) {
				return 1;
			}
			return HiveSlotProvider.super.getSlots(type);
		}

		@Override
		public HiveSlot getSlot(HiveSlotType type, int index) {
			if(type==HiveSlotType.QUEEN) {
				return component.getQueenSlot().get(index);
			}
			return HiveSlotProvider.super.getSlot(type, index);
		}
		
	};
	public BeeCityQueenBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(Blocks.BEE_CITY_QUEEN_BLOCKENTITY.get(), pWorldPosition, pBlockState,this.component=new BeeCityQueenComponent(1,0,0,1));
		;
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
			component.level=serverLevel;
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
	@Override
	public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
		return new BeeCityQueenMenu(containerId,inventory,this);
	}

	@Override
	public Component getDisplayName() {
		return Blocks.BEE_CITY_QUEEN.get().getName();
	}
}
