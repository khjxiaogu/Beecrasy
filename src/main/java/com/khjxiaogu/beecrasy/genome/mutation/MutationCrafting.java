package com.khjxiaogu.beecrasy.genome.mutation;

import com.khjxiaogu.beecrasy.genome.DiploidGenome;
import com.khjxiaogu.beecrasy.genome.Genes;
import com.khjxiaogu.beecrasy.genome.Mutation;
import net.minecraft.util.RandomSource;

public class MutationCrafting implements Mutation{

	public MutationCrafting() {
	}

	@Override
	public boolean mutate(DiploidGenome genome, RandomSource rnd) {
		if(genome.maternal().get(Genes.BIOTOPE)==Genes.Alleles.CRAFT&&genome.paternal().get(Genes.BIOTOPE)==Genes.Alleles.CRAFT) {
			if(rnd.nextFloat()<.075f) {
				
				return true;
			}
		}
		return false;
	}

}
