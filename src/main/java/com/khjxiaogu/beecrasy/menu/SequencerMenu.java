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

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class SequencerMenu extends BeecrasyContainerMenu {
	
	protected SequencerMenu(MenuType<?> menuType,int containerId, Inventory inventory,ItemStacksResourceHandler handler) {
		this(menuType, containerId, inventory, handler,handler::set);
	}

	protected SequencerMenu(MenuType<?> menuType,int containerId, Inventory inventory,ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> slotModifier) {
		super(menuType, containerId, inventory, 10);
		addSlots(handler,slotModifier);
		this.addPlayerInventory(8, 140, 198);
	}
	protected void addSlots(ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> slotModifier) {
		this.addSlot(new ResourceHandlerSlot(handler,slotModifier,0,152,20));
	}
	
	@Override
	public void receiveOperation(short opCode, int opData) {
		
	}

	@Override
	public boolean quickMoveIn(ItemStack slotStack) {
		return this.moveItemStackTo(slotStack, 0, 1, false);
	}

}
