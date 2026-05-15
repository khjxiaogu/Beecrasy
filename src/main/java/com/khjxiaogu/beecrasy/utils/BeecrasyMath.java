package com.khjxiaogu.beecrasy.utils;

import java.util.Collection;
import java.util.Iterator;

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
}
