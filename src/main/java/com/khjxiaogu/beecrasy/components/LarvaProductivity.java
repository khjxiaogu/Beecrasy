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

package com.khjxiaogu.beecrasy.components;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.genome.Genes;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.ProductHelper;
import com.khjxiaogu.beecrasy.genome.ProductHelper.ProductWithCount;
import com.khjxiaogu.beecrasy.utils.BeecrasyMath;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public record LarvaProductivity(float biotopeProductive,float wildcardProductive) {
	public static final Codec<LarvaProductivity> CODEC=RecordCodecBuilder.create(t->t.group(
			Codec.FLOAT.fieldOf("biotope").forGetter(LarvaProductivity::biotopeProductive),
			Codec.FLOAT.fieldOf("wildcard").forGetter(LarvaProductivity::wildcardProductive)
			).apply(t, LarvaProductivity::new));
	public static final LarvaProductivity DEFAULT=new LarvaProductivity();
	private LarvaProductivity() {
		this(0,0);
	}
	public LarvaProductivity increaseBiotoped(float value) {
		return new LarvaProductivity(biotopeProductive+value,wildcardProductive);
	}
	public LarvaProductivity increaseWildcard(float value) {
		return new LarvaProductivity(biotopeProductive,wildcardProductive+value);
	}
	public ItemStack getProduction(Genome genome,RandomSource rs) {
		if(biotopeProductive()<wildcardProductive()) {
			return Items.PRODUCT_COMB.toStack(BeecrasyMath.getRandomRate(wildcardProductive(), rs));
		}else {
			ProductWithCount product=ProductHelper.pickSingleProduct(genome.getAllele(Genes.BIOTOPE), genome.getAllele(Genes.PRODUCTS), rs, BeecrasyMath.getRandomRate(biotopeProductive(), rs));
			return product.createProductComb();
		}
	}
}
