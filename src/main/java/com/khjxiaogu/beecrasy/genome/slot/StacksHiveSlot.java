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

package com.khjxiaogu.beecrasy.genome.slot;

import java.util.ArrayList;
import java.util.List;

import com.khjxiaogu.beecrasy.genome.HiveSlot;
import com.mojang.serialization.Codec;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueInput.ValueInputList;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class StacksHiveSlot implements HiveSlot,ValueIOSerializable {
	public static final Codec<StacksHiveSlot> CODEC=ItemStack.CODEC.xmap(StacksHiveSlot::new, StacksHiveSlot::getItem);
	public static final Codec<List<StacksHiveSlot>> LIST_CODEC=CODEC.listOf();
	ItemStack stack;
	
	public StacksHiveSlot() {
		this(ItemStack.EMPTY);
	}

	public StacksHiveSlot(ItemStack stack) {
		super();
		this.stack = stack;
	}

	@Override
	public ItemStack getItem() {
		return stack;
	}

	@Override
	public void setItem(ItemStack stack) {
		this.stack=stack;
	}

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}
	public static List<StacksHiveSlot> createSlots(int count){
		List<StacksHiveSlot> slots=new ArrayList<>();
		for(int i=0;i<count;i++)
			slots.add(new StacksHiveSlot());
		return slots;
	}
	@SuppressWarnings("deprecation")
	@Override
	public void serialize(ValueOutput output) {
		if(!stack.isEmpty())
			output.store(ItemStack.MAP_CODEC,stack);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void deserialize(ValueInput input) {
		stack=input.read(ItemStack.MAP_CODEC).orElse(ItemStack.EMPTY);
	}
	public static void serialize(List<StacksHiveSlot> slots,ValueOutputList output) {
		
		for(StacksHiveSlot i:slots) {
			i.serialize(output.addChild());
		}
	}
	public static void deserialize(List<StacksHiveSlot> slots,ValueInputList input) {
		int idx=0;
		for(ValueInput i:input) {
			slots.get(idx).deserialize(i);
			idx++;
		}
	}
}
