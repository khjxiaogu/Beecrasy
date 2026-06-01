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
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

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
	protected void addSlots(ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> slotModifier) {
		super.addSlots(handler,slotModifier);
		this.addSlot(new ResourceHandlerSlot(handler,slotModifier,1,152,105));
	}
	
}
