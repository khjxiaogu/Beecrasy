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

/**
 * 物品类型验证工具类。
 * <p>
 * 提供判断物品是否为雄蜂、蜂后、蜜脾、蜂蜜等类型的方法。
 */
public class ItemValidateHelper {
	private ItemValidateHelper() {}
	/**
	 * 判断物品是否为雄蜂。
	 *
	 * @param stack 待验证的物品
	 * @return 如果是雄蜂则返回 {@code true}
	 */
	public static boolean isDrone(ItemStack stack) {
		return stack.is(Items.DRONE);
	}
	/**
	 * 判断物品是否为蜜脾（幼虫或雄蜂）。
	 *
	 * @param stack 待验证的物品
	 * @return 如果是蜜脾则返回 {@code true}
	 */
	public static boolean isComb(ItemStack stack) {
		return stack.is(Items.LARVA)||stack.is(Items.DRONE);
	}
	/**
	 * 判断物品是否为蜂后（幼虫或蜂后）。
	 *
	 * @param stack 待验证的物品
	 * @return 如果是蜂后则返回 {@code true}
	 */
	public static boolean isQueen(ItemStack stack) {
		return stack.is(Items.LARVA)||stack.is(Items.QUEEN_BEE);
	}
	/**
	 * 判断物品是否是增强组件。
	 *
	 * @param stack 待验证的物品
	 * @return 如果是增强组件则返回 {@code true}
	 */
	public static boolean isArgument(ItemStack stack) {
		return stack.has(Components.ARGUMENTATION);
	}
	/**
	 * 判断物品是否为蜂蜜滴。
	 *
	 * @param stack 待验证的物品
	 * @return 如果是蜂蜜滴则返回 {@code true}
	 */
	public static boolean isHoney(ItemStack stack) {
		return stack.is(Items.HONEY_DROP);
	}
}
