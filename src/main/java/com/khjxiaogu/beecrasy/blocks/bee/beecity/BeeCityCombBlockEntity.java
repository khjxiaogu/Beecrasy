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

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.beehive.HiveSlot;
import com.khjxiaogu.beecrasy.beehive.slot.ResourceStackHiveSlot;
import com.khjxiaogu.beecrasy.blocks.BeecrasyBlockEntity;
import com.khjxiaogu.beecrasy.menu.BeeCityCombMenu;

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
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class BeeCityCombBlockEntity extends BeecrasyBlockEntity implements MenuProvider{
	public final ItemStacksResourceHandler container=new ItemStacksResourceHandler(2);
	public final List<ResourceStackHiveSlot> resources=List.of(new ResourceStackHiveSlot(container,0),new ResourceStackHiveSlot(container,1));
	public BlockPos corePos;
	public BeeCityCombBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(Blocks.BEE_CITY_COMB_BLOCKENTITY.get(), pWorldPosition, pBlockState);
	}
	public HiveSlotProvider slots=new HiveSlotProvider() {

		@Override
		public boolean isBindable(BlockPos core) {
			return corePos==null||core.equals(corePos);
		}

		@Override
		public boolean bind(BlockPos core) {
			if(isBindable(core)) {
				corePos=core;
				return true;
			}
			return false;
		}

		@Override
		public boolean unbind(BlockPos core) {
			if(core.equals(corePos)) {
				corePos=null;
				return true;
			}
			return false;
		}

		@Override
		public int getSlots(HiveSlotType type) {
			if(type==HiveSlotType.COMB) {
				return 2;
			}
			return HiveSlotProvider.super.getSlots(type);
		}

		@Override
		public HiveSlot getSlot(HiveSlotType type, int index) {
			if(type==HiveSlotType.COMB) {
				return resources.get(index);
			}
			return HiveSlotProvider.super.getSlot(type, index);
		}
		
	};
	@Override
	public void readCustomNBT(ValueInput nbt, boolean isClient) {
		if(!isClient) {
			nbt.readChild("inv", container);
		}
		corePos=nbt.read("core", BlockPos.CODEC).orElse(null);
	}

	@Override
	public void writeCustomNBT(ValueOutput nbt, boolean isClient) {
		if(!isClient) {
			nbt.putChild("inv", container);
		}
		nbt.storeNullable("core", BlockPos.CODEC, corePos);
	}

	@Override
	public void tick() {
		if(level instanceof ServerLevel serverLevel) {
			boolean oldstate=this.getBlockState().getValue(BlockStateProperties.LIT);
			boolean newstate=oldstate;
			for(int i=0;i<container.size();i++) {
				if(container.getAmountAsInt(i)>0)
					newstate|=true;
			}
			if(corePos!=null&&level.isLoaded(corePos)&&!(serverLevel.getBlockEntity(corePos) instanceof BeeCityCoreBlockEntity))
				corePos=null;
			if(oldstate!=newstate) {
				BlockState nextstate=getBlockState().setValue(BlockStateProperties.LIT, newstate);
				this.level.setBlockAndUpdate(worldPosition, nextstate);
			}
		}
	}

	@Override
	public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
		return new BeeCityCombMenu(containerId,inventory,this);
	}

	@Override
	public Component getDisplayName() {
		return Blocks.BEE_CITY_COMB.get().getName();
	}

}
