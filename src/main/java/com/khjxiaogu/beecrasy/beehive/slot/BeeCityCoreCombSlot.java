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

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.StacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class BeeCityCoreCombSlot extends ResourceStackHiveSlot {

	public BeeCityCoreCombSlot(StacksResourceHandler<ItemStack, ItemResource> handler, int slot) {
		super(handler, slot);
	}

	@Override
	public boolean is(Holder<Item> item) {
		if(item.is(Items.PRODUCT_COMB))
			return false;
		return super.is(item);
	}

	@Override
	public ItemStack getItem() {
		ItemStack stack=super.getItem();
		if(stack.is(Items.PRODUCT_COMB))
			return ItemStack.EMPTY;
		return stack;
	}

	@Override
	public boolean isEmpty() {
		ItemResource ir=handler.getResource(slot);
		return ir.isEmpty()||ir.is(Items.PRODUCT_COMB.get());
	}



}
