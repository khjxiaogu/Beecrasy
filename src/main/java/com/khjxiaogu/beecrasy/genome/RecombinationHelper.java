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

public class RecombinationHelper {
	Genome[] genomes;
	public RecombinationHelper(Genome[] genomeSet) {
		genomes=genomeSet;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Genome.Builder makeHaploid(Genome[] genome,BooleanSupplier extractor) {
		Genome.Builder b=genome[0].createBuilder();
		for(Identifier i:GeneRegistry.getGeneTypes()) {
			if(extractor.getAsBoolean()) {
				Gene type=GeneRegistry.get(i);
				b.add(type, genome[1].getAllele(type));
			}
		}
		return b;
	}

	public Genome.Builder getHaploid(BooleanSupplier extractor) {
		return makeHaploid(genomes,extractor);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DiploidGenome getDiploid(Genome genome,BooleanSupplier extractor) {
		Genome.Builder mat=makeHaploid(genomes,extractor);
		Genome.Builder par=genome.createBuilder();
		for(Identifier i:GeneRegistry.getGeneTypes()) {
			if(extractor.getAsBoolean()) {
				Gene type=GeneRegistry.get(i);
				Object palle=par.get(type);
				Object malle=mat.get(type);
				mat.add(type, palle);
				par.add(type, malle);
			}
		}
		return new DiploidGenome(mat,par);
	}
}
