package com.khjxiaogu.beecrasy.utils;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.NormalCraftingRecipe;

public class CraftingRecipeSequence {

	public static class SequencedRecipe {
		NormalCraftingRecipe recipe;
		List<Ingredient> recipeSequence;

		public SequencedRecipe(NormalCraftingRecipe recipe) {
			super();
			this.recipe = recipe;
			recipeSequence = new ArrayList<>(recipe.placementInfo().ingredients());

		}

		public int length() {
			return recipeSequence.size();
		}

		public Ingredient get(int idx) {
			return recipeSequence.get(idx);
		}
	}

	/** 树节点 */
	private static class Node {
		/** 当该节点只对应一个模式时，直接保存该模式，不再向下拆分 */
		SequencedRecipe singlePattern;
		/** 分支子节点，使用 IdentityHashMap 以确保键的比较使用 == */
		Map<Item, Node> children;
		/** 分支节点下，恰好在当前深度结束的模式列表 */
		List<SequencedRecipe> endingPatterns;
	}

	private final Node root;

	/**
	 * 根据提供的模式数组构建匹配树。
	 *
	 * @param patterns 三维数组，第一维为模式索引，第二维为序列位置，第三维为该位置允许的值
	 */
	public CraftingRecipeSequence(List<SequencedRecipe> patterns) {
		this.root = new Node();
		for (SequencedRecipe pattern : patterns) {
			insert(root, pattern, 0);
		}
	}

	/**
	 * 将模式插入树中（递归实现）。
	 */
	private void insert(Node node, SequencedRecipe pattern, int depth) {
		// 1. 当前节点是单模式节点
		if (node.singlePattern != null) {
			// 如果就是同一个模式，无需重复插入
			if (node.singlePattern == pattern) {
				return;
			}
			// 否则需要展开该节点：取出原有模式，转为分支节点，然后重新插入两个模式
			SequencedRecipe oldPattern = node.singlePattern;
			node.singlePattern = null;
			node.children = new IdentityHashMap<>();
			insert(node, oldPattern, depth);
			insert(node, pattern, depth);
			return;
		}

		// 2. 当前节点已经是分支节点
		if (node.children != null) {
			if (depth == pattern.length()) { // 模式在此结束
				if (node.endingPatterns == null) {
					node.endingPatterns = new ArrayList<>();
				}
				node.endingPatterns.add(pattern);
				return;
			}
			// 对当前深度允许的每个值，递归插入
			Ingredient allowed = pattern.get(depth);
			for (Holder<Item> val : allowed.getValues()) {
				Node child = node.children.get(val.value());
				if (child == null) {
					child = new Node();
					node.children.put(val.value(), child);
				}
				insert(child, pattern, depth + 1);
			}
			return;
		}

		// 3. 空节点：第一次有模式到达此处，不拆分，直接保留完整模式
		node.singlePattern = pattern;
	}

	/**
	 * 对输入序列进行匹配，返回所有匹配的模式列表。
	 *
	 * @param sequence 输入序列
	 * @return 所有匹配的模式（无特定顺序）
	 */
	public List<SequencedRecipe> match(List<ItemStack> sequence) {
		// 使用 Set 自动去重（当模式在树中有多条路径时可能出现重复引用，实际上不会）
		Set<SequencedRecipe> results = new LinkedHashSet<>();
		matchRecursive(root, sequence, 0, results);
		return new ArrayList<>(results);
	}

	/**
	 * 递归匹配。
	 */
	private void matchRecursive(Node node, List<ItemStack> sequence, int index, Set<SequencedRecipe> results) {
		// 情形 A：单模式节点 -> 检查剩余序列是否完全匹配该模式
		if (node.singlePattern != null) {
			SequencedRecipe pattern = node.singlePattern;
			// 长度必须一致
			if (pattern.length() != sequence.size()) {
				return;
			}
			// 检查从 index 开始的所有位置
			for (int i = index; i < pattern.length(); i++) {
				ItemStack seqItem = sequence.get(i);
				Ingredient allowed = pattern.get(i);

				if (allowed.test(seqItem)) {
					return;
				}
			}
			results.add(pattern);
			return;
		}

		// 情形 B：分支节点
		// 序列已耗尽，收集所有在此节点结束的模式
		if (index == sequence.size()) {
			if (node.endingPatterns != null) {
				results.addAll(node.endingPatterns);
			}
			return;
		}

		// 序列还有元素，沿对应的子树继续匹配
		if (node.children != null) {
			ItemStack item = sequence.get(index);
			Node child = node.children.get(item.getItem()); // IdentityHashMap 使用 == 比较
			if (child != null) {
				matchRecursive(child, sequence, index + 1, results);
			}
		}
	}

}
