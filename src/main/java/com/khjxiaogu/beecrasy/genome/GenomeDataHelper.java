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


public final class GenomeDataHelper {
	private GenomeDataHelper() {
		
	}
	public static void setHaploidGenome(MutableDataComponentHolder stack,Genome genome) {
		List<ProductItem> products=genome.getAllele(Genes.PRODUCTS);
		if(!products.isEmpty()) {
			stack.set(Components.TINT_STACK, products.get(0).stack().create());
		}
		stack.set(Components.GENOME, new GenomeComponent(false,genome));
	}
	public static void setDiploidGenome(MutableDataComponentHolder stack,DiploidGenome genome) {
		List<ProductItem> products=genome.maternal().getAllele(Genes.PRODUCTS);
		if(!products.isEmpty()) {
			stack.set(Components.TINT_STACK, products.get(0).stack().create());
		}
		stack.set(Components.GENOME, new GenomeComponent(false,genome.maternal().build(),genome.paternal().build()));
	}
	public static void setDiploidGenome(MutableDataComponentHolder stack,Genome genome1,Genome genome2) {
		List<ProductItem> products=genome1.getAllele(Genes.PRODUCTS);
		if(!products.isEmpty()) {
			stack.set(Components.TINT_STACK, products.get(0).stack().create());
		}
		stack.set(Components.GENOME, new GenomeComponent(false,genome1,genome2));
	}
	public static void setGenomeInspected(MutableDataComponentHolder stack) {
		GenomeComponent component=stack.get(Components.GENOME);
		if(component!=null) {
			stack.set(Components.GENOME, component.asInspected());
			
		}
	}
	public static AllelesHolder getPhenoType(GenomeComponent component) {
		return component.getGenome(0);
	}
	public static AllelesHolder getPhenoType(MutableDataComponentHolder stack) {
		GenomeComponent component=stack.get(Components.GENOME);
		if(component!=null) {
			return getPhenoType(component);
		}
		return Genome.DEFAULT;
	}
	public static int getLifespanTicks(AllelesHolder allele) {
		return (int) (allele.getAllele(Genes.LIFESPAN).getNumber()*BeecrasyConfig.SERVER.LIFESPAN.getAsInt());
	}
}
