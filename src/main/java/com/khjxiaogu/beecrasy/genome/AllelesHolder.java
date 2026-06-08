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

/**
 * 等位基因持有者接口，提供按基因类型获取等位基因值的方法。
 */
public interface AllelesHolder {

	/**
	 * 获取指定基因类型的等位基因值。
	 *
	 * @param <T>  等位基因值的类型
	 * @param type 基因类型
	 * @return 等位基因值，可能为 {@code null}
	 */
	<T> T getAllele(Gene<T> type);

}