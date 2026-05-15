package com.khjxiaogu.beecrasy.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.NormalCraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class CraftingRecipeSequence {

	public static class SequencedRecipe {
		RecipeHolder<CraftingRecipe> recipe;
		List<Ingredient> recipeSequence;

		protected SequencedRecipe(RecipeHolder<CraftingRecipe> recipe, List<Ingredient> recipeSequence) {
			super();
			this.recipe = recipe;
			this.recipeSequence = recipeSequence;
		}

		public RecipeHolder<CraftingRecipe> getRecipe() {
			return recipe;
		}

		public SequencedRecipe(RecipeHolder<CraftingRecipe> recipe) {
			super();
			this.recipe = recipe;
			recipeSequence = new ArrayList<>(recipe.value().placementInfo().ingredients());

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
	    public static Set<List<Item>> cartesianProduct(List<Ingredient> sets) {
	        List<List<Item>> result = new ArrayList<>();
	        result.add(new ArrayList<>());

	        for (Ingredient set : sets) {
	            List<List<Item>> newResult = new ArrayList<>();
	            for (List<Item> combination : result) {
	                for (Holder<Item> item : set.getValues()) {
	                    List<Item> newComb = new ArrayList<>(combination);
	                    newComb.add(item.value());
	                    newResult.add(newComb);
	                }
	            }
	            result = newResult;
	        }
	        for (List<Item> combination : result) {
	            combination.sort(Comparator.comparingInt(Item::hashCode));
	        }

	        return new HashSet<>(result);
	    }
		public List<UnOrderedSequencedRecipe> toUnordered() {
			Set<List<Item>> li=cartesianProduct(recipeSequence);
			List<UnOrderedSequencedRecipe> lo=new ArrayList<>(li.size());
			for(List<Item> i:li) {
				lo.add(new UnOrderedSequencedRecipe(recipe,recipeSequence,i));
			}
			return lo;
		}
	}
	public static class UnOrderedSequencedRecipe extends SequencedRecipe{
		List<Item> specificItem;

	

		public UnOrderedSequencedRecipe(RecipeHolder<CraftingRecipe> recipe, List<Ingredient> recipeSequence, List<Item> specificItem) {
			super(recipe, recipeSequence);
			this.specificItem = specificItem;
		}

		public Stream<Item> getValues(int idx){
			return Stream.of(specificItem.get(idx));
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
