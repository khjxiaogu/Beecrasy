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
import java.util.function.Predicate;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class BeecrasyMath {
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

	public static int getRandomRate(float rate, RandomSource random) {
		int count=Mth.floor(rate);
		float frac=Mth.frac(rate);
		if(frac<=1E-7)
			return count;
		if(random.nextFloat()<frac)
			count++;
		return count;
	}

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

}
