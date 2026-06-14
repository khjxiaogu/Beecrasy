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

import com.khjxiaogu.beecrasy.beehive.ErrCode;
import com.khjxiaogu.beecrasy.beehive.WorkBehaviour;
import com.khjxiaogu.beecrasy.blocks.BeeHiveBaseBlockEntity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public abstract class BeeHiveBaseMenu extends BeecrasyContainerMenu {
	protected ContainerData data;
	public BeeHiveBaseMenu(MenuType<?> menuType, int containerId,Inventory inventory,int slots,RegistryFriendlyByteBuf bytebuf) {
		this(menuType, containerId,inventory,slots,new ItemStacksResourceHandler(slots));
		this.addDataSlots(data=new SimpleContainerData(4));
	}

	public BeeHiveBaseMenu(MenuType<?> menuType, int containerId,Inventory inventory,int slots, BeeHiveBaseBlockEntity blockEntity) {
		this(menuType, containerId,inventory,slots,blockEntity.component.getInternInv());
		this.addDataSlots(data=blockEntity.component.containerData());
		super.blockEntity=blockEntity;
	}
	private BeeHiveBaseMenu(MenuType<?> menuType,int containerId, Inventory inventory,int slotCount, ItemStacksResourceHandler slots) {
		super(menuType, containerId, inventory, slotCount);
		addSlots(slots,slots::set);
	}
	public abstract void addSlots(ResourceHandler<ItemResource> slot, IndexModifier<ItemResource> slotModifier) ;
	public void cycleWork() {
		sendOperation(0,(data.get(1)+1)%3);
	}
	@Override
	public boolean quickMoveIn(ItemStack slotStack) {
		return this.moveItemStackTo(slotStack, 0, machineSlotCount, false);
	}
	@Override
	public void receiveOperation(short opCode, int opData) {
		if(opCode==0&&opData>=0&&opData<WorkBehaviour.values().length) {
			((BeeHiveBaseBlockEntity)blockEntity).component.work=WorkBehaviour.values()[opData];
			blockEntity.setChanged();
		}
	}

	public ErrCode getErrCode() {
		return ErrCode.values()[data.get(0)];
	}

	public WorkBehaviour getWorkBehaviour() {
		return WorkBehaviour.values()[data.get(1)];
	}

	public int getProcess() {
		return data.get(2);
	}

	public int getProcessMax() {
		return data.get(3);
	}

}