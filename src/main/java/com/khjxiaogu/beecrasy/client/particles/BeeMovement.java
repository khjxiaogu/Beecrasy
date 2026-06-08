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

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;


/**
 * 蜜蜂运动类型枚举。
 * <p>
 * 定义了蜜蜂粒子支持的 5 种运动模式：
 * <ul>
 *   <li>{@link #RANDOM}——随机漫步，每 0.25 秒施加高斯扰动；</li>
 *   <li>{@link #FIGURE_8_X}/{@link #FIGURE_8_Z}——沿 X/Z 轴的 8 字舞；</li>
 *   <li>{@link #CIRCLE_X}/{@link #CIRCLE_Z}——沿 X/Z 轴的圆形舞。</li>
 * </ul>
 * 每种模式对应 {@link BeeDanceSimulator#MOVEMENTS} 中的速度计算函数。
 * <p>
 * 提供 {@link #CODEC}（大小写不敏感的字符串编解码器）和 {@link #STREAM_CODEC}
 * （基于序数的网络传输编解码器）。
 */
public enum BeeMovement{
	/** 随机漫步模式 */
	RANDOM,
	/** 沿 X 轴的 8 字舞模式 */
	FIGURE_8_X,
	/** 沿 Z 轴的 8 字舞模式 */
	FIGURE_8_Z,
	/** 沿 X 轴的圆形舞模式 */
	CIRCLE_X,
	/** 沿 Z 轴的圆形舞模式 */
	CIRCLE_Z;

	/**
	 * 字符串编解码器。
	 * <p>
	 * 大小写不敏感地将字符串映射到枚举常量。
	 * 编码时统一转为小写，解码时尝试大写匹配。
	 */
	public static final Codec<BeeMovement> CODEC=Codec.STRING.comapFlatMap(
		t->{
			try {
				return DataResult.success(BeeMovement.valueOf(t.toUpperCase()));
			}catch(IllegalArgumentException err) {
				return DataResult.error(()->"No movement '"+t+"'");
			}
		}, t->t.name().toLowerCase()
		);
	/**
	 * 网络传输流编解码器。
	 * <p>
	 * 基于枚举序数（{@link #ordinal}）进行序列化/反序列化，
	 * 使用变长整数（VAR_INT）编码以节省空间。
	 */
	public static final StreamCodec<ByteBuf,BeeMovement> STREAM_CODEC=ByteBufCodecs.VAR_INT.map(o->BeeMovement.values()[o],BeeMovement::ordinal);
	private BeeMovement() {
	}
}
