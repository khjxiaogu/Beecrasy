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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.khjxiaogu.beecrasy.beehive.HiveSlot;
import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueInput.ValueInputList;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/**
 * 可序列化的蜂巢槽位实现。
 * 内部持有 {@link ItemStack}，支持通过 NeoForge 的 ValueIO 进行 NBT 持久化，
 * 也支持 Codec 和 StreamCodec 的数据序列化，适用于磁盘存档和网络同步。
 */
public class StacksHiveSlot implements HiveSlot,ValueIOSerializable {
	/** 单个槽位的编解码器，基于 ItemStack 的可选编解码器。 */
	public static final Codec<StacksHiveSlot> CODEC=ItemStack.OPTIONAL_CODEC.xmap(StacksHiveSlot::new, StacksHiveSlot::getItem);
	/** 槽位列表的编解码器。 */
	public static final Codec<List<StacksHiveSlot>> LIST_CODEC=CODEC.listOf();
	/** 单个槽位的网络流式编解码器。 */
	public static final StreamCodec<RegistryFriendlyByteBuf,StacksHiveSlot> STREAM_CODEC=ItemStack.OPTIONAL_STREAM_CODEC.map(StacksHiveSlot::new, StacksHiveSlot::getItem);
	/** 槽位列表的网络流式编解码器。 */
	public static final StreamCodec<RegistryFriendlyByteBuf,List<StacksHiveSlot>> LIST_STREAM_CODEC=STREAM_CODEC.apply(ByteBufCodecs.list());
	/** 内部持有的物品栈。 */
	ItemStack stack;
	
	/**
	 * 创建一个空槽位，内部持有 {@link ItemStack#EMPTY}。
	 */
	public StacksHiveSlot() {
		this(ItemStack.EMPTY);
	}

	/**
	 * 创建一个持有指定物品栈的槽位。
	 * @param stack 要持有的物品栈
	 */
	public StacksHiveSlot(ItemStack stack) {
		super();
		this.stack = stack;
	}

	/**
	 * 获取内部物品栈的副本。
	 * @return 物品栈的副本（修改不影响原槽位）
	 */
	@Override
	public ItemStack getItem() {
		return stack.copy();
	}

	/**
	 * 设置内部物品栈。
	 * @param stack 要持有的物品栈（直接引用，非副本）
	 */
	@Override
	public void setItem(ItemStack stack) {
		this.stack=stack;
	}

	/**
	 * 检查内部物品栈是否为空。
	 * @return 如果内部栈为 {@link ItemStack#EMPTY} 则返回 true
	 */
	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}
	/**
	 * 创建指定数量的空槽位列表。
	 * @param count 槽位数量
	 * @return 包含 count 个空 StacksHiveSlot 的列表
	 */
	public static List<StacksHiveSlot> createSlots(int count){
		List<StacksHiveSlot> slots=new ArrayList<>();
		for(int i=0;i<count;i++)
			slots.add(new StacksHiveSlot());
		return slots;
	}
	/**
	 * 从现有 HiveSlot 列表复制创建槽位列表。
	 * 每个新槽位持有对应源槽位物品栈的副本。
	 * @param list 源槽位列表
	 * @return 新创建的 StacksHiveSlot 列表
	 */
	public static List<StacksHiveSlot> createSlots(List<? extends HiveSlot> list){
		List<StacksHiveSlot> slots=new ArrayList<>(list.size());
		for(int i=0;i<list.size();i++)
			slots.add(new StacksHiveSlot(list.get(i).getItem()));
		return slots;
	}
	/**
	 * 将内部物品栈序列化到 ValueOutput 中。
	 * 仅当物品栈非空时才写入数据。
	 * @param output 输出目标
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void serialize(ValueOutput output) {
		if(!stack.isEmpty())
			output.store(ItemStack.MAP_CODEC,stack);
	}

	/**
	 * 从 ValueInput 中反序列化恢复内部物品栈。
	 * 如果输入中不存在有效数据，则设为空栈。
	 * @param input 输入源
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void deserialize(ValueInput input) {
		stack=input.read(ItemStack.MAP_CODEC).orElse(ItemStack.EMPTY);
	}
	/**
	 * 批量将槽位列表序列化到 ValueOutputList 中。
	 * @param slots  槽位列表
	 * @param output 输出列表（每个槽位作为独立子项写入）
	 */
	public static void serialize(List<StacksHiveSlot> slots,ValueOutputList output) {
		
		for(StacksHiveSlot i:slots) {
			i.serialize(output.addChild());
		}
	}
	/**
	 * 从 ValueInputList 中批量反序列化恢复槽位列表。
	 * 输入列表中每个子项对应一个槽位，按顺序填充。
	 * @param slots 待填充的槽位列表
	 * @param input 输入列表源
	 */
	public static void deserialize(List<StacksHiveSlot> slots,ValueInputList input) {
		int idx=0;
		for(ValueInput i:input) {
			slots.get(idx).deserialize(i);
			idx++;
		}
	}

	@Override
	public int hashCode() {
		return ItemStack.hashItemAndComponents(stack);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		StacksHiveSlot other = (StacksHiveSlot) obj;
		return Objects.equals(stack, other.stack);
	}
}
