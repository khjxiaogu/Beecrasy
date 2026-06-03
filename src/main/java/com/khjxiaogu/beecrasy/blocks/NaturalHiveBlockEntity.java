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

import java.util.List;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.beehive.BeeHiveHandler;
import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterSet;
import com.khjxiaogu.beecrasy.beehive.slot.StacksHiveSlot;
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.events.NaturalBeeGenomeGenerateEvent;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.GenomeDataHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.NeoForge;

public class NaturalHiveBlockEntity extends BeecrasyBlockEntity{
	BeeHiveHandler hiveInfo;
	ItemStack queen=ItemStack.EMPTY;
	List<StacksHiveSlot> queenSlot=StacksHiveSlot.createSlots(1);
	List<StacksHiveSlot> combSlot=StacksHiveSlot.createSlots(2);
	List<StacksHiveSlot> droneSlot=StacksHiveSlot.createSlots(2);
	boolean isGrowthStarted=false;
	public NaturalHiveBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(Blocks.NATURAL_HIVE_BLOCKENTITY.get(), pWorldPosition, pBlockState);
		hiveInfo=new BeeHiveHandler(queenSlot, droneSlot, combSlot);
	}
	public void beginGrowth(ServerLevel level,BeeHiveParameterSet param) {
		if(isGrowthStarted)return;
		GenomeComponent comp=queen.get(Components.GENOME);
		if(comp!=null) {
			NaturalBeeGenomeGenerateEvent event = new NaturalBeeGenomeGenerateEvent(param, Genome.builder());
			NeoForge.EVENT_BUS.post(event);
			hiveInfo.beginWork(param, GenomeDataHelper.getAsDiploid(comp), List.of(event.genome.build()));
			isGrowthStarted=true;
		}
	}
	
	public void setQueen(ItemStack queen) {
		this.queen = queen;
	}
	@Override
	public void readCustomNBT(ValueInput nbt, boolean isClient) {
		StacksHiveSlot.deserialize(queenSlot, nbt.childrenListOrEmpty("queen"));
		StacksHiveSlot.deserialize(combSlot, nbt.childrenListOrEmpty("comb"));
		StacksHiveSlot.deserialize(droneSlot, nbt.childrenListOrEmpty("drone"));
		nbt.readChild("hiveInfo", hiveInfo);
		isGrowthStarted=nbt.getBooleanOr("growthStarted", false);
		queen=nbt.read("queenItem", ItemStack.CODEC).orElse(ItemStack.EMPTY);
	}

	@Override
	public void writeCustomNBT(ValueOutput nbt, boolean isClient) {
		StacksHiveSlot.serialize(queenSlot, nbt.childrenList("queen"));
		StacksHiveSlot.serialize(combSlot, nbt.childrenList("comb"));
		StacksHiveSlot.serialize(droneSlot, nbt.childrenList("drone"));
		nbt.putChild("hiveInfo", hiveInfo);
		nbt.putBoolean("growthStarted", isGrowthStarted);
		nbt.store("queenItem", ItemStack.CODEC, queen);
	}

	@Override
	public void tick() {
		if(level instanceof ServerLevel serverLevel) {
			BeeHiveParameterSet params=new BeeHiveParameterSet.Builder(serverLevel,worldPosition).build();
			beginGrowth(serverLevel,params);
			hiveInfo.tick(params);
			int oldstate=this.getBlockState().getValue(BlockStateProperties.AGE_2);
			int newstate=hiveInfo.isWorking()?(hiveInfo.getProcess()<hiveInfo.getProcessMax()/2?1:0):2;
			if(oldstate!=newstate) {
				BlockState nextstate=getBlockState().setValue(BlockStateProperties.AGE_2, newstate);
				this.level.setBlockAndUpdate(worldPosition, nextstate);
			}
		}
	}

}
