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

import java.util.BitSet;
import java.util.function.LongSupplier;

/**
 * 冯·诺依曼去偏提取器。
 * <p>
 * 将可能带有偏差的随机比特流转换为无偏的随机布尔值。
 * 核心算法：连续读取两个比特，若不同则返回第一个比特，否则丢弃并重试。
 */
public class VonNeumannExtractor {
	/** 底层随机数提供者。 */
	LongSupplier random;
	/** 比特缓冲区。 */
	BitSet bits;
	/** 当前读取位置索引。 */
	int index;
	/** 用于从 {@link LongSupplier} 读取单个long的临时数组。 */
	long[] num=new long[1];
	/**
	 * 构造一个冯·诺依曼去偏提取器。
	 *
	 * @param random 底层随机比特提供者
	 */
	public VonNeumannExtractor(LongSupplier random) {
		this.random=random;
	}
	/**
	 * 返回经过去偏处理的无偏随机布尔值。
	 * <p>
	 * 使用冯·诺依曼去偏算法：连续读取两个比特，
	 * 如果它们不同则返回第一个比特，否则丢弃这对比特并重试。
	 *
	 * @return 无偏的随机布尔值
	 */
	public boolean nextBoolean() {
		
		while(true) {
			boolean first=nextBit();
			boolean second=nextBit();
			if(first^second) {
				return first;
			}
		}
	}
	
	/**
	 * 读取下一个原始比特。
	 * <p>
	 * 如果比特缓冲区不足则自动补充，然后返回当前位置的比特值。
	 *
	 * @return 原始比特值
	 */
	private boolean nextBit() {
		refillBitsetIfNeeded();
		return bits.get(index++);
	}
	/**
	 * 按需补充比特缓冲区。
	 * <p>
	 * 如果比特缓冲区为 {@code null} 或当前索引已超出缓冲区长度，
	 * 则重新填充缓冲区。
	 */
	public void refillBitsetIfNeeded() {
		if(bits==null||index>=bits.length())
			refillBitset();
	}
	/**
	 * 从随机源读取一个新的 long 值来填充比特缓冲区。
	 * <p>
	 * 将 {@link #random} 提供的 long 值转换为 {@link BitSet}，
	 * 并将读取索引重置为0。
	 */
	public void refillBitset() {
		num[0]=random.getAsLong();
		bits=BitSet.valueOf(num);
		index=0;
	}
}
