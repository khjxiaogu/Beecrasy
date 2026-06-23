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

package com.khjxiaogu.beecrasy.blocks.machine;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.beedi.BeediDisk;
import com.khjxiaogu.beecrasy.beedi.ServerBeediManager;
import com.khjxiaogu.beecrasy.blocks.BeecrasyBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class BeediboxBlockEntity extends BeecrasyBlockEntity {
	ItemStacksResourceHandler disk=new ItemStacksResourceHandler(1) {

		@Override
		public boolean isValid(int index, ItemResource resource) {
			return resource.has(Components.BEEDI_RECORD);
		}

		@Override
		protected int getCapacity(int index, ItemResource resource) {
			return 1;
		}

		@Override
		protected void onContentsChanged(int index, ItemStack previousContents) {
			super.onContentsChanged(index, previousContents);
			BeediDisk id=getResource(index).get(Components.BEEDI_RECORD);
			if(level instanceof ServerLevel serverLevel)
				if(id!=null) {
					ServerBeediManager.playSong(serverLevel, worldPosition, id.name());
					ticks=id.ticks();
					
				}else {
					ServerBeediManager.stopSong(serverLevel, worldPosition);
					ticks=0;
				}
			level.setBlockAndUpdate(worldPosition, getBlockState().setValue(BlockStateProperties.HAS_RECORD, !getResource(index).isEmpty()));
			setChanged();
		}
		
	};
	long ticks;
	public BeediboxBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(Blocks.BEEDIBOX_BLOCKENTITY.get(), pWorldPosition, pBlockState);
	}

	@Override
	public void readCustomNBT(ValueInput nbt, boolean isClient) {
		if(!isClient) {
			nbt.readChild("inv", disk);
			ticks=nbt.getLongOr("ticks", 0);
		}
	}

	@Override
	public void writeCustomNBT(ValueOutput nbt, boolean isClient) {
		if(!isClient) {
			nbt.putChild("inv", disk);
			nbt.putLong("ticks", ticks);
		}
	}

	@Override
	public void tick() {
		ticks--;
	}
	public boolean isPlaying() {
		return ticks>0;
	}

    public void popOutTheItem() {
        if (this.level != null && !this.level.isClientSide()) {
            BlockPos pos = this.getBlockPos();
            ItemResource ir=disk.getResource(0);
            if (!ir.isEmpty()) {
            	try(Transaction trans=Transaction.openRoot()){
            		if(disk.extract(0, ir, 1, trans)==1) {
		                Vec3 itemPos = Vec3.atLowerCornerWithOffset(pos, 0.5, 1.01, 0.5).offsetRandomXZ(this.level.getRandom(), 0.7F);
		                ItemStack itemStack = ir.toStack();
		                ItemEntity entity = new ItemEntity(this.level, itemPos.x(), itemPos.y(), itemPos.z(), itemStack);
		                entity.setDefaultPickUpDelay();
		                this.level.addFreshEntity(entity);
		                trans.commit();
            		}
            	}
            }
        }
    }
	public int getComparatorOutput() {
		BeediDisk id=disk.getResource(0).get(Components.BEEDI_RECORD);
		return id==null?0:id.comparatorOutput();
	}

}
