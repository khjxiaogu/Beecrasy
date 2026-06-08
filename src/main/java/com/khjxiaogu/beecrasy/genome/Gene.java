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

package com.khjxiaogu.beecrasy.genome;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/**
 * 基因类型接口，仅用于访问权限控制。
 *
 * @param <T> 等位基因值的类型
 */
public interface Gene<T>{
	/**
	 * 获取该基因的默认等位基因值。
	 *
	 * @return 默认等位基因值
	 */
	public T getDefault();
	/**
	 * 获取该基因的唯一标识符。
	 *
	 * @return 标识符
	 */
	public Identifier id();
	/**
	 * 获取该基因等位基因值的 {@link Codec} 编解码器。
	 *
	 * @return Codec 编解码器
	 */
	public Codec<T> codec();
	/**
	 * 获取该基因等位基因值的 {@link StreamCodec} 流编解码器。
	 *
	 * @return StreamCodec 流编解码器
	 */
	public StreamCodec<RegistryFriendlyByteBuf,T> streamCodec();
	/**
	 * 获取基因名称的可读文本组件。
	 *
	 * @return 可读文本组件
	 */
	public Component getReadableText();
	/**
	 * 获取指定等位基因值的可读文本组件。
	 *
	 * @param allele 等位基因值
	 * @return 可读文本组件
	 */
	public Component getReadableText(T allele);
	/**
	 * 从基因组中获取该基因的值并返回可读文本组件。
	 *
	 * @param genome 等位基因持有者（基因组）
	 * @return 可读文本组件
	 */
	public default Component getReadableText(AllelesHolder genome) {
		return getReadableText(genome.getAllele(this));
	}
	/**
	 * 获取基因名称的简写可读文本组件。
	 *
	 * @return 简写可读文本组件
	 */
	public Component getShortReadableText();
	/**
	 * 获取指定等位基因值的简写可读文本组件。
	 *
	 * @param genome 等位基因值
	 * @return 简写可读文本组件
	 */
	public Component getShortReadableText(T genome);
	/**
	 * 从基因组中获取该基因的值并返回简写可读文本组件。
	 *
	 * @param genome 等位基因持有者（基因组）
	 * @return 简写可读文本组件
	 */
	public default Component getShortReadableText(AllelesHolder genome) {
		return getShortReadableText(genome.getAllele(this));
	}
	/**
	 * 获取基因名称的本地化语言键。
	 *
	 * @return 语言键
	 */
	public String getLanguageKey();
	/**
	 * 获取基因名称的简写本地化语言键。
	 *
	 * @return 简写语言键
	 */
	public String getShortLanguageKey();;
}