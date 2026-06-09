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
 * 蜂群粒子类型——管理 {@link BeeSwarmParticle} 的序列化与默认选项。
 * <p>
 * 继承 {@link ParticleType}<{@link BeeSwarmParticleOption}>，持有预构建的空选项
 * （{@link #EMPTY}，中心/翻转均为空，由粒子运行时决定），
 * 并提供便捷方法创建带轨道中心和翻转标志的选项。
 * 管理自身的 MapCodec 和 StreamCodec。
 */
public class BeeSwarmParticleType extends ParticleType<BeeSwarmParticleOption> {
	/** 此粒子类型对应的 MapCodec，委托给 {@link BeeSwarmParticleOption#codec(ParticleType)} */
	private final MapCodec<BeeSwarmParticleOption> codec = BeeSwarmParticleOption.codec(this);
	/** 此粒子类型对应的网络流编解码器，委托给 {@link BeeSwarmParticleOption#streamCodec(ParticleType)} */
    private final StreamCodec<ByteBuf, BeeSwarmParticleOption> streamCodec = BeeSwarmParticleOption.streamCodec(this);
    /**
     * 预构建的空选项实例。
     * center 和 flipped 均为 {@link Optional#empty()}，
     * 由 {@link BeeSwarmParticle} 在初始化时自行决定。
     */
	private final BeeSwarmParticleOption EMPTY=new BeeSwarmParticleOption(this,Optional.empty(),Optional.empty());
	/**
	 * 构造蜂群粒子类型。
	 *
	 * @param overrideLimiter 是否覆盖粒子数量限制器
	 */
    public BeeSwarmParticleType(boolean overrideLimiter) {
		super(overrideLimiter);
	}
	/**
	 * 获取空选项（中心/翻转均未指定）。
	 *
	 * @return 预构建的空 {@link BeeSwarmParticleOption} 实例
	 */
	public BeeSwarmParticleOption empty() {
		return EMPTY;
	}
	/**
	 * 创建指定轨道中心（含半径）的粒子选项，翻转标志由粒子运行时决定。
	 *
	 * @param pos 轨道中心坐标 (x,y,z) 与半径 (w)，封装为 Vector4fc
	 * @return 新的 {@link BeeSwarmParticleOption} 实例
	 */
	public BeeSwarmParticleOption create(Vector4fc pos) {
		return new BeeSwarmParticleOption(this,Optional.of(pos),Optional.empty());
	}
	/**
	 * 创建指定轨道中心和翻转标志的粒子选项。
	 *
	 * @param pos     轨道中心坐标 (x,y,z) 与半径 (w)
	 * @param flipped 是否水平翻转纹理
	 * @return 新的 {@link BeeSwarmParticleOption} 实例
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
