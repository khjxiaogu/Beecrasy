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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

public class CraftingRecipeSequence {

	public static class SequencedRecipe {
		RecipeHolder<CraftingRecipe> recipe;
		List<Ingredient> recipeSequence;
		Set<Pair<Item,Item>> cartesianProduct;
		protected SequencedRecipe(RecipeHolder<CraftingRecipe> recipe, List<Ingredient> recipeSequence) {
			super();
			this.recipe = recipe;
			this.recipeSequence = recipeSequence;
			if(recipeSequence.size()>=2)
			this.cartesianProduct=SequencedRecipe.cartesianProduct(recipeSequence);
		}

		public RecipeHolder<CraftingRecipe> getRecipe() {
			return recipe;
		}

		public SequencedRecipe(RecipeHolder<CraftingRecipe> recipe) {
			this(recipe,recipe.value().placementInfo().ingredients());

		}
		public void optimize() {
			cartesianProduct=null;
		}
		public int length() {
			return recipeSequence.size();
		}

		public boolean match(int idx,ItemStack stack) {
			if(idx>=length())
				return false;
			return recipeSequence.get(idx).test(stack);
		}
		public Stream<Item> getValues(int idx){
			return recipeSequence.get(idx).getValues().stream().map(t->t.value());
		}
	    public static Set<Pair<Item,Item>> cartesianProduct(List<Ingredient> sets) {
	    	Set<Pair<Item,Item>> result = new HashSet<>(160000);
	        for (Holder<Item> i1 : sets.get(0).getValues()) {
	            for (Holder<Item> i2 : sets.get(1).getValues()) {
	            	Item e1=i1.value();
	            	Item e2=i2.value();
	            	if(e1.hashCode()<=e2.hashCode())
	            		result.add(Pair.of(e1, e2));
	            	else
	            		result.add(Pair.of(e2, e1));
	            }
	        }
	        if(sets.size()>2) {

	        	Set<Pair<Item,Item>> newResult = new HashSet<>(Math.max(result.size(), 160000));
	        	Set<Pair<Item,Item>> temp=null;
		        for (Ingredient set : sets) {
		            for (Pair<Item,Item> combination : result) {
		                for (Holder<Item> item : set.getValues()) {
		                	Item it=item.value();
		                	int hash=it.hashCode();
		                	Item elm1=combination.getFirst();
		                	if(elm1.hashCode()>hash) {
			                	Item elm0=combination.getSecond();
		                		if(elm0.hashCode()>hash) {
			                		newResult.add(Pair.of(it, elm0));
		                		}else {

			                		newResult.add(Pair.of(elm0,it));
		                		}
		                	}else {
		                		newResult.add(combination);
		                	}
		                }
		            }
		            temp = result;
		            result = newResult;
		            newResult = temp;
		            newResult.clear();
		        }
	        }
	        return result;
	    }

		public boolean matches(List<ItemStack> sequence) {
			
			return BeecrasyMath.canMatch(sequence, this.recipeSequence);
		}
	}
	
	protected HashMap<Pair<Item,Item>, List<SequencedRecipe>> unorderedIndex=new HashMap<>(BuiltInRegistries.ITEM.size()*200);
	protected ArrayList<SequencedRecipe> shortUnordered = new ArrayList<>(2000);

	public CraftingRecipeSequence() {
	}
    // 无序配方插入：基于所有可能的（排序后）前两个物品建立索引
    public void insert(SequencedRecipe pattern) {
        List<Ingredient> ingredients = pattern.recipeSequence;
        int len = ingredients.size();
        if (len < 2) {
        	synchronized(shortUnordered) {
        		shortUnordered.add(pattern);
        	}
            return;
        }
        synchronized(unorderedIndex) {
	        for (Pair<Item, Item> pair : pattern.cartesianProduct) {
	            unorderedIndex.computeIfAbsent(pair, _ -> new ArrayList<>(10)).add(pattern);
	        }
        }
    }
    public Collection<SequencedRecipe> match(List<ItemStack> sequence) {
        Set<SequencedRecipe> results = new LinkedHashSet<>();
        if (!sequence.isEmpty()) {
            // 对输入序列排序，取前两个物品作为索引键
            List<ItemStack> sorted = new ArrayList<>(sequence);
            sorted.sort(Comparator.comparingInt(t->t.getItem().hashCode()));
            if (sorted.size() >= 2) {
                Item a = sorted.get(0).getItem();
                Item b = sorted.get(1).getItem();
                Pair<Item, Item> key = Pair.of(a, b);
                List<SequencedRecipe> candidates = unorderedIndex.get(key);
                if (candidates != null) {
                    for (SequencedRecipe pattern : candidates) {
                        if (pattern.matches(sequence)) {
                            results.add(pattern);
                        }
                    }
                }
            }else {
	            for (SequencedRecipe pattern : shortUnordered) {
	                if (pattern.matches(sequence)) {
	                    results.add(pattern);
	                }
	            }
            }
        }
        return results;
    }
    public void insertAll(List<? extends SequencedRecipe> patterns) {
		for (SequencedRecipe pattern : patterns) {
			insert(pattern);
		}
	}
    public long size() {
    	long tsize=shortUnordered.size();
    	for(List<SequencedRecipe> li:unorderedIndex.values()) {
    		tsize+=li.size();
    	}
    	
    	return tsize;
    }
    public void bake() {
    	unorderedIndex=new HashMap<>(unorderedIndex);
    	shortUnordered.trimToSize();
    }

	/**
	 * 根据提供的配方构建匹配树。
	 *
	 */
	public CraftingRecipeSequence(List<SequencedRecipe> patterns) {
		this();
		insertAll(patterns);
	}

}
