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
 * 数值型等位基因，携带一个浮点数值（如产量倍率、繁殖力、寿命等）。
 */
public class NumericAllele extends BaseAllele {
	/** 数值。 */
	private final float number;

	/**
	 * 创建具有指定标识符和数值的等位基因。
	 *
	 * @param id     标识符字符串
	 * @param number 浮点数值
	 */
	public NumericAllele(String id, float number) {
		super(id);
		this.number = number;
	}

	/**
	 * 获取等位基因的数值。
	 *
	 * @return 浮点数值
	 */
	public float getNumber() {
		return number;
	}

}
