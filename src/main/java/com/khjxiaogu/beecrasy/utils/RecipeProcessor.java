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

import net.minecraft.resources.Identifier;

/**
 * 配方处理器函数式接口。
 * <p>
 * 定义配方执行完成时的回调，接收配方ID并返回处理状态。
 */
public interface RecipeProcessor {
	/**
	 * 执行指定ID的配方。
	 *
	 * @param id 配方的资源标识符
	 * @return 执行后的处理状态
	 */
	RecipeHandleStatus run(Identifier id);
}
