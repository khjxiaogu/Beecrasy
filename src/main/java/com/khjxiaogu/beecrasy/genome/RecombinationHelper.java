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

import java.util.function.BooleanSupplier;

import net.minecraft.resources.Identifier;

/**
 * 基因重组辅助工具类，实现亲代基因交换产生子代基因组的逻辑。
 */
public class RecombinationHelper {
	/**
	 * 根据布尔选择器从两个亲代基因组中抽取等位基因生成单倍基因组。
	 * <p>
	 * 遍历所有基因类型，当选择器返回 {@code true} 时取第二个亲代的基因值覆盖第一个亲代的对应值。
	 *
	 * @param genome    亲代基因组数组（[0]=第一个亲代，[1]=第二个亲代）
	 * @param extractor 布尔选择器，决定是否交换基因
	 * @return 生成的单倍基因组建构器
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Genome.Builder makeHaploid(Genome[] genome,BooleanSupplier extractor) {
		Genome.Builder b=genome[0].createBuilder();
		for(Identifier i:GeneRegistry.getGeneTypes()) {
			if(extractor.getAsBoolean()) {
				Gene type=GeneRegistry.get(i);
				b.add(type, genome[1].getAllele(type));
			}
		}
		return b;
	}

	/**
	 * 生成二倍体基因组，同时交换部分等位基因。
	 * <p>
	 * 先通过 {@link #makeHaploid} 生成母方单倍基因组，再将父方与母方的部分基因交换。
	 *
	 * @param genomes  亲代基因组数组
	 * @param genome   父方参考基因组（用于拷贝）
	 * @param extractor 布尔选择器，决定是否交换基因
	 * @return 二倍体基因组
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static DiploidGenome makeDiploid(Genome[] genomes,Genome genome,BooleanSupplier extractor) {
		Genome.Builder mat=makeHaploid(genomes,extractor);
		Genome.Builder par=genome.createBuilder();
		for(Identifier i:GeneRegistry.getGeneTypes()) {
			if(extractor.getAsBoolean()) {
				Gene type=GeneRegistry.get(i);
				Object palle=par.getAllele(type);
				Object malle=mat.getAllele(type);
				mat.add(type, palle);
				par.add(type, malle);
			}
		}
		return new DiploidGenome(mat,par);
	}
}
