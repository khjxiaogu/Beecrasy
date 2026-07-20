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

package com.khjxiaogu.beecrasy.genome.mutation;

import java.util.ArrayList;
import java.util.List;

import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterSet;
import com.khjxiaogu.beecrasy.genome.DiploidGenome;
import com.khjxiaogu.beecrasy.genome.Genes;
import com.khjxiaogu.beecrasy.genome.Mutation;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;

import net.minecraft.util.RandomSource;

/**
 * 序列削减突变，当双亲生境相同且产品序列长度大于1时，随机移除末尾产品项。
 */
public class MutationDecreaseSequence implements Mutation {

	public MutationDecreaseSequence() {
	}

	@Override
	public boolean mutate(BeeHiveParameterSet params, DiploidGenome genome, RandomSource rnd) {

		List<ProductItem> matSeqOriginal = genome.maternal().get(Genes.PRODUCTS);
		List<ProductItem> parSeqOriginal = genome.paternal().get(Genes.PRODUCTS);
		boolean flag1 = matSeqOriginal.size() > 1;
		boolean flag2 = parSeqOriginal.size() > 1;

		if (flag1 && flag2) {
			ArrayList<ProductItem> matSeq = new ArrayList<>(matSeqOriginal);
			ArrayList<ProductItem> parSeq = new ArrayList<>(parSeqOriginal);
			int r = rnd.nextInt(8);
			if (r < 3) {
				matSeq.remove(matSeq.size() - 1);
			} else if (r < 6) {
				parSeq.remove(parSeq.size() - 1);
			} else {
				matSeq.remove(matSeq.size() - 1);
				parSeq.remove(parSeq.size() - 1);
			}
			genome.maternal().add(Genes.PRODUCTS, matSeq);
			genome.paternal().add(Genes.PRODUCTS, parSeq);

		} else if (flag1) {
			ArrayList<ProductItem> matSeq = new ArrayList<>(matSeqOriginal);
			matSeq.remove(matSeq.size() - 1);
			genome.maternal().add(Genes.PRODUCTS, matSeq);
		} else if (flag2) {
			ArrayList<ProductItem> parSeq = new ArrayList<>(parSeqOriginal);
			parSeq.remove(parSeq.size() - 1);
			genome.paternal().add(Genes.PRODUCTS, parSeq);
		}
		return true;
	}

	@Override
	public float getChance(BeeHiveParameterSet params,DiploidGenome genome) {
		return .05f;
	}

	@Override
	public boolean isApplicable(BeeHiveParameterSet params, DiploidGenome genome) {
		if (genome.maternal().getAllele(Genes.BIOTOPE) != genome.paternal().getAllele(Genes.BIOTOPE))
			return false;
		List<ProductItem> matSeqOriginal = genome.maternal().getAllele(Genes.PRODUCTS);
		List<ProductItem> parSeqOriginal = genome.paternal().getAllele(Genes.PRODUCTS);
		boolean flag1 = matSeqOriginal.size() > 1;
		boolean flag2 = parSeqOriginal.size() > 1;
		return flag1 || flag2;
	}

}
