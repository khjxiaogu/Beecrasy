package com.khjxiaogu.beecrasy.genome;

import java.util.List;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;

import net.neoforged.neoforge.common.MutableDataComponentHolder;


public final class GenomeDataHelper {
	private GenomeDataHelper() {
		
	}
	public void setHaploidGenome(MutableDataComponentHolder stack,Genome genome) {
		List<ProductItem> products=genome.getAllele(Genes.PRODUCTS);
		if(!products.isEmpty()) {
			stack.set(Components.TINT_STACK, products.get(0).stack().create());
		}
		stack.set(Components.GENOME, new GenomeComponent(false,genome));
	}
	public void setDiploidGenome(MutableDataComponentHolder stack,DiploidGenome genome) {
		List<ProductItem> products=genome.maternal().getAllele(Genes.PRODUCTS);
		if(!products.isEmpty()) {
			stack.set(Components.TINT_STACK, products.get(0).stack().create());
		}
		stack.set(Components.GENOME, new GenomeComponent(false,genome.maternal().build(),genome.paternal().build()));
	}
	public void setGenomeInspected(MutableDataComponentHolder stack) {
		GenomeComponent component=stack.get(Components.GENOME);
		if(component!=null) {
			stack.set(Components.GENOME, component.asInspected());
			
		}
	}
	public AllelesHolder getPhenoType(GenomeComponent component) {
		return component.getGenome(0);
	}
	public AllelesHolder getPhenoType(MutableDataComponentHolder stack) {
		GenomeComponent component=stack.get(Components.GENOME);
		if(component!=null) {
			return getPhenoType(component);
		}
		return Genome.DEFAULT;
	}
}
