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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.khjxiaogu.beecrasy.network.ContainerOperationMessage;
import com.khjxiaogu.beecrasy.network.ContainerTankMessage;
import com.khjxiaogu.beecrasy.network.ContainerTankMessage.MessagePair;
import com.khjxiaogu.beecrasy.network.PacketHandler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;

public abstract class BeecrasyContainerMenu extends AbstractContainerMenu implements OperatableMenu {
	public static record TankSlot(ResourceHandler<FluidResource> handler, IndexModifier<FluidResource> slotModifier, int index) {

	}

	private static class TankStack {
		protected final TankSlot slot;

		public TankStack(TankSlot slot) {
			super();
			this.slot = slot;
		}

		protected FluidResource resource;
		protected int amount;

		public boolean checkAndUpdate() {
			boolean updated = false;
			FluidResource rc = slot.handler().getResource(slot.index());
			int am = slot.handler().getAmountAsInt(slot.index());
			if (!Objects.equals(rc, resource))
				updated = true;
			if (am != amount)
				updated = true;
			resource = rc;
			amount = am;
			return updated;
		}

		public void set(FluidResource resource, int amount) {
			slot.slotModifier().set(slot.index(), resource, amount);
		}

		public FluidResource getResource() {
			return slot.handler().getResource(slot.index());
		}

		public int getAmount() {
			return slot.handler().getAmountAsInt(slot.index());
		}
	}

	private final List<TankStack> tanks = new ArrayList<>();

	protected void sendOperation(int opCode, int opData) {
		PacketHandler.sendToServer(new ContainerOperationMessage(containerId,(short) opCode, opData));
	}

	protected BlockEntity blockEntity;
	protected Inventory inventory;

	protected BeecrasyContainerMenu(MenuType<?> menuType,int containerId, Inventory inventory,  int machineSlotCount) {
		super(menuType, containerId);
		this.machineSlotCount = machineSlotCount;
		this.inventory = inventory;
	}

	protected final int machineSlotCount;
	protected static final int PLAYER_INVENTORY_SLOTS = 36;
	protected static final int PLAYER_QUICKBAR_INDEX = 27;

	protected void addPlayerInventory(int dx, int dy, int quickBarY) {
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlot(new Slot(inventory, j + i * 9 + 9, dx + j * 18, dy + i * 18));
		for (int i = 0; i < 9; i++)
			addSlot(new Slot(inventory, i, dx + i * 18, quickBarY));
	}

	public abstract boolean quickMoveIn(ItemStack slotStack);

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if (slot != null && slot.hasItem()) {
			ItemStack slotStack = slot.getItem();
			itemStack = slotStack.copy();
			if (index < machineSlotCount) {
				if (!this.moveItemStackTo(slotStack, machineSlotCount, PLAYER_INVENTORY_SLOTS + machineSlotCount, true)) {
					return ItemStack.EMPTY;
				}
				slot.onQuickCraft(slotStack, itemStack);
			} else if (index >= machineSlotCount) {
				if (!quickMoveIn(slotStack)) {
					if (index < PLAYER_QUICKBAR_INDEX + machineSlotCount) {
						if (!this.moveItemStackTo(slotStack, PLAYER_QUICKBAR_INDEX + machineSlotCount, PLAYER_INVENTORY_SLOTS + machineSlotCount, false))
							return ItemStack.EMPTY;
					} else if (index < PLAYER_INVENTORY_SLOTS + machineSlotCount && !this.moveItemStackTo(slotStack, machineSlotCount, PLAYER_QUICKBAR_INDEX + machineSlotCount, false))
						return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(slotStack, machineSlotCount, PLAYER_INVENTORY_SLOTS + machineSlotCount, false)) {
				return ItemStack.EMPTY;
			}
			if (slotStack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
			if (slotStack.getCount() == itemStack.getCount()) {
				return ItemStack.EMPTY;
			}
			slot.onTake(playerIn, slotStack);
		}
		return itemStack;
	}

	public void addFluidTank(ResourceHandler<FluidResource> handler, IndexModifier<FluidResource> slotModifier, int index) {
		addFluidTank(new TankSlot(handler,slotModifier,index));
	}
	public void addFluidTank(TankSlot tank) {
		tanks.add(new TankStack(tank));
	}

	public void addDummyTank(int capacity) {
		FluidStacksResourceHandler tank = new FluidStacksResourceHandler(1,1250);
		
		addFluidTank(tank,tank::set,0);
	}
	public FluidResource getResource(int index) {
		return tanks.get(index).getResource();
	}

	public int getAmount(int index) {
		return tanks.get(index).getAmount();
	}
	public void processPacket(ContainerTankMessage packets) {
		for (MessagePair packet : packets.list()) {
			TankStack slot = tanks.get(packet.tank());
			slot.set(packet.fluid(), packet.amount());
		}
	}

	@Override
	public void broadcastChanges() {

		super.broadcastChanges();

		ContainerTankMessage packet = new ContainerTankMessage(this.containerId);
		for (int i = 0; i < tanks.size(); i++) {
			TankStack slot = tanks.get(i);
			if (slot.checkAndUpdate()) {
				packet.list().add(new MessagePair(i, slot.getResource(), slot.getAmount()));
			}
		}
		if (!packet.list().isEmpty() && inventory.player instanceof ServerPlayer serverPlayer)
			PacketHandler.sendToPlayer(serverPlayer, packet);

	}

	@Override
	public void broadcastFullState() {
		super.broadcastFullState();
		ContainerTankMessage packet = new ContainerTankMessage(this.containerId);
		for (int i = 0; i < tanks.size(); i++) {
			TankStack slot = tanks.get(i);
			slot.checkAndUpdate();
			packet.list().add(new MessagePair(i, slot.getResource(), slot.getAmount()));

		}
		if (!packet.list().isEmpty() && inventory.player instanceof ServerPlayer serverPlayer)
			PacketHandler.sendToPlayer(serverPlayer, packet);
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		if (blockEntity == null)
			return true;
		return !blockEntity.isRemoved() && pPlayer.distanceToSqr(blockEntity.getBlockPos().getCenter()) <= 100;
	}
}
