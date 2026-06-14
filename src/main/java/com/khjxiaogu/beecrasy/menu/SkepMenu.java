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
import com.khjxiaogu.beecrasy.blocks.BeeHiveBaseBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class SkepMenu extends BeeHiveBaseMenu {
	
	public SkepMenu(int containerId,Inventory inventory, RegistryFriendlyByteBuf bytebuf) {
		super(Menus.SKEP_MENU.get(), containerId,inventory,11,bytebuf);
	}

	public SkepMenu(int containerId,Inventory inventory, BeeHiveBaseBlockEntity blockEntity) {
		super(Menus.SKEP_MENU.get(), containerId,inventory,11,blockEntity);
	}



	@Override
	public void addSlots(ResourceHandler<ItemResource> slots, IndexModifier<ItemResource> slotModifier) {
		this.addSlot(new QueenSlot(slots,slotModifier,0,152,32));
		this.addSlot(new DroneSlot(slots,slotModifier,1,49,21));
		this.addSlot(new DroneSlot(slots,slotModifier,2,70,16));
		this.addSlot(new DroneSlot(slots,slotModifier,3,44,42));
		this.addSlot(new DroneSlot(slots,slotModifier,4,65,37));
		this.addSlot(new CombSlot(slots,slotModifier,5,86,32));
		this.addSlot(new CombSlot(slots,slotModifier,6,107,27));
		this.addSlot(new CombSlot(slots,slotModifier,7,81,53));
		this.addSlot(new CombSlot(slots,slotModifier,8,102,48));
		this.addSlot(new ArgumentationSlot(slots,slotModifier,9,14,29));
		this.addSlot(new ArgumentationSlot(slots,slotModifier,10,14,47));
		this.addPlayerInventory(8, 84, 142);
	}

}
