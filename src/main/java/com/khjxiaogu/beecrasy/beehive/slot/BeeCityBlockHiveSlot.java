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

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Capability;
import com.khjxiaogu.beecrasy.beehive.HiveSlot;
import com.khjxiaogu.beecrasy.blocks.HiveSlotProvider.HiveSlotType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BeeCityBlockHiveSlot implements HiveSlot {
	public final Level level;
	public final BlockPos pos;
	public final HiveSlotType type;
	public final int index;
	public BeeCityBlockHiveSlot(Level level, BlockPos pos, HiveSlotType type, int index) {
		super();
		this.level = level;
		this.pos = pos;
		this.type = type;
		this.index = index;
	}
	public HiveSlot getSlot() {
		if(!level.isLoaded(pos))
			return null;
		return level.getCapability(Capability.BEE_CITY_BLOCK, pos, null).getSlot(type, index);
	}
	@Override
	public ItemStack getItem() {
		return getSlot().getItem();
	}

	@Override
	public void setItem(ItemStack stack) {
		HiveSlot hs=getSlot();
		hs.setItem(stack);
	}

	@Override
	public boolean isEmpty() {
		HiveSlot hs=getSlot();
		return hs!=null&&hs.isEmpty();
	}

	@Override
	public boolean isValid() {
		HiveSlot hs=getSlot();
		return hs!=null&&hs.isValid();
	}
	public boolean isInvalid() {
		return level.isLoaded(pos)&&level.getCapability(Capability.BEE_CITY_BLOCK, pos, null)==null;
	}
	@Override
	public boolean is(Holder<Item> item) {
		HiveSlot hs=getSlot();
		return hs!=null&&hs.is(item);
	}

}
