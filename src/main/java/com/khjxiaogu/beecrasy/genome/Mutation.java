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

import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterSet;

import net.minecraft.util.RandomSource;

/**
 * 突变接口，定义蜜蜂基因组的突变行为。
 */
public interface Mutation {
	/**
	 * 执行突变。
	 *
	 * @param params 蜂箱参数集合
	 * @param genome 子代的二倍体基因组
	 * @param rnd    随机序列
	 * @return 是否跳过后续突变
	 */
	boolean mutate(BeeHiveParameterSet params,DiploidGenome genome,RandomSource rnd);
	/**
	 * 获取突变发生的概率基础值。
	 *
	 * @param params 蜂箱参数集合
	 * @param genome 子代的二倍体基因组
	 * @return 突变概率（0~1）
	 */
	float getChance(BeeHiveParameterSet params,DiploidGenome genome);
	/**
	 * 判断当前突变是否适用于给定的蜂箱参数和基因组。
	 *
	 * @param params 蜂箱参数集合
	 * @param genome 子代的二倍体基因组
	 * @return 如果适用则返回 {@code true}
	 */
	boolean isApplicable(BeeHiveParameterSet params,DiploidGenome genome);
}
