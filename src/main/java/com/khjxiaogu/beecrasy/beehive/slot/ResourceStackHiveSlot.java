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

package com.khjxiaogu.beecrasy.beehive.slot;

import com.khjxiaogu.beecrasy.beehive.HiveSlot;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.StacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class ResourceStackHiveSlot implements HiveSlot {
	protected final StacksResourceHandler<ItemStack,ItemResource> handler;
	protected final int slot;
	public ResourceStackHiveSlot(StacksResourceHandler<ItemStack,ItemResource> handler, int slot) {
		super();
		this.handler = handler;
		this.slot = slot;
	}
	@Override
	public ItemStack getItem() {
		return handler.getResource(slot).toStack();
	}
	@Override
	public void setItem(ItemStack stack) {
		handler.set(slot, ItemResource.of(stack), stack.getCount());
	}
	@Override
	public boolean isEmpty() {
		return handler.getAmountAsInt(slot)==0;
	}
}
