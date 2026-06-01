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

package com.khjxiaogu.beecrasy.utils;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;

import net.minecraft.world.item.ItemStack;

public class ItemValidateHelper {
	private ItemValidateHelper() {}
	public static boolean isDrone(ItemStack stack) {
		return stack.is(Items.DRONE);
	}
	public static boolean isComb(ItemStack stack) {
		return stack.is(Items.LARVA)||stack.is(Items.DRONE);
	}
	public static boolean isQueen(ItemStack stack) {
		return stack.is(Items.LARVA)||stack.is(Items.QUEEN_BEE);
	}
	public static boolean isArgument(ItemStack stack) {
		return stack.has(Components.ARGUMENTATION);
	}
	public static boolean isHoney(ItemStack stack) {
		return stack.is(Items.HONEY_DROP);
	}
}
