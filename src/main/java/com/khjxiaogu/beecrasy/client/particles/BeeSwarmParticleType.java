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

import java.util.Optional;

import org.joml.Vector4fc;

import com.mojang.serialization.MapCodec;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * 蜜蜂粒子类型类。
 * <p>
 * 继承 {@link ParticleType}<{@link BeeParticleOption}>，持有预生成的默认随机粒子选项
 * （{@link #RANDOM}），并提供便捷构造方法创建指定运动序列的粒子选项。
 * 管理自身的 Codec 和 StreamCodec。
 */
public class BeeSwarmParticleType extends ParticleType<BeeSwarmParticleOption> {
	/** 此粒子类型对应的 MapCodec */
	private final MapCodec<BeeSwarmParticleOption> codec = BeeSwarmParticleOption.codec(this);
	/** 此粒子类型对应的网络流编解码器 */
    private final StreamCodec<ByteBuf, BeeSwarmParticleOption> streamCodec = BeeSwarmParticleOption.streamCodec(this);
    /**
     * 预构建的默认随机粒子选项。
     * 运动序列和翻转标志均为空（{@link Optional#empty()}），
     * 由粒子实例在初始化时随机选择。
     */
	private final BeeSwarmParticleOption EMPTY=new BeeSwarmParticleOption(this,Optional.empty(),Optional.empty());
	/**
	 * 构造蜜蜂粒子类型。
	 *
	 * @param overrideLimiter 是否覆盖粒子数量限制器
	 */
    public BeeSwarmParticleType(boolean overrideLimiter) {
		super(overrideLimiter);
	}
	/**
	 * 获取默认的随机粒子选项。
	 *
	 * @return 预构建的随机 {@link BeeParticleOption} 实例
	 */
	public BeeSwarmParticleOption empty() {
		return EMPTY;
	}
	/**
	 * 创建指定运动序列的粒子选项（无翻转设置）。
	 *
	 * @return 新的 {@link BeeParticleOption} 实例
	 */
	public BeeSwarmParticleOption create(Vector4fc pos) {
		return new BeeSwarmParticleOption(this,Optional.of(pos),Optional.empty());
	}
	/**
	 * 创建指定运动序列和翻转标志的粒子选项。
	 *
	 * @param list 运动序列列表
	 * @param flip 是否水平翻转纹理
	 * @return 新的 {@link BeeParticleOption} 实例
	 */
	public BeeSwarmParticleOption create(Vector4fc pos,boolean flipped) {
		return new BeeSwarmParticleOption(this,Optional.of(pos),Optional.of(flipped));
	}
	@Override
	public MapCodec<BeeSwarmParticleOption> codec() {
		return codec;
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, BeeSwarmParticleOption> streamCodec() {
		return streamCodec;
	}

}
