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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;

/**
 * 数学和集合相关的通用工具方法。
 * <p>
 * 提供随机选取、随机率计算、二分图匹配以及共同元素计数等功能。
 */
public final class BeecrasyMath {
	/**
	 * 从集合中按均匀分布随机选取一个元素。
	 *
	 * @param <T>    元素类型
	 * @param set    待选取的集合
	 * @param random 随机数生成器
	 * @return 随机选取的元素；如果集合为 {@code null} 或为空则返回 {@code null}
	 */
	public static <T> T getRandomElement(Collection<T> set, RandomSource random) {
		if (set == null || set.isEmpty()) {
			return null;
		}
		int index = random.nextInt(set.size());
		Iterator<T> iterator = set.iterator();
		for (int i = 0; i < index; i++) {
			iterator.next();
		}
		return iterator.next();
	}

	/**
	 * 按给定速率返回整数次成功次数。
	 * <p>
	 * 将速率拆分为整数部分和小数部分，整数部分直接计入，
	 * 小数部分按概率（random.nextFloat() &lt; 小数部分）决定是否额外加一。
	 *
	 * @param rate   速率值（含小数部分概率）
	 * @param random 随机数生成器
	 * @return 计算后的整数成功次数
	 */
	public static int getRandomRate(float rate, RandomSource random) {
		int count=Mth.floor(rate);
		float frac=Mth.frac(rate);
		if(frac<=1E-7)
			return count;
		if(random.nextFloat()<frac)
			count++;
		return count;
	}

	/**
	 * 利用二分图最大匹配（匈牙利算法/DFS增广）判断对象列表能否被谓词列表一一匹配。
	 * <p>
	 * 将谓词视为左侧节点，对象视为右侧节点，若某谓词与某对象匹配则存在边，
	 * 使用DFS增广路径算法判断是否存在完美匹配。
	 *
	 * @param <T>       元素类型
	 * @param objects   对象列表
	 * @param predicates 谓词列表
	 * @return 如果每个谓词都能匹配到唯一的对象则返回 {@code true}
	 */
	public static <T> boolean canMatch(List<? extends T> objects, List<? extends Predicate<T>> predicates) {
		if (objects == null || predicates == null) {
			return false;
		}
		int n = objects.size();
		int m = predicates.size();
		if (n != m) {
			return false;
		}
		if (n == 0) {
			return true;
		}
		@SuppressWarnings("unchecked")
		List<Integer>[] adj = new List[n];
		for (int i = 0; i < n; i++) {
			adj[i] = new ArrayList<>();
			Predicate<T> pred = predicates.get(i);
			for (int j = 0; j < n; j++) {
				if (pred.test(objects.get(j))) {
					adj[i].add(j);
				}
			}
		}

		// matchR[j] 表示当前匹配给对象 j 的谓词索引，-1 表示未匹配
		int[] matchR = new int[n];
		Arrays.fill(matchR, -1);

		// 对每个谓词尝试增广
		for (int u = 0; u < n; u++) {
			boolean[] visited = new boolean[n];
			if (!dfs(u, adj, visited, matchR)) {
				// 某个左侧节点无法匹配，直接失败
				return false;
			}
		}
		// 所有节点都匹配成功
		return true;
	}

	/**
	 * 匈牙利算法中的DFS增广辅助方法。
	 *
	 * @param u       当前尝试匹配的谓词索引
	 * @param adj     邻接表
	 * @param visited 记录对象是否已被访问
	 * @param matchR  matchR[v] 表示当前匹配给对象 v 的谓词索引，-1 表示未匹配
	 * @return 如果找到增广路径则返回 {@code true}
	 */
	private static boolean dfs(int u, List<Integer>[] adj, boolean[] visited, int[] matchR) {
		for (int v : adj[u]) {
			if (!visited[v]) {
				visited[v] = true;
				// 如果对象 v 未匹配，或者能为已匹配它的谓词找到新的对象，则匹配成功
				if (matchR[v] == -1 || dfs(matchR[v], adj, visited, matchR)) {
					matchR[v] = u;
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 统计两个列表中共同元素的数量（考虑频次去重）。
	 * <p>
	 * 使用 HashMap 统计 list1 中各元素出现次数，再遍历 list2 逐一匹配，
	 * 每匹配一个则将频次减一，确保重复元素被正确计数。
	 *
	 * @param <T>   元素类型
	 * @param list1 第一个列表
	 * @param list2 第二个列表
	 * @return 共同元素的数量
	 */
	public static <T> int countCommonElements(List<T> list1, List<T> list2) {
		if (list1 == null || list2 == null) {
			return 0;
		}

		// 使用 HashMap 统计 list1 中每个元素的出现次数
		Map<T, Integer> freqMap = new HashMap<>();
		for (T element : list1) {
			freqMap.put(element, freqMap.getOrDefault(element, 0) + 1);
		}

		int commonCount = 0;
		// 遍历 list2，匹配可用的元素
		for (T element : list2) {
			Integer count = freqMap.get(element);
			if (count != null && count > 0) {
				commonCount++;
				freqMap.put(element, count - 1);
			}
		}
		return commonCount;
	}
	public static float[] notes=Util.make(()->{
		float[] values=new float[256];
		for(int i=0;i<values.length;i++) {
			values[i]=(float) Math.pow(2, ((i-60)/12f));
		}
		return values;
	});
	public static float noteToPitch(int note) {
		return notes[Objects.checkIndex(note,256)];
	}
}
