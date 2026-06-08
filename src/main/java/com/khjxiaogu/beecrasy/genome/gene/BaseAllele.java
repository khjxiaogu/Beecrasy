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

package com.khjxiaogu.beecrasy.genome.gene;

/**
 * 等位基因基础实现类，存储并返回等位基因的标识符字符串。
 */
public class BaseAllele implements Allele {
	/** 等位基因的唯一字符串标识符。 */
	private final String id;
	
	@Override
	public String getId() {
		return id;
	}

	/**
	 * 创建具有指定标识符的等位基因。
	 *
	 * @param id 标识符字符串
	 */
	public BaseAllele(String id) {
		super();
		this.id = id;
	}

}
