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

package com.khjxiaogu.beecrasy.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.utils.CraftingRecipeSequence.SequencedRecipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class CraftingSequenceMatcher {
	public static CraftingRecipeSequence unordered;
	//每个任务的建议数量
	private static final float TASK_EFFORT=300;
	public static void bake(Collection<RecipeHolder<CraftingRecipe>> recipes) {
		unordered=new CraftingRecipeSequence();
		Beecrasy.LOGGER.info("Filtering Recipe From "+recipes.size()+" Recipes");
		

		List<RecipeHolder<CraftingRecipe>> toindex=new ArrayList<>(recipes.size());
		outer:for(RecipeHolder<CraftingRecipe> recipe:recipes) {
			CraftingRecipe rcp=recipe.value();
			if(rcp instanceof ShapedRecipe sr&&rcp.getClass()==ShapedRecipe.class) {
				ItemStackTemplate result=sr.result;
				
				for(Optional<Ingredient> ig:sr.pattern.ingredients()) {
					if(ig.isPresent()) {
						Ingredient igd=ig.get();
						//仅支持原版无NBT原料，不支持自定义配方
						if(!isValidIngredient(igd))
							continue outer;
						//避免循环配方，粗略判断包含相同物品即可排除
						if(igd.acceptsItem(result.item()))
							continue outer;
					}
				}
				toindex.add(recipe);
			}else if(rcp instanceof ShapelessRecipe sr&&rcp.getClass()==ShapelessRecipe.class) {
				ItemStackTemplate result=sr.result;
				
				for(Ingredient igd:sr.ingredients) {
					//仅支持原版无NBT原料，不支持自定义配方
					if(!isValidIngredient(igd))
						continue outer;
					//避免循环配方，粗略判断包含相同物品即可排除
					if(igd.acceptsItem(result.item()))
						continue outer;
				
				}
				toindex.add(recipe);
			}
		}
		List<List<RecipeHolder<CraftingRecipe>>> ltasks=Utils.splitTasks(toindex,TASK_EFFORT);
		Beecrasy.LOGGER.info("Filtered "+toindex.size()+" Recipes, creating "+ltasks.size()+" tasks.");
		List<CompletableFuture<List<SequencedRecipe>>> futures=new ArrayList<>(ltasks.size());
		for(List<RecipeHolder<CraftingRecipe>> task:ltasks) {
			futures.add(CompletableFuture.completedFuture(task)
				.thenApplyAsync(CraftingSequenceMatcher::createSequencedRecipe));
			
		}
		Beecrasy.LOGGER.info("Awaiting task completion.");
		try {
			CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
			throw new RuntimeException("Interrupted when baking recipe",e);
		} catch (ExecutionException e) {
			e.printStackTrace();
			throw new RuntimeException("Error when baking recipe",e);
		}

		Beecrasy.LOGGER.info("All Recipe proceded, begin optimizing.");
		unordered.bake();
		Beecrasy.LOGGER.info("Baked "+unordered.size()+" recipe.");
		Beecrasy.LOGGER.info("Recipe Baking Complete.");
	}
	public static List<SequencedRecipe> createSequencedRecipe(List<RecipeHolder<CraftingRecipe>> param){
		for(RecipeHolder<CraftingRecipe> recipe:param) {
			SequencedRecipe seqr=new SequencedRecipe(recipe);
			unordered.insert(seqr);
			seqr.optimize();
		}
		return List.of();
	}
	public static boolean isValidIngredient(Ingredient igd) {
		return igd.isSimple()&&!igd.isCustom();
	}
	public static Collection<SequencedRecipe> match(List<ItemStack> matcher) {
		if(unordered==null)
			return Set.of();
		List<ItemStack> sorted=new ArrayList<>(matcher);
		sorted.sort(Comparator.comparingInt(t->t.getItem().hashCode()));
		return unordered.match(sorted);
	};
}
