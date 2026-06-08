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

import java.util.List;

import com.khjxiaogu.beecrasy.BeecrasyConfig;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;

import net.neoforged.neoforge.common.MutableDataComponentHolder;


/**
 * 基因组数据辅助工具类，提供基因组与物品组件的读写转换、表型获取等便捷方法。
 */
public final class GenomeDataHelper {
	private GenomeDataHelper() {
		
	}
	/**
	 * 将单倍基因组写入物品组件，同时设置染色物品模板和基因组组件。
	 *
	 * @param stack  物品数据组件持有者
	 * @param genome 单倍基因组
	 */
	public static void setHaploidGenome(MutableDataComponentHolder stack,Genome genome) {
		List<ProductItem> products=genome.getAllele(Genes.PRODUCTS);
		if(!products.isEmpty()) {
			stack.set(Components.TINT_STACK, products.get(0).stack());
		}
		stack.set(Components.GENOME, new GenomeComponent(false,genome));
	}
	/**
	 * 将二倍体基因组写入物品组件（使用 {@link DiploidGenome}）。
	 *
	 * @param stack  物品数据组件持有者
	 * @param genome 二倍体基因组
	 */
	public static void setDiploidGenome(MutableDataComponentHolder stack,DiploidGenome genome) {
		List<ProductItem> products=genome.maternal().getAllele(Genes.PRODUCTS);
		if(!products.isEmpty()) {
			stack.set(Components.TINT_STACK, products.get(0).stack());
		}
		stack.set(Components.GENOME, genome.toComponent());
	}
	/**
	 * 将两个单倍基因组作为二倍体写入物品组件。
	 *
	 * @param stack   物品数据组件持有者
	 * @param genome1 第一个单倍基因组
	 * @param genome2 第二个单倍基因组
	 */
	public static void setDiploidGenome(MutableDataComponentHolder stack,Genome genome1,Genome genome2) {
		List<ProductItem> products=genome1.getAllele(Genes.PRODUCTS);
		if(!products.isEmpty()) {
			stack.set(Components.TINT_STACK, products.get(0).stack());
		}
		stack.set(Components.GENOME, new GenomeComponent(false,genome1,genome2));
	}
	/**
	 * 标记基因组已被鉴定。
	 *
	 * @param stack 物品数据组件持有者
	 */
	public static void setGenomeInspected(MutableDataComponentHolder stack) {
		GenomeComponent component=stack.get(Components.GENOME);
		if(component!=null) {
			stack.set(Components.GENOME, component.asInspected());
			
		}
	}
	/**
	 * 从基因组组件获取表现型基因组（第一个基因组）。
	 *
	 * @param component 基因组组件
	 * @return 表现型基因组
	 */
	public static Genome getPhenoType(GenomeComponent component) {
		return component.getGenome(0);
	}
	/**
	 * 从基因组组件获取二倍体基因组数组。
	 *
	 * @param component 基因组组件
	 * @return 包含两个基因组的数组（不足时用默认值填充）
	 */
	public static Genome[] getAsDiploid(GenomeComponent component) {
		if(component.size()<=0)
			return new Genome[] {Genome.DEFAULT,Genome.DEFAULT};
		if(component.size()==1)
			return new Genome[] {component.getGenome(0),component.getGenome(0)};

		return new Genome[] {component.getGenome(0),component.getGenome(1)};
	}
	/**
	 * 从物品组件获取二倍体基因组数组。
	 *
	 * @param stack 物品数据组件持有者
	 * @return 包含两个基因组的数组（无组件时用默认值填充）
	 */
	public static Genome[] getAsDiploid(MutableDataComponentHolder stack) {
		GenomeComponent component=stack.get(Components.GENOME);
		if(component!=null) {
			return getAsDiploid(component);
		}
		return new Genome[] {Genome.DEFAULT,Genome.DEFAULT};
	}
	/**
	 * 从物品组件获取表现型基因组。
	 *
	 * @param stack 物品数据组件持有者
	 * @return 表现型基因组，无组件时返回默认基因组
	 */
	public static Genome getPhenoType(MutableDataComponentHolder stack) {
		GenomeComponent component=stack.get(Components.GENOME);
		if(component!=null) {
			return getPhenoType(component);
		}
		return Genome.DEFAULT;
	}
	/**
	 * 根据等位基因计算寿命对应的tick数。
	 *
	 * @param allele 等位基因持有者（基因组）
	 * @return 寿命tick数
	 */
	public static int getLifespanTicks(AllelesHolder allele) {
		return (int) (allele.getAllele(Genes.LIFESPAN).getNumber()*BeecrasyConfig.SERVER.LIFESPAN.getAsInt());
	}
}
