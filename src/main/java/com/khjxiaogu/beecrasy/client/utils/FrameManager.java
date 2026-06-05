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

package com.khjxiaogu.beecrasy.client.utils;

import java.util.List;

public class FrameManager<T> {
	public static record FrameData<T>(int length, AnimateFunction<T> animator) {
	}

	private final FrameData<T>[] frames;
	private final int[] prefixSums; // 前缀和数组，长度为 frames.length + 1
	private final int totalLength; // 所有帧的总长度
	@SuppressWarnings("unchecked")
	public FrameManager(List<FrameData<T>> frames) {
		this(frames.toArray(FrameData[]::new));
	}
	@SafeVarargs
	public FrameManager(FrameData<T>... frames) {
		this.frames = frames;
		int n = this.frames.length;
		this.prefixSums = new int[n + 1];
		int sum = 0;
		for (int i = 0; i < n; i++) {
			sum += this.frames[i].length();
			this.prefixSums[i + 1] = sum;
		}
		this.totalLength = sum;
	}

	public void tick(int age,T obj) {
		int idx = 0;
		if (age < 0) {
			idx = 0;
		} else if (age >= totalLength) {
			idx=frames.length-1;
		}else {
			idx = binarySearchPrefix(age);
		}
		frames[idx].animator.tick(((age - prefixSums[idx])%frames[idx].length)/20f, obj);
	}

	private int binarySearchPrefix(int key) {
		int lo = 0, hi = prefixSums.length - 1;
		while (lo <= hi) {
			int mid = (lo + hi) >>> 1;
			if (prefixSums[mid] <= key) {
				lo = mid + 1;
			} else {
				hi = mid - 1;
			}
		}
		return hi;
	}

	public int getTotalLength() {
		return totalLength;
	}
}