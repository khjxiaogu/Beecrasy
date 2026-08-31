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

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Menus;
import com.khjxiaogu.beecrasy.blocks.machine.GMBlockEntity;
import com.khjxiaogu.beecrasy.client.screens.sequencertabs.GeneticModifier;
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.genome.Gene;
import com.khjxiaogu.beecrasy.network.GeneticEditMessage;
import com.khjxiaogu.beecrasy.network.PacketHandler;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class GMMenu extends SequencerMenu{
	ContainerData data;
	GeneticModifier gene1;
	GeneticModifier gene2;
	ItemStacksResourceHandler handler;
	public GeneticModifier getGene1() {
		GenomeComponent gc=handler.getResource(0).get(Components.GENOME);
		if(gc==null)
			return null;
		return gene1;
	}
	public GeneticModifier getGene2() {
		GenomeComponent gc=handler.getResource(0).get(Components.GENOME);
		if(gc==null)
			return null;
		if(gc.size()<2)
			return null;
		return gene2;
	}
	private class GMGuiModifier implements GeneticModifier{
		final int genomeSlot;
		public GMGuiModifier(int genomeSlot) {
			super();
			this.genomeSlot = genomeSlot;
		}

		@Override
		public <T> T getAllele(Gene<T> type) {
			GenomeComponent gc=handler.getResource(0).get(Components.GENOME);
			return gc.getGenome(genomeSlot).getAllele(type);
		}

		@Override
		public <T> void setAllele(Gene<T> type, T value) {
			PacketHandler.sendToServer(new GeneticEditMessage(containerId, genomeSlot, type,value));
		}
		
	}
	/**
	 * @param buf  
	 */
	public GMMenu( int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
		this(containerId, inventory, new ItemStacksResourceHandler(1) {
			@Override
			public boolean isValid(int index, ItemResource resource) {
				return resource.has(Components.GENOME);
			}
		},new SimpleContainerData(1));
	}
	GMBlockEntity be;
	public GMBlockEntity getBe() {
		return be;
	}
	public GMMenu(int containerId, Inventory inventory, GMBlockEntity access) {
		this(containerId, inventory, access.inv,access);
		blockEntity=be=access;
	}
	public GMMenu(int containerId, Inventory inventory, ItemStacksResourceHandler slots,ContainerData data) {
		super(Menus.GM_MENU.get(), containerId, inventory, slots,slots::set);
		this.addDataSlots(data);
		this.data=data;
		this.handler=slots;
		gene1=new GMGuiModifier(0);
		gene2=new GMGuiModifier(1);
	}
	@Override
	public boolean quickMoveIn(ItemStack slotStack) {
		return super.quickMoveIn(slotStack)||this.moveItemStackTo(slotStack, 1, 2, false);
	}
	@Override
	public int getTab() {
		return data.get(0);
	}
	@Override
	public void removed(Player player) {
		super.removed(player);
	}
	@Override
	public void setTab(int tab) {
		this.data.set(0, tab);
		super.setTab(tab);
	}
	@Override
	protected void doSetTab(int tab) {
		be.page=tab;
		be.setChanged();
	}
	
	
}
