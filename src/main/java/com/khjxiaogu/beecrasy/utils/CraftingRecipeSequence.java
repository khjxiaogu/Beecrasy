/** 
* Copyright (c) 2026 khjxiaogu
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
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

		public int length() {
			return recipeSequence.size();
		}

		public boolean match(int idx,ItemStack stack) {
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
	public static class UnordererRecipeSequence{
		
		Map<Pair<Item,Item>, List<SequencedRecipe>> unorderedIndex=new HashMap<>(BuiltInRegistries.ITEM.size()*BuiltInRegistries.ITEM.size());
		private final List<SequencedRecipe> shortUnordered = new ArrayList<>(10);
	    // 无序配方插入：基于所有可能的（排序后）前两个物品建立索引
	    public void insert(SequencedRecipe pattern) {
	        List<Ingredient> ingredients = pattern.recipeSequence;
	        int len = ingredients.size();
	        if (len < 2) {
	            shortUnordered.add(pattern);
	            return;
	        }
	        for (Pair<Item, Item> pair : pattern.cartesianProduct) {
	            unorderedIndex.computeIfAbsent(pair, _ -> new ArrayList<>()).add(pattern);
	        }
	    }
	    public void insertAll(List<? extends SequencedRecipe> patterns) {
			for (SequencedRecipe pattern : patterns) {
				insert(pattern);
			}
		}
	    public Collection<RecipeHolder<CraftingRecipe>> match(List<ItemStack> sequence) {
	        Set<RecipeHolder<CraftingRecipe>> results = new LinkedHashSet<>();
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
	                            results.add(pattern.recipe);
	                        }
	                    }
	                }
	            }else {
		            for (SequencedRecipe pattern : shortUnordered) {
		                if (pattern.matches(sequence)) {
		                    results.add(pattern.recipe);
		                }
		            }
	            }
	        }
	        return results;
	    }
	}
	private static class Node {
		/** 当该节点只对应一个配方，不再向下拆分 */
		SequencedRecipe singlePattern;
		/** 分支子节点，使用 IdentityHashMap 以确保键的比较使用 == */
		Map<Item, Node> children;
		/** 分支节点下，恰好在当前深度结束的配方列表 */
		List<SequencedRecipe> endingPatterns;
	}

	private final Node root;

	/**
	 * 根据提供的配方构建匹配树。
	 *
	 */
	public CraftingRecipeSequence(List<SequencedRecipe> patterns) {
		this();
		insertAll(patterns);
	}
	public CraftingRecipeSequence() {
		this.root = new Node();
	}
	public void insertAll(List<? extends SequencedRecipe> patterns) {
		for (SequencedRecipe pattern : patterns) {
			insert(pattern);
		}
	}
	public void insert(SequencedRecipe pattern) {
		insert(root, pattern, 0);
	}
	/**
	 * 将配方插入树中（递归实现）。
	 */
	private void insert(Node node, SequencedRecipe pattern, int depth) {
		// 1. 当前节点是单节点
		if (node.singlePattern != null) {
			if (node.singlePattern == pattern) {
				return;
			}
			SequencedRecipe oldPattern = node.singlePattern;
			node.singlePattern = null;
			node.children = new IdentityHashMap<>();
			insert(node, oldPattern, depth);
			insert(node, pattern, depth);
			return;
		}

		// 2. 当前节点已经是分支节点
		if (node.children != null) {
			if (depth == pattern.length()) {
				if (node.endingPatterns == null) {
					node.endingPatterns = new ArrayList<>();
				}
				node.endingPatterns.add(pattern);
				return;
			}
			Stream<Item> allowed = pattern.getValues(depth);
			allowed.forEach(val->{
				Node child = node.children.get(val);
				if (child == null) {
					child = new Node();
					node.children.put(val, child);
				}
				insert(child, pattern, depth + 1);
			});
			return;
		}

		node.singlePattern = pattern;
	}

	/**
	 * 对输入序列进行匹配，返回所有匹配的配方列表。
	 *
	 * @param sequence 输入序列
	 * @return 所有匹配的配方（无特定顺序）
	 */
	public Collection<RecipeHolder<CraftingRecipe>> match(List<ItemStack> sequence) {
		Set<RecipeHolder<CraftingRecipe>> results = new LinkedHashSet<>();
		matchRecursive(root, sequence, 0, results);
		return results;
	}

	/**
	 * 递归匹配。
	 */
	private void matchRecursive(Node node, List<ItemStack> sequence, int index, Set<RecipeHolder<CraftingRecipe>> results) {
		if (node.singlePattern != null) {
			SequencedRecipe pattern = node.singlePattern;
			if (pattern.length() != sequence.size()) {
				return;
			}
			for (int i = 0; i < pattern.length(); i++) {
				ItemStack seqItem = sequence.get(i);

				if (!pattern.match(i,seqItem)) {
					return;
				}
			}
			results.add(pattern.recipe);
			return;
		}
		if (index == sequence.size()) {
			if (node.endingPatterns != null) {
				outer:for(SequencedRecipe recipe:node.endingPatterns) {
					for(int i=0;i<sequence.size();i++) {
						if (!recipe.match(i,sequence.get(i))) {
							 continue outer;
						}
					}
					results.add(recipe.recipe);
				}
			}
			return;
		}

		if (node.children != null) {
			ItemStack item = sequence.get(index);
			Node child = node.children.get(item.getItem()); 
			if (child != null) {
				matchRecursive(child, sequence, index + 1, results);
			}
		}
	}

}
