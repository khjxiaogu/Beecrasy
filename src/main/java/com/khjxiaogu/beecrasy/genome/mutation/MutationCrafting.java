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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterSet;
import com.khjxiaogu.beecrasy.genome.DiploidGenome;
import com.khjxiaogu.beecrasy.genome.Genes;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.Mutation;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;
import com.khjxiaogu.beecrasy.utils.CraftingRecipeSequence.SequencedRecipe;
import com.khjxiaogu.beecrasy.utils.CraftingSequenceMatcher;
import com.khjxiaogu.beecrasy.utils.Utils;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * 合成突变，当蜜蜂具有合成生境时，尝试将产品序列中的物品通过工作台合成配方合并为新物品。
 */
public class MutationCrafting implements Mutation{

	public MutationCrafting() {
	}

	@Override
	public boolean mutate(BeeHiveParameterSet params,DiploidGenome genome, RandomSource rnd) {
		boolean succeed=false;
		boolean flag1=genome.maternal().get(Genes.BIOTOPE)==Genes.Alleles.CRAFT;
		boolean flag2=genome.paternal().get(Genes.BIOTOPE)==Genes.Alleles.CRAFT;
		
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
	/**
	 * 在服务器配方系统中查找匹配的合成配方并执行。
	 *
	 * @param genome 基因组建构器
	 * @param random 随机数生成器
	 * @return 如果成功合成了新物品则返回 {@code true}
	 */
	public static boolean handleCraft(Genome.Builder genome, RandomSource random) {
		List<ProductItem> products=genome.get(Genes.PRODUCTS);
		List<ItemStack> pending=new ArrayList<>(products.size());
		for(ProductItem product:products) {
			pending.add(product.stack().create());
		}
		List<RecipeHolder<CraftingRecipe>> seq=getRecipeSequence(pending);
		if(!seq.isEmpty()) {
			
			RecipeHolder<CraftingRecipe> selected=seq.get(random.nextInt(seq.size()));
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
	/**
	 * 使用 {@link CraftingSequenceMatcher} 查找配方，优先精确顺序匹配，否则返回所有可能的配方。
	 *
	 * @param products 物品列表
	 * @return 匹配的配方列表
	 */
	public static List<RecipeHolder<CraftingRecipe>> getRecipeSequence(List<ItemStack> products){
		Collection<SequencedRecipe> sequence=CraftingSequenceMatcher.match(products);
		if(sequence.isEmpty())
			return Collections.emptyList();
		List<RecipeHolder<CraftingRecipe>> orderedMatch=new ArrayList<>();
		outer:for(SequencedRecipe rh:sequence) {
			for(int i=0;i<products.size();i++) {
				if(!rh.match(i, products.get(i)))
					continue outer;
			}
			orderedMatch.add(rh.getRecipe());
		}
		if(!orderedMatch.isEmpty())
			return orderedMatch;
		return sequence.stream().map(SequencedRecipe::getRecipe).toList();
	}

	@Override
	public float getChance(BeeHiveParameterSet params,DiploidGenome genome) {
		return .05f;
	}

	@Override
	public boolean isApplicable(BeeHiveParameterSet params, DiploidGenome genome) {
		boolean flag1=genome.maternal().get(Genes.BIOTOPE)==Genes.Alleles.CRAFT;
		boolean flag2=genome.paternal().get(Genes.BIOTOPE)==Genes.Alleles.CRAFT;
		return flag1||flag2;
	}
}
