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

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.BitRandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;

/**
 * 可序列化的随机数生成器，基于线性同余算法。
 * <p>
 * 实现 {@link BitRandomSource} 接口，支持种子的存取与派生新的随机源。
 * 核心算法：newSeed = (oldSeed * 25214903917L + 11L) &amp; 281474976710655L。
 */
public class SerializableRandomSource implements BitRandomSource {
    /** 当前种子值。 */
    private long seed;
    /**
     * 使用给定种子构造随机数生成器。
     *
     * @param seed 初始种子
     */
    public SerializableRandomSource(long seed) {
    	this.seed = seed;
    }
    /**
     * 创建一个可序列化的随机数生成器，使用标准LCG种子初始化。
     * <p>
     * 对种子进行与Java Random兼容的线性同余变换后构造实例。
     *
     * @param seed 初始种子
     * @return 新的随机数生成器实例
     */
    public static SerializableRandomSource create(long seed) {
    	return new SerializableRandomSource((seed ^ 25214903917L) & 281474976710655L);
    }
    /**
     * 从此随机源派生一个新的独立随机源。
     *
     * @return 新的 {@link SingleThreadedRandomSource} 实例
     */
    @Override
    public RandomSource fork() {
        return new SingleThreadedRandomSource(this.nextLong());
    }
    /**
     * 派生一个位置相关随机工厂。
     *
     * @return 新的 {@link LegacyRandomSource.LegacyPositionalRandomFactory} 实例
     */
    @Override
    public PositionalRandomFactory forkPositional() {
        return new LegacyRandomSource.LegacyPositionalRandomFactory(this.nextLong());
    }
    /**
     * 设置随机种子的值。
     *
     * @param seed 新的种子值
     */
    @Override
    public void setSeed(long seed) {
    	this.seed=seed;
    }
    /**
     * 获取当前种子值。
     *
     * @return 当前种子
     */
    public long getSeed() {
    	return seed;
    }
    /**
     * 生成指定比特位数的随机整数。
     * <p>
     * 使用线性同余算法生成下一随机数，返回高 {@code bits} 位。
     *
     * @param bits 随机比特位数（1-32）
     * @return 生成的随机整数
     */
    @Override
    public int next(int bits) {
        long newSeed = this.seed * 25214903917L + 11L & 281474976710655L;
        this.seed = newSeed;
        return (int)(newSeed >> 48 - bits);
    }
	/**
	 * 生成一个近似标准高斯分布的随机双精度值。
	 * <p>
	 * 使用三个均匀分布随机数的平均值来近似高斯分布。
	 *
	 * @return 近似标准高斯分布的随机值
	 */
	@Override
	public double nextGaussian() {
		double d=0;
		d+=nextDouble();
		d+=nextDouble();
		d+=nextDouble();
		return d/3d;
	}

}