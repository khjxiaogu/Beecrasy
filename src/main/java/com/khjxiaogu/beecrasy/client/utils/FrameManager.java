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

/**
 * 帧序列管理器。
 * <p>
 * 管理一组按顺序执行的 {@link FrameData}，每个帧有固定持续时间（tick）和
 * 关联的 {@link AnimateFunction}。通过前缀和数组实现 O(log n) 的帧查找，
 * 将目标对象的年龄（age）映射到正确的帧和帧内时间，并驱动对应的动画函数。
 * 当年龄超过总长度时，会自动锁定到最后一帧。
 *
 * @param <T> 被动画驱动的目标对象类型
 */
public class FrameManager<T> {
	/**
	 * 帧数据记录。
	 *
	 * @param length   帧持续时间（tick）
	 * @param animator 帧对应的动画函数，每 tick 以帧内时间（秒）驱动目标对象
	 * @param <T>      被动画驱动的目标对象类型
	 */
	public static record FrameData<T>(int length, AnimateFunction<T> animator) {
	}

	/** 所有帧的数组 */
	private final FrameData<T>[] frames;
	/**
	 * 前缀和数组，长度为 {@code frames.length + 1}。
	 * {@code prefixSums[i]} 表示前 i 个帧的总长度，
	 * 用于二分查找将年龄映射到正确的帧索引。
	 */
	private final int[] prefixSums;
	/** 所有帧的总长度（tick） */
	private final int totalLength;
	/**
	 * 从列表构造帧管理器。
	 *
	 * @param frames 帧数据列表
	 */
	@SuppressWarnings("unchecked")
	public FrameManager(List<FrameData<T>> frames) {
		this(frames.toArray(FrameData[]::new));
	}

	/**
	 * 从可变参数构造帧管理器。
	 * <p>
	 * 构建前缀和数组并计算所有帧的总长度。
	 *
	 * @param frames 帧数据数组
	 */
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
	 * 根据年龄更新目标对象的动画状态。
	 * <p>
	 * 通过前缀和二分查找将年龄映射到正确的帧，计算帧内的相对时间（秒），
	 * 并调用该帧的 {@link AnimateFunction}。
	 * <ul>
	 *   <li>年龄 &lt; 0：锁定到第 0 帧；</li>
	 *   <li>年龄 &ge; 总长度：锁定到最后一帧；</li>
	 *   <li>否则：二分查找定位当前帧。</li>
	 * </ul>
	 *
	 * @param age 当前年龄（tick）
	 * @param obj 被驱动的目标对象
	 */
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

	/**
	 * 在前缀和数组中二分查找 ≤ key 的最大索引。
	 * <p>
	 * 返回的索引 {@code hi} 即当前年龄所在的帧索引。
	 *
	 * @param key 查找的年龄值
	 * @return ≤ key 的最大前缀和索引
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
	 * 获取所有帧的总长度。
	 *
	 * @return 总 tick 长度
	 */
	public int getTotalLength() {
		return totalLength;
	}
}