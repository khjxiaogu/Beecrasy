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
import com.khjxiaogu.beecrasy.blocks.bee.BeeHiveBaseBlockEntity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class HiveMenu extends BeeHiveBaseMenu {
	
	public HiveMenu(int containerId,Inventory inventory, RegistryFriendlyByteBuf bytebuf) {
		super(Menus.HIVE_MENU.get(), containerId,inventory,17,bytebuf);
	}

	public HiveMenu(int containerId,Inventory inventory, BeeHiveBaseBlockEntity blockEntity) {
		super(Menus.HIVE_MENU.get(), containerId,inventory,17,blockEntity);
	}



	@Override
	public void addSlots(ResourceHandler<ItemResource> slots, IndexModifier<ItemResource> slotModifier) {
		this.addSlot(new QueenSlot(slots,slotModifier,0,152,41));
		this.addSlot(new QueenSlot(slots,slotModifier,1,147,62));

		this.addSlot(new DroneSlot(slots,slotModifier,2,49,21));
		this.addSlot(new DroneSlot(slots,slotModifier,3,70,16));
		this.addSlot(new DroneSlot(slots,slotModifier,4,44,42));
		this.addSlot(new DroneSlot(slots,slotModifier,5,65,37));
		this.addSlot(new DroneSlot(slots,slotModifier,6,60,58));
		this.addSlot(new DroneSlot(slots,slotModifier,7,56,79));

		this.addSlot(new CombSlot(slots,slotModifier,8,91,11));
		this.addSlot(new CombSlot(slots,slotModifier,10,86,32));
		this.addSlot(new CombSlot(slots,slotModifier,12,81,53));
		this.addSlot(new CombSlot(slots,slotModifier,13,102,48));
		this.addSlot(new CombSlot(slots,slotModifier,11,76,74));
		this.addSlot(new CombSlot(slots,slotModifier,14,97,69));
		
		this.addSlot(new ArgumentationSlot(slots,slotModifier,15,14,29));
		this.addSlot(new ArgumentationSlot(slots,slotModifier,16,14,47));
		this.addSlot(new ArgumentationSlot(slots,slotModifier,17,14,65));
		this.addPlayerInventory(8, 103, 161);
	}

}
