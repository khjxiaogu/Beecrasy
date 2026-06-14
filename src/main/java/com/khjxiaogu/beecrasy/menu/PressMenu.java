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
import com.khjxiaogu.beecrasy.blocks.PressBlockEntity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class PressMenu extends BeecrasyContainerMenu {
	private final ContainerData data;
	
	/**
	 * @param bytebuf  
	 */
	public PressMenu(int containerId,Inventory inventory, RegistryFriendlyByteBuf bytebuf) {
		this(containerId,inventory,new SimpleContainerData(2),new ItemStacksResourceHandler(10));
		this.addDummyTank(1000);
	}

	public PressMenu(int containerId,Inventory inventory, PressBlockEntity press) {
		this(containerId,inventory,press.getRecipeHandler(),press.getInternInv());
		this.addFluidTank(press.tank,press.tank::set,0);
		super.blockEntity=press;
	}

	protected PressMenu(int containerId, Inventory inventory,ContainerData data,ItemStacksResourceHandler slots) {
		super(Menus.PRESS_MENU.get(), containerId, inventory, 10);
		this.data=data;
		this.addDataSlots(data);
		this.addSlot(new ResourceHandlerSlot(slots,slots::set,0,44,37));
		for(int x=0;x<3;x++)
			for(int y=0;y<3;y++)
				this.addSlot(new OutputSlot(slots,slots::set,y*3+x+1,119+x*18,9+y*18));
		this.addPlayerInventory(8, 84, 142);
	}
	public int getProcess() {
		return data.get(0);
	}
	public int getProcessMax() {
		return data.get(1);
	}
	@Override
	public void receiveOperation(short opCode, int opData) {
		
	}

	@Override
	public boolean quickMoveIn(ItemStack slotStack) {
		return this.moveItemStackTo(slotStack, 0, 1, false);
	}

}
