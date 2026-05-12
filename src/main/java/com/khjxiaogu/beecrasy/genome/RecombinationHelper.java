package com.khjxiaogu.beecrasy.genome;

import com.khjxiaogu.beecrasy.genome.GeneRegistry.GeneType;
import com.khjxiaogu.beecrasy.utils.VonNeumannExtractor;

import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

public class RecombinationHelper {
	Genome[] genomes;
	public RecombinationHelper(Genome[] genomeSet) {
		genomes=genomeSet;
	}
	private static Genome.Builder makeHaploid(Genome[] genome,VonNeumannExtractor extractor) {
		Genome.Builder b=genome[0].createBuilder();
		for(Identifier i:GeneRegistry.getGeneTypes()) {
			if(extractor.nextBoolean()) {
				GeneType type=GeneRegistry.get(i);
				b.add(type, genome[1].getAllele(type));
			}
		}
		return b;
	}

	public Genome.Builder getHaploid(RandomSource rnd) {
		return makeHaploid(genomes,new VonNeumannExtractor(rnd::nextLong));
	}
	public DiploidGenome getDiploid(Genome genome,RandomSource rnd) {
		VonNeumannExtractor extractor=new VonNeumannExtractor(rnd::nextLong);
		Genome.Builder mat=makeHaploid(genomes,extractor);
		Genome.Builder par=genome.createBuilder();
		for(Identifier i:GeneRegistry.getGeneTypes()) {
			if(extractor.nextBoolean()) {
				GeneType type=GeneRegistry.get(i);
				Object palle=par.get(type);
				Object malle=mat.get(type);
				mat.add(type, palle);
				par.add(type, malle);
			}
		}
		return new DiploidGenome(mat,par);
	}
}
