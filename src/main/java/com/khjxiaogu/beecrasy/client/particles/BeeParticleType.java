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

import java.util.List;
import java.util.Optional;

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
public class BeeParticleType extends ParticleType<BeeParticleOption> {
	/** 此粒子类型对应的 MapCodec */
	private final MapCodec<BeeParticleOption> codec = BeeParticleOption.codec(this);
	/** 此粒子类型对应的网络流编解码器 */
    private final StreamCodec<ByteBuf, BeeParticleOption> streamCodec = BeeParticleOption.streamCodec(this);
    /**
     * 预构建的默认随机粒子选项。
     * 运动序列和翻转标志均为空（{@link Optional#empty()}），
     * 由粒子实例在初始化时随机选择。
     */
	private final BeeParticleOption RANDOM=new BeeParticleOption(this,Optional.empty(),Optional.empty());
	/**
	 * 构造蜜蜂粒子类型。
	 *
	 * @param overrideLimiter 是否覆盖粒子数量限制器
	 */
    public BeeParticleType(boolean overrideLimiter) {
		super(overrideLimiter);
	}
	/**
	 * 获取默认的随机粒子选项。
	 *
	 * @return 预构建的随机 {@link BeeParticleOption} 实例
	 */
	public BeeParticleOption random() {
		return RANDOM;
	}
	/**
	 * 创建指定运动序列的粒子选项（无翻转设置）。
	 *
	 * @param list 运动序列列表
	 * @return 新的 {@link BeeParticleOption} 实例
	 */
	public BeeParticleOption create(List<BeeMovement> list) {
		return new BeeParticleOption(this,Optional.of(list),Optional.empty());
	}
	/**
	 * 创建指定运动序列和翻转标志的粒子选项。
	 *
	 * @param list 运动序列列表
	 * @param flip 是否水平翻转纹理
	 * @return 新的 {@link BeeParticleOption} 实例
	 */
	public BeeParticleOption create(List<BeeMovement> list,boolean flip) {
		return new BeeParticleOption(this,Optional.of(list),Optional.of(flip));
	}
	@Override
	public MapCodec<BeeParticleOption> codec() {
		return codec;
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, BeeParticleOption> streamCodec() {
		return streamCodec;
	}

}
