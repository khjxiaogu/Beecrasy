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

/**
 * 无序配方序列索引，用于快速查找给定物品序列可匹配的合成配方。
 * <p>
 * 基于前两个物品的笛卡尔积建立索引，支持高效匹配查询。
 */
public class CraftingRecipeSequence {

	/**
	 * 带序列化原料列表的配方封装，支持与物品序列的匹配及笛卡尔积预计算。
	 */
	public static class SequencedRecipe {
		/** 配方持有者。 */
		RecipeHolder<CraftingRecipe> recipe;
		/** 配方原料列表（有序）。 */
		List<Ingredient> recipeSequence;
		/** 前两个原料的笛卡尔积集合（用于索引）。 */
		Set<Pair<Item,Item>> cartesianProduct;
		/**
		 * 构造一个带原料列表的SequencedRecipe。
		 * <p>
		 * 如果原料数≥2，还会预计算前两个原料的笛卡尔积。
		 *
		 * @param recipe         配方持有者
		 * @param recipeSequence 原料列表
		 */
		protected SequencedRecipe(RecipeHolder<CraftingRecipe> recipe, List<Ingredient> recipeSequence) {
			super();
			this.recipe = recipe;
			this.recipeSequence = recipeSequence;
			if(recipeSequence.size()>=2)
			this.cartesianProduct=SequencedRecipe.cartesianProduct(recipeSequence);
		}

		/**
		 * 获取配方持有者。
		 *
		 * @return 配方持有者
		 */
		public RecipeHolder<CraftingRecipe> getRecipe() {
			return recipe;
		}

		/**
		 * 从配方持有者构造SequencedRecipe，自动提取原料列表。
		 *
		 * @param recipe 配方持有者
		 */
		public SequencedRecipe(RecipeHolder<CraftingRecipe> recipe) {
			this(recipe,recipe.value().placementInfo().ingredients());

		}
		/**
		 * 释放笛卡尔积缓存以优化内存占用。
		 */
		public void optimize() {
			cartesianProduct=null;
		}
		/**
		 * 获取原料列表的长度。
		 *
		 * @return 原料数量
		 */
		public int length() {
			return recipeSequence.size();
		}

		/**
		 * 判断指定位置的原料是否与给定的物品栈匹配。
		 *
		 * @param idx   原料位置索引
		 * @param stack 物品栈
		 * @return 如果匹配则返回 {@code true}
		 */
		public boolean match(int idx,ItemStack stack) {
			if(idx>=length())
				return false;
			return recipeSequence.get(idx).test(stack);
		}
		/**
		 * 获取指定位置原料的所有可选项。
		 *
		 * @param idx 原料位置索引
		 * @return 可选的物品流
		 */
		public Stream<Item> getValues(int idx){
			return recipeSequence.get(idx).getValues().stream().map(t->t.value());
		}
		/**
		 * 计算多组原料的笛卡尔积，用于索引构建。
		 * <p>
		 * 以前两个原料的笛卡尔积为基础，逐步合并后续原料，
		 * 每步保留排序后的Pair以简化查找。
		 *
		 * @param sets 原料列表
		 * @return 以排序Pair表示的笛卡尔积集合
		 */
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

		/**
		 * 判断给定的物品序列是否能匹配此配方的所有原料。
		 *
		 * @param sequence 物品序列
		 * @return 如果配方能匹配则返回 {@code true}
		 */
		public boolean matches(List<ItemStack> sequence) {
			
			return BeecrasyMath.canMatch(sequence, this.recipeSequence);
		}
	}
	
	/** 基于前两个物品（排序后）的笛卡尔积索引。 */
	protected HashMap<Pair<Item,Item>, List<SequencedRecipe>> unorderedIndex=new HashMap<>(BuiltInRegistries.ITEM.size()*200);
	/** 原料数小于2的短配方列表。 */
	protected ArrayList<SequencedRecipe> shortUnordered = new ArrayList<>(2000);

	/**
	 * 构造一个空的配方序列索引。
	 */
	public CraftingRecipeSequence() {
	}
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
	/**
	 * 根据输入的物品序列查找匹配的配方集合。
	 * <p>
	 * 将输入序列按hashCode排序，取前两个物品作为索引键，
	 * 从索引中查找候选配方并逐一验证。短配方（原料&lt;2）单独处理。
	 *
	 * @param sequence 输入的物品序列
	 * @return 匹配的配方集合
	 */
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
	/**
	 * 批量插入配方。
	 *
	 * @param patterns 配方列表
	 */
	public void insertAll(List<? extends SequencedRecipe> patterns) {
		for (SequencedRecipe pattern : patterns) {
			insert(pattern);
		}
	}
	/**
	 * 返回索引中配方总条数。
	 *
	 * @return 配方总条数
	 */
	public long size() {
    	long tsize=shortUnordered.size();
    	for(List<SequencedRecipe> li:unorderedIndex.values()) {
    		tsize+=li.size();
    	}
    	
    	return tsize;
    }
	/**
	 * 固化索引结构，优化内存占用。
	 * <p>
	 * 将HashMap和ArrayList重新包装或trim，释放多余的容量。
	 */
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
