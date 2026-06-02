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

import com.khjxiaogu.beecrasy.BeecrasyConfig;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Menus;
import com.khjxiaogu.beecrasy.blocks.SequencerBlockEntity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class SequencerMenuBlock extends SequencerMenu {
	ContainerData data;
	public SequencerMenuBlock(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
		super(Menus.SEQUENCER_HANDHELD_MENU.get(), containerId, inventory, new ItemStacksResourceHandler(1) {
			@Override
			public boolean isValid(int index, ItemResource resource) {
				if(index==0) {
					return resource.has(Components.GENOME);
				}
				return super.isValid(index, resource);
			}
		});
		this.addDataSlots(data=new SimpleContainerData(1));
		this.addDummyTank(2000);
		
	}
	public SequencerMenuBlock( int containerId, Inventory inventory, SequencerBlockEntity access) {
		this(containerId, inventory, access.inv);
		this.addDataSlots(data=access);
		this.addFluidTank(access.tank, access.tank::set, 0);
	}
	protected SequencerMenuBlock(int containerId, Inventory inventory, ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> slotModifier) {
		super(Menus.SEQUENCER_BLOCK_MENU.get(), containerId, inventory, handler, slotModifier);

	}

	public SequencerMenuBlock(int containerId, Inventory inventory, ItemStacksResourceHandler handler) {
		super(Menus.SEQUENCER_BLOCK_MENU.get(), containerId, inventory, handler);
	}
	public int getEnergy() {
		return data.get(0);
	}
	public int getWorkEnergy() {
		return BeecrasyConfig.SERVER.SEQUENCER_ENERGY.getAsInt();
	}

}
