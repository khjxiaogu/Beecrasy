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


import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyConfig;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Tags;
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.menu.SequencerMenuBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.energy.LimitingEnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class SequencerBlockEntity extends BeecrasyBlockEntity implements MenuProvider,ContainerData {
	public final int energyCost=BeecrasyConfig.SERVER.SEQUENCER_ENERGY.getAsInt();
	public static final int ENERGY_BUFF=4;
	public int page;
	public SimpleEnergyHandler energy=new SimpleEnergyHandler(energyCost*ENERGY_BUFF) {
		@Override
		protected void onEnergyChanged(int previousAmount) {
			setChanged();
			super.onEnergyChanged(previousAmount);
		}
	};
	public ItemStacksResourceHandler inv=new ItemStacksResourceHandler(1) {
		@Override
		protected void onContentsChanged(int slot,ItemStack stackBefore) {
			setChanged();
			super.onContentsChanged(slot,stackBefore);
		}
		@Override
		public boolean isValid(int index, ItemResource resource) {
			return super.isValid(index, resource)&&resource.has(Components.GENOME);
		}
	};
	public FluidStacksResourceHandler tank = new FluidStacksResourceHandler(1,2000) {
		
		@Override
		protected void onContentsChanged(int slot,FluidStack stackBefore) {
			setChanged();
			super.onContentsChanged(slot,stackBefore);
		}

		@Override
		public boolean isValid(int index, FluidResource resource) {
			return super.isValid(index, resource)&&resource.is(Tags.HONEY);
		}
	};
	public DelegatingResourceHandler<ItemResource> invTransport=new DelegatingResourceHandler<>(inv) {

		@Override
		public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
			if((!resource.has(Components.GENOME))||resource.get(Components.GENOME).isInspected())
				return super.extract(index, resource, amount, transaction);
			return 0;
		}
		
	};
	public LimitingEnergyHandler energyTransport=new LimitingEnergyHandler(energy, BeecrasyConfig.SERVER.SEQUENCER_THROUGHPUT.getAsInt(), 0);
	public SequencerBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(Blocks.SEQUENCER_BLOCKENTITY.get(), pWorldPosition, pBlockState);
	}

	@Override
	public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
		return new SequencerMenuBlock(containerId,inventory,this);
	}

	@Override
	public Component getDisplayName() {
		return Blocks.SEQUENCER.get().getName();
	}

	@Override
	public void readCustomNBT(ValueInput nbt, boolean isClient) {
		if(!isClient) {
			nbt.readChild("energy", energy);
			nbt.readChild("inv", inv);
			nbt.readChild("fluid", tank);
			page=nbt.getIntOr("page", 0);
		}
	}

	@Override
	public void writeCustomNBT(ValueOutput nbt, boolean isClient) {
		if(!isClient) {
			nbt.putChild("energy", energy);
			nbt.putChild("inv", inv);
			nbt.putChild("fluid", tank);
			nbt.putInt("page", page);
		}
	}

	@Override
	public void tick() {
		if(!level.isClientSide()) {
			ItemResource ir=inv.getResource(0);
			if(!ir.isEmpty()) {
				GenomeComponent gc=ir.get(Components.GENOME);
				if(gc!=null&&!gc.isInspected()) {
					try(Transaction trans=Transaction.openRoot()){
						if(energy.extract(energyCost, trans)==energyCost) {
							int fluidCost=BeecrasyConfig.SERVER.SEQUENCER_HONEY.getAsInt();
							FluidResource honey=tank.getResource(0);
							if(tank.extract(0,honey, fluidCost, trans)==fluidCost) {
								int amt=inv.getAmountAsInt(0);
								if(inv.extract(0, ir, amt, trans)==amt) {
									if(inv.insert(0, ir.with(Components.GENOME, gc.asInspected()), amt, trans)==amt) {
										trans.commit();
										this.setChanged();
									}
								}
							}
						}
					}
				}
			}
			BlockState bs=this.getBlockState();
			boolean lit=bs.getValue(BlockStateProperties.LIT);
			boolean afterLit=inv.getAmountAsInt(0)>0;
			if(lit!=afterLit) {
				BlockState nextstate=getBlockState().setValue(BlockStateProperties.LIT, afterLit);
				this.level.setBlockAndUpdate(worldPosition, nextstate);
			}
		}
	}

	@Override
	public int get(int dataId) {
		if(dataId==0)
			return energy.getAmountAsInt();
		if(dataId==1)
			return page;
		return 0;
	}

	@Override
	public void set(int dataId, int value) {
		if(dataId==0)
			energy.set(value);
		if(dataId==1)
			page=value;
		
	}

	@Override
	public int getCount() {
		return 2;
	}

}
