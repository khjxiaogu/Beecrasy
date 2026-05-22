package com.khjxiaogu.beecrasy.menu;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Menus;
import com.khjxiaogu.beecrasy.blocks.PressBlockEntity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class PressMenu extends BeecrasyContainerMenu {
	public PressMenu(int containerId,Inventory inventory, RegistryFriendlyByteBuf bytebuf) {
		this(containerId,inventory, 10);
		this.addDataSlots(new SimpleContainerData(2));
		this.addDummyTank(1000);
	}

	public PressMenu(int containerId,Inventory inventory, PressBlockEntity press) {
		this(containerId,inventory, 10);
		this.addDataSlots(press.getRecipeHandler());
		this.addFluidTank(press.tank,press.tank::set,0);
		super.blockEntity=press;
	}

	protected PressMenu(int containerId, Inventory inventory, int machineSlotCount) {
		super(Menus.PRESS_MENU.get(), containerId, inventory, machineSlotCount);
		
	}

	@Override
	public void receiveOperation(short opCode, int opData) {
		
	}

	@Override
	public boolean quickMoveIn(ItemStack slotStack) {
		return this.moveItemStackTo(slotStack, 0, 1, false);
	}

}
