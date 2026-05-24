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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.khjxiaogu.beecrasy.genome.BeeHiveParameters;
import com.khjxiaogu.beecrasy.genome.DiploidGenome;
import com.khjxiaogu.beecrasy.genome.Genes;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.Mutation;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;
import com.khjxiaogu.beecrasy.utils.BeecrasyMath;
import com.khjxiaogu.beecrasy.utils.CraftingSequenceMatcher;
import com.khjxiaogu.beecrasy.utils.Utils;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class MutationCrafting implements Mutation{

	public MutationCrafting() {
	}

	@Override
	public boolean mutate(BeeHiveParameters params,DiploidGenome genome, RandomSource rnd) {
		boolean succeed=false;
		boolean flag1=genome.maternal().get(Genes.BIOTOPE)==Genes.Alleles.CRAFT;
		boolean flag2=genome.paternal().get(Genes.BIOTOPE)==Genes.Alleles.CRAFT;
		if(!flag1&&!flag2)return false;
		if(rnd.nextFloat()>.075f)return false;
		
		if(flag1&&flag2) {
			int r=rnd.nextInt(8);
			if (r < 3) {
				succeed|=handleCraft(genome.maternal(),rnd);
            } else if (r < 6) {
            	succeed|=handleCraft(genome.paternal(),rnd);
            } else {
				succeed|=handleCraft(genome.maternal(),rnd);
				succeed|=handleCraft(genome.paternal(),rnd);
            }
			return succeed;
		}else if(flag1) {
			return handleCraft(genome.maternal(),rnd);
		}else if(flag2) {
			return handleCraft(genome.paternal(),rnd);
		}
		return false;
	}
	public static boolean handleCraft(Genome.Builder genome, RandomSource random) {
		List<ProductItem> products=genome.get(Genes.PRODUCTS);
		List<ItemStack> pending=new ArrayList<>(products.size());
		for(ProductItem product:products) {
			pending.add(product.stack().create());
		}
		Collection<RecipeHolder<CraftingRecipe>> seq=getRecipeSequence(pending);
		if(!seq.isEmpty()) {
			
			RecipeHolder<CraftingRecipe> selected=BeecrasyMath.getRandomElement(seq, random);
			if(selected!=null) {
				ItemStackTemplate ist=Utils.getRecipeOutput(pending, selected.value());
				if(ist!=null) {
					genome.add(Genes.PRODUCTS, List.of(new ProductItem(Genes.Alleles.CRAFT,Optional.of(selected.id().identifier()),ist)));
					return true;
				}
			}
		}
		return false;
	}
	public static Collection<RecipeHolder<CraftingRecipe>> getRecipeSequence(List<ItemStack> products){
		
		
		Collection<RecipeHolder<CraftingRecipe>> sequence=CraftingSequenceMatcher.matchOrdered(products);
		if(sequence.isEmpty())
			sequence=CraftingSequenceMatcher.matchUnordered(products);
		return sequence;
	}
}
