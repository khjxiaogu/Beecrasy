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

import net.minecraft.network.chat.Component;

/**
 * 翻译记录，封装本地化键与对应文本组件的映射关系。
 *
 * @param key           本地化键（完整形式）
 * @param component     对应的可翻译文本组件
 * @param shortKey      简写本地化键（{@code key + ".short"}）
 * @param shortComponent 简写对应的可翻译文本组件
 */
public record Translation(String key,Component component,String shortKey,Component shortComponent){
	/** 缺失翻译的默认占位实例。 */
	public static final Translation MISSING=new Translation("missing",Component.literal("ERROR"),"missing",Component.literal("ERROR"));
	/**
	 * 构造翻译记录，自动从完整键推导出简写键及对应的可翻译文本组件。
	 *
	 * @param key 本地化键
	 */
	public Translation(String key) {
		this(key,Component.translatable(key),key+".short",Component.translatable(key+".short"));
	}
}