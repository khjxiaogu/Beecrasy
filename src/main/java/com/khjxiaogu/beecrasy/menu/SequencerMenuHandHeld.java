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

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Menus;
import com.khjxiaogu.beecrasy.components.SequencerItemHandler;
import com.khjxiaogu.beecrasy.utils.ItemValidateHelper;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class SequencerMenuHandHeld extends SequencerMenu{
	ItemAccess iaccess;
	SimpleContainerData tab=new SimpleContainerData(1);
	/**
	 * @param buf  
	 */
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
		this.addDataSlots(tab);
	}
	public SequencerMenuHandHeld( int containerId, Inventory inventory, ItemAccess access) {
		this(containerId, inventory, new SequencerItemHandler(access));
		iaccess=access;
		Integer seqtab=iaccess.getResource().get(Components.SEQUENCER_TAB);
		if(seqtab!=null)
			tab.set(0, seqtab);
	}
	public SequencerMenuHandHeld(int containerId, Inventory inventory, SequencerItemHandler slots) {
		super(Menus.SEQUENCER_HANDHELD_MENU.get(), containerId, inventory, slots,slots::set);
		this.addDataSlots(tab);
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
	@Override
	public boolean stillValid(Player pPlayer) {
		return iaccess==null||iaccess.getAmount()==1;
	}
	@Override
	public int getTab() {
		return tab.get(0);
	}
	@Override
	public void removed(Player player) {
		super.removed(player);
	}
	@Override
	public void setTab(int tab) {
		this.tab.set(0, tab);
		super.setTab(tab);
	}
	@Override
	protected void doSetTab(int tab) {
		try (Transaction trans=Transaction.openRoot()){
			if(this.iaccess.exchange(this.iaccess.getResource().with(Components.SEQUENCER_TAB, tab), 1, trans)==1) {
				this.tab.set(0, tab);
				trans.commit();
			}
		}
	}
	
	
}
