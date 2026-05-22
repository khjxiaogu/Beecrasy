/** 
* Copyright (c) 2026 khjxiaogu
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
