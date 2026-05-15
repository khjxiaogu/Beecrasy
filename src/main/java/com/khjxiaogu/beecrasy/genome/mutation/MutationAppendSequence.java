package com.khjxiaogu.beecrasy.genome.mutation;

import java.util.ArrayList;
import java.util.List;

import com.khjxiaogu.beecrasy.genome.BeeHiveParameters;
import com.khjxiaogu.beecrasy.genome.DiploidGenome;
import com.khjxiaogu.beecrasy.genome.Genes;
import com.khjxiaogu.beecrasy.genome.Mutation;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;

import net.minecraft.util.RandomSource;

public class MutationAppendSequence implements Mutation {

	public MutationAppendSequence() {
	}

	@Override
	public boolean mutate(BeeHiveParameters params,DiploidGenome genome,RandomSource rnd) {
		if(genome.maternal().get(Genes.BIOTOPE)==genome.paternal().get(Genes.BIOTOPE)) {
			if(rnd.nextFloat()<.075f) {
				List<ProductItem> matSeqOriginal=genome.maternal().get(Genes.PRODUCTS);
				List<ProductItem> parSeqOriginal=genome.paternal().get(Genes.PRODUCTS);
				List<ProductItem> matSeq=new ArrayList<>(matSeqOriginal);
				List<ProductItem> parSeq=new ArrayList<>(parSeqOriginal);
				parSeq.addAll(matSeqOriginal);
				matSeq.addAll(parSeqOriginal);
				genome.maternal().add(Genes.PRODUCTS, matSeq);
				genome.paternal().add(Genes.PRODUCTS, parSeq);
				return true;
			}
		}
		return false;
	}

	@Override
	public int priority() {
		return Mutation.super.priority()+100;
	}

}
