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
import com.khjxiaogu.beecrasy.blocks.BeeHiveBaseBlockEntity.WorkBehaviour;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class SkepMenu extends BeecrasyContainerMenu {
	public ContainerData data;
	public SkepMenu(int containerId,Inventory inventory, RegistryFriendlyByteBuf bytebuf) {
		this(containerId,inventory,new ItemStacksResourceHandler(11));
		this.addDataSlots(data=new SimpleContainerData(4));
	}

	public SkepMenu(int containerId,Inventory inventory, BeeHiveBaseBlockEntity blockEntity) {
		this(containerId,inventory,blockEntity.getInternInv());
		this.addDataSlots(data=blockEntity.containerData());
		super.blockEntity=blockEntity;
	}

	protected SkepMenu(int containerId, Inventory inventory, ItemStacksResourceHandler machineSlotCount) {
		super(Menus.SKEP_MENU.get(), containerId, inventory, 11);
		this.addSlot(new QueenSlot(machineSlotCount,machineSlotCount::set,0,152,32));
		this.addSlot(new DroneSlot(machineSlotCount,machineSlotCount::set,1,49,21));
		this.addSlot(new DroneSlot(machineSlotCount,machineSlotCount::set,2,70,16));
		this.addSlot(new DroneSlot(machineSlotCount,machineSlotCount::set,3,44,42));
		this.addSlot(new DroneSlot(machineSlotCount,machineSlotCount::set,4,65,37));
		this.addSlot(new CombSlot(machineSlotCount,machineSlotCount::set,5,86,32));
		this.addSlot(new CombSlot(machineSlotCount,machineSlotCount::set,6,107,27));
		this.addSlot(new CombSlot(machineSlotCount,machineSlotCount::set,7,81,53));
		this.addSlot(new CombSlot(machineSlotCount,machineSlotCount::set,8,102,48));
		this.addSlot(new ResourceHandlerSlot(machineSlotCount,machineSlotCount::set,9,14,29));
		this.addSlot(new ResourceHandlerSlot(machineSlotCount,machineSlotCount::set,10,14,47));
		this.addPlayerInventory(8, 84, 142);
	}
	public void cycleWork() {
		sendOperation(0,(data.get(1)+1)%3);
	}
	@Override
	public void receiveOperation(short opCode, int opData) {
		if(opCode==0&&opData>=0&&opData<WorkBehaviour.values().length) {
			((BeeHiveBaseBlockEntity)blockEntity).work=WorkBehaviour.values()[opData];
			blockEntity.setChanged();
		}
	}

	@Override
	public boolean quickMoveIn(ItemStack slotStack) {
		return this.moveItemStackTo(slotStack, 0, 11, false);
	}

}
