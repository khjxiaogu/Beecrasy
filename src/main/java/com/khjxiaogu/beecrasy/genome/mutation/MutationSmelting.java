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

import java.util.List;
import java.util.Optional;

import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterSet;
import com.khjxiaogu.beecrasy.genome.DiploidGenome;
import com.khjxiaogu.beecrasy.genome.Genes;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.Mutation;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;

/**
 * 熔炼突变，当蜜蜂具有熔炼生境时，尝试将产品序列末尾物品通过熔炉配方冶炼为新物品。
 */
public class MutationSmelting implements Mutation {

	@Override
	public boolean mutate(BeeHiveParameterSet params, DiploidGenome genome, RandomSource rnd) {
		
		boolean succeed=false;
		boolean flag1=genome.maternal().get(Genes.BIOTOPE)==Genes.Alleles.SMELT;
		boolean flag2=genome.paternal().get(Genes.BIOTOPE)==Genes.Alleles.SMELT;
		if(!flag1&&!flag2)return false;
		
		if(flag1&&flag2) {
			int r=rnd.nextInt(8);
			if (r < 3) {
				succeed|=handleCraft(params,genome.maternal(),rnd);
            } else if (r < 6) {
            	succeed|=handleCraft(params,genome.paternal(),rnd);
            } else {
				succeed|=handleCraft(params,genome.maternal(),rnd);
				succeed|=handleCraft(params,genome.paternal(),rnd);
            }
			return succeed;
		}else if(flag1) {
			return handleCraft(params,genome.maternal(),rnd);
		}else if(flag2) {
			return handleCraft(params,genome.paternal(),rnd);
		}
		return false;
	}
	/**
	 * 查询熔炉配方并执行熔炼。
	 *
	 * @param param  蜂箱参数集合
	 * @param genome 基因组建构器
	 * @param random 随机数生成器
	 * @return 如果成功冶炼了新物品则返回 {@code true}
	 */
	@SuppressWarnings("resource")
	public static boolean handleCraft(BeeHiveParameterSet param,Genome.Builder genome, RandomSource random) {
		List<ProductItem> products=genome.get(Genes.PRODUCTS);
		SingleRecipeInput sri=new SingleRecipeInput(products.get(products.size()-1).stack().create());
		Optional<RecipeHolder<SmeltingRecipe>> recipe=param.level().recipeAccess().getRecipeFor(RecipeType.SMELTING, sri, param.level());
		if(!recipe.isEmpty()) {
			RecipeHolder<SmeltingRecipe> selected=recipe.get();
			ItemStack ist=selected.value().assemble(sri);
			if(ist!=null&&!ist.isEmpty()) {
				genome.add(Genes.PRODUCTS, List.of(new ProductItem(Genes.Alleles.CRAFT,Optional.of(selected.id().identifier()),new ItemStackTemplate(ist.getItem(),ist.getCount(),ist.getComponentsPatch()))));
				return true;
			}
			
		}
		return false;
	}
	@Override
	public float getChance(BeeHiveParameterSet params,DiploidGenome genome) {
		return .05f;
	}
	@Override
	public boolean isApplicable(BeeHiveParameterSet params, DiploidGenome genome) {
		boolean flag1=genome.maternal().get(Genes.BIOTOPE)==Genes.Alleles.SMELT;
		boolean flag2=genome.paternal().get(Genes.BIOTOPE)==Genes.Alleles.SMELT;
		return (flag1||flag2)&&params.hasBiotope(Genes.Alleles.SMELT);
	}
}
