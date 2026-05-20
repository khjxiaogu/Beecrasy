package com.khjxiaogu.beecrasy.menu;

import com.khjxiaogu.beecrasy.network.ContainerOperationMessage;
import com.khjxiaogu.beecrasy.network.PacketHandler;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public abstract class BeecrasyContainerMenu extends AbstractContainerMenu implements OperatableMenu {

	protected BeecrasyContainerMenu(MenuType<?> menuType, int containerId) {
		super(menuType, containerId);
	}
	protected void sendOperation(short opCode,int opData) {
		PacketHandler.sendToServer(new ContainerOperationMessage(containerId,opCode,opData));
	}

}
