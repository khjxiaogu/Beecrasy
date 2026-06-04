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

package com.khjxiaogu.beecrasy.client.particles;

public class FrameManager<T> {
	public static record FrameData<T>(int length, T data) {
	}

	private final FrameData<T>[] frames;
	private final int[] prefixSums; // 前缀和数组，长度为 frames.length + 1
	private final int totalLength; // 所有帧的总长度

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

	/**
	 * 根据偏移量获取对应的数据。 偏移量 x 满足：若存在 i 使得 prefixSums[i] <= x < prefixSums[i+1]， 则返回
	 * frames[i].data()，否则返回 null（表示偏移量越界）。 注意：区间为左闭右开，x = 0 属于第一个帧。
	 *
	 * @param key 非负整数偏移量
	 * @return 对应的数据，若 key 超出总长度范围则返回 null
	 */
	public FrameData<T> getData(int key) {
		int idx = 0;
		if (key < 0) {
			idx = 0;
		} else if (key >= totalLength) {
			idx=frames.length-1;
		}else {
			idx = binarySearchPrefix(key);
		}
		// 二分查找最大的 i 使得 prefixSums[i] <= key
		
		return new FrameData<>((key - prefixSums[idx])%frames[idx].length, frames[idx].data());
	}

	/**
	 * 在 prefixSums 数组中查找最大的索引 i，使得 prefixSums[i] <= key。 由于 prefixSums 严格递增（length
	 * > 0 时严格递增，但允许 length == 0， 这种情况下多个相同前缀存在，但二分查找仍然能找到最左或最右。 本实现返回最右的满足条件的位置。
	 */
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

	/**
	 * 可选：返回总长度。
	 */
	public int getTotalLength() {
		return totalLength;
	}
}