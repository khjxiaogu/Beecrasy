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

/**
 * 基于 {@link StacksResourceHandler} 的蜂巢槽位实现。
 * 直接操作底层资源句柄中的指定索引，适用于需要直接与物品传输 API 交互的场景。
 */
public class ResourceStackHiveSlot implements HiveSlot {
	/** 底层资源句柄，提供对物品栈的读写能力。 */
	protected final StacksResourceHandler<ItemStack,ItemResource> handler;
	/** 在该资源句柄中对应的槽位索引。 */
	protected final int slot;
	/**
	 * 创建一个绑定到指定资源句柄和槽位的槽位适配器。
	 * @param handler 底层物品资源句柄
	 * @param slot    在该句柄中的槽位索引
	 */
	public ResourceStackHiveSlot(StacksResourceHandler<ItemStack,ItemResource> handler, int slot) {
		super();
		this.handler = handler;
		this.slot = slot;
	}
	/**
	 * 从资源句柄中获取指定槽位的物品栈。
	 * @return 物品栈（由资源句柄转换而来）
	 */
	@Override
	public ItemStack getItem() {
		return handler.getResource(slot).toStack();
	}
	/**
	 * 将物品栈设置到资源句柄的指定槽位中。
	 * @param stack 要设置的物品栈
	 */
	@Override
	public void setItem(ItemStack stack) {
		handler.set(slot, ItemResource.of(stack), stack.getCount());
	}
	/**
	 * 检查资源句柄中指定槽位是否为空。
	 * @return 如果该槽位的物品数量为 0 则返回 true
	 */
	@Override
	public boolean isEmpty() {
		return handler.getAmountAsInt(slot)==0;
	}
}
