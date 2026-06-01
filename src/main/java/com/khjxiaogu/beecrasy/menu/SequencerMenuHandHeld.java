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

package com.khjxiaogu.beecrasy.menu;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Menus;
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.components.SequencerItemHandler;
import com.khjxiaogu.beecrasy.utils.ItemValidateHelper;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class SequencerMenuHandHeld extends SequencerMenu{
	
	public SequencerMenuHandHeld( int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
		super(Menus.SEQUENCER_HANDHELD_MENU.get(), containerId, inventory, new ItemStacksResourceHandler(2) {

			@Override
			public boolean isValid(int index, ItemResource resource) {
				if(index==0) {
					return resource.has(Components.GENOME);
				}
				return ItemValidateHelper.isHoney(resource.toStack());
			}
			
		});
	}
	public SequencerMenuHandHeld( int containerId, Inventory inventory, ItemAccess access) {
		this(containerId, inventory, new SequencerItemHandler(access));
	}
	public SequencerMenuHandHeld(int containerId, Inventory inventory, SequencerItemHandler slots) {
		super(Menus.SEQUENCER_HANDHELD_MENU.get(), containerId, inventory, slots,slots::set);
	}
    @Override
    public void slotsChanged(Container container) {
        
    	int honey=handler.getAmountAsInt(1);
    	int bee=handler.getAmountAsInt(0);
    	if(honey>0&&bee>0) {
    		ItemResource beeItem=handler.getResource(0);
    		GenomeComponent genome=beeItem.get(Components.GENOME);
    		if(genome!=null&&!genome.isInspected()) {
        		ItemResource honeyItem=handler.getResource(1);
        		try(Transaction trans=Transaction.openRoot()){
	        		if(handler.extract(0, beeItem, bee, trans)==bee) {
	        			if(handler.extract(1, honeyItem, 1, trans)==1) {
	            			if(handler.insert(0, beeItem.with(Components.GENOME, genome.asInspected()), bee, trans)==bee) {
	            				trans.commit();
	            				return;
	            			}
	            		}
	        		}
        		}
    		}
    	}
        
    }
	@Override
	protected void addSlots(ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> slotModifier) {
		super.addSlots(handler,slotModifier);
		this.addSlot(new ResourceHandlerSlot(handler,slotModifier,1,152,105));
	}
	@Override
	public boolean quickMoveIn(ItemStack slotStack) {
		return super.quickMoveIn(slotStack)||this.moveItemStackTo(slotStack, 1, 2, false);
	}
	
}
