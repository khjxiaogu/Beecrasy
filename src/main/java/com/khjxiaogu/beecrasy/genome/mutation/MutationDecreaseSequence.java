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

import com.khjxiaogu.beecrasy.genome.BeeHiveParameters;
import com.khjxiaogu.beecrasy.genome.DiploidGenome;
import com.khjxiaogu.beecrasy.genome.Genes;
import com.khjxiaogu.beecrasy.genome.Mutation;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;

import net.minecraft.util.RandomSource;

public class MutationDecreaseSequence implements Mutation {

	public MutationDecreaseSequence() {
	}

	@Override
	public boolean mutate(BeeHiveParameters params,DiploidGenome genome,RandomSource rnd) {
		if(genome.maternal().get(Genes.BIOTOPE)==genome.paternal().get(Genes.BIOTOPE)) {

			List<ProductItem> matSeqOriginal=genome.maternal().get(Genes.PRODUCTS);
			List<ProductItem> parSeqOriginal=genome.paternal().get(Genes.PRODUCTS);
			boolean flag1=matSeqOriginal.size()>1;
			boolean flag2=parSeqOriginal.size()>1;
			
			if(!flag1&&!flag2)return false;
			if(rnd.nextFloat()>.05f)return false;
			
			if(flag1&&flag2) {
				ArrayList<ProductItem> matSeq=new ArrayList<>(matSeqOriginal);
				ArrayList<ProductItem> parSeq=new ArrayList<>(parSeqOriginal);
				int r=rnd.nextInt(8);
				if (r < 3) {
					matSeq.remove(matSeq.size()-1);
	            } else if (r < 6) {
	            	parSeq.remove(parSeq.size()-1);
	            } else {
	            	matSeq.remove(matSeq.size()-1);
	            	parSeq.remove(parSeq.size()-1);
	            }
				genome.maternal().add(Genes.PRODUCTS, matSeq);
				genome.paternal().add(Genes.PRODUCTS, parSeq);
				
			}else if(flag1) {
				ArrayList<ProductItem> matSeq=new ArrayList<>(matSeqOriginal);
            	matSeq.remove(matSeq.size()-1);
				genome.maternal().add(Genes.PRODUCTS, matSeq);
			}else if(flag2) {
				ArrayList<ProductItem> parSeq=new ArrayList<>(parSeqOriginal);
				parSeq.remove(parSeq.size()-1);
				genome.paternal().add(Genes.PRODUCTS, parSeq);
			}
			return true;
		}
		return false;
	}

	@Override
	public int priority() {
		return Mutation.super.priority()+100;
	}

}
