package com.khjxiaogu.beecrasy.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

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
}
