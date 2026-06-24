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

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Menus;
import com.khjxiaogu.beecrasy.blocks.bee.beecity.BeeCityCombBlockEntity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class BeeCityCombMenu extends BeecrasyContainerMenu {
	
	public BeeCityCombMenu(int containerId,Inventory inventory, RegistryFriendlyByteBuf bytebuf) {
		super(Menus.BEE_CITY_COMB.get(), containerId,inventory,2);
		ItemStacksResourceHandler container=new ItemStacksResourceHandler(2);
		addSlots(container,container::set);
	}

	public BeeCityCombMenu(int containerId,Inventory inventory, BeeCityCombBlockEntity blockEntity) {
		super(Menus.BEE_CITY_COMB.get(), containerId,inventory,2);
		addSlots(blockEntity.container,blockEntity.container::set);
	}


	public void addSlots(ResourceHandler<ItemResource> slots, IndexModifier<ItemResource> slotModifier) {
		this.addSlot(new CombSlot(slots,slotModifier,0,73,21));
		this.addSlot(new CombSlot(slots,slotModifier,1,89,37));
		this.addPlayerInventory(8, 84, 142);
	}

	@Override
	public void receiveOperation(short opCode, int opData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean quickMoveIn(ItemStack slotStack) {
		return this.moveItemStackTo(slotStack, 0, 2, false);
	}

}
