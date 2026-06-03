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
import com.khjxiaogu.beecrasy.beehive.ErrCode;
import com.khjxiaogu.beecrasy.beehive.WorkBehaviour;
import com.khjxiaogu.beecrasy.blocks.BeeHiveBaseBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class SkepMenu extends BeecrasyContainerMenu {
	private ContainerData data;
	public SkepMenu(int containerId,Inventory inventory, RegistryFriendlyByteBuf bytebuf) {
		this(containerId,inventory,new ItemStacksResourceHandler(11));
		this.addDataSlots(data=new SimpleContainerData(4));
	}

	public SkepMenu(int containerId,Inventory inventory, BeeHiveBaseBlockEntity blockEntity) {
		this(containerId,inventory,blockEntity.component.getInternInv());
		this.addDataSlots(data=blockEntity.component.containerData());
		super.blockEntity=blockEntity;
	}

	protected SkepMenu(int containerId, Inventory inventory, ItemStacksResourceHandler slots) {
		super(Menus.SKEP_MENU.get(), containerId, inventory, 11);
		this.addSlot(new QueenSlot(slots,slots::set,0,152,32));
		this.addSlot(new DroneSlot(slots,slots::set,1,49,21));
		this.addSlot(new DroneSlot(slots,slots::set,2,70,16));
		this.addSlot(new DroneSlot(slots,slots::set,3,44,42));
		this.addSlot(new DroneSlot(slots,slots::set,4,65,37));
		this.addSlot(new CombSlot(slots,slots::set,5,86,32));
		this.addSlot(new CombSlot(slots,slots::set,6,107,27));
		this.addSlot(new CombSlot(slots,slots::set,7,81,53));
		this.addSlot(new CombSlot(slots,slots::set,8,102,48));
		this.addSlot(new ArgumentationSlot(slots,slots::set,9,14,29));
		this.addSlot(new ArgumentationSlot(slots,slots::set,10,14,47));
		this.addPlayerInventory(8, 84, 142);
	}
	public void cycleWork() {
		sendOperation(0,(data.get(1)+1)%3);
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
	@Override
	public boolean quickMoveIn(ItemStack slotStack) {
		return this.moveItemStackTo(slotStack, 0, 11, false);
	}

}
