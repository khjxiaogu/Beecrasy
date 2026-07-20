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

import java.util.HashMap;
import java.util.Map;

import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterSet;
import com.khjxiaogu.beecrasy.genome.DiploidGenome;
import com.khjxiaogu.beecrasy.genome.Genes;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.Genome.Builder;
import com.khjxiaogu.beecrasy.genome.Mutation;
import com.khjxiaogu.beecrasy.genome.gene.NumericAllele;
import net.minecraft.util.RandomSource;

/**
 * 产量增加突变。
 */
public abstract class MutationIncreaseYield implements Mutation {
	public static record YieldModifier(NumericAllele to,float chance) {
		
	}
	public static final Map<NumericAllele,YieldModifier> MUTATIONS=new HashMap<>();
	static {
		MUTATIONS.put(Genes.Alleles.MEAGER_YIELD, new YieldModifier(Genes.Alleles.MODERATE_YIELD,0.05f));
		MUTATIONS.put(Genes.Alleles.MODERATE_YIELD, new YieldModifier(Genes.Alleles.ABUNDANT_YIELD,0.0025f));
		MUTATIONS.put(Genes.Alleles.ABUNDANT_YIELD, new YieldModifier(Genes.Alleles.BUMPER_YIELD,0.000125f));
	}
	public MutationIncreaseYield() {
	}
	public static class Paternal extends MutationIncreaseYield{
		@Override
		public Builder getBuilder(DiploidGenome genome) {
			return genome.paternal();
		}
	}
	public static class Maternal extends MutationIncreaseYield{
		@Override
		public Builder getBuilder(DiploidGenome genome) {
			return genome.maternal();
		}
	}
	public abstract Genome.Builder getBuilder(DiploidGenome genome);
	@Override
	public boolean mutate(BeeHiveParameterSet params,DiploidGenome genome,RandomSource rnd) {

		YieldModifier m1= MUTATIONS.get(getBuilder(genome).getAllele(Genes.YIELD));
		if(m1!=null) {
			genome.maternal().add(Genes.YIELD, m1.to());
			return true;
		}
		return false;
	}

	@Override
	public float getChance(BeeHiveParameterSet params,DiploidGenome genome) {
		YieldModifier m1= MUTATIONS.get(getBuilder(genome).getAllele(Genes.YIELD));
		return m1==null?0:m1.chance();
	}

	@Override
	public boolean isApplicable(BeeHiveParameterSet params, DiploidGenome genome) {
		return MUTATIONS.containsKey(getBuilder(genome).getAllele(Genes.YIELD));
	}

}
