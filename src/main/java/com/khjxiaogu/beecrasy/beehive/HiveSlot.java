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

package com.khjxiaogu.beecrasy.beehive;

import java.util.List;

import net.minecraft.world.item.ItemStack;

/**
 * 蜂巢槽位接口。
 * 定义了蜂巢中各个槽位的读写契约，用于统一操作不同实现方式（如直接资源句柄或可序列化物品栈）的槽位。
 */
public interface HiveSlot {
	/**
	 * 获取该槽位中的物品栈副本。
	 * @return 槽位中的 ItemStack（副本修改不影响原槽位）
	 */
	public ItemStack getItem();
	/**
	 * 设置该槽位中的物品栈。
	 * @param stack 要放入的物品栈
	 */
	public void setItem(ItemStack stack);
	/**
	 * 检查该槽位是否为空。
	 * @return 如果槽位中没有物品则返回 true
	 */
	public boolean isEmpty();
	/**
	 * 将一个源槽位列表中的物品全部复制到目标槽位列表。
	 * 复制会在两个列表的公共长度范围内逐槽位进行。
	 * @param from 源槽位列表（数据来源）
	 * @param to   目标槽位列表（数据写入目标）
	 */
	public static void copy(List<? extends HiveSlot> from,List<? extends HiveSlot> to) {
		for(int i=0;i<Math.min(to.size(), from.size());i++) {
			to.get(i).setItem(from.get(i).getItem());
		}
	}
}
