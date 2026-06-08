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
import java.util.function.BiFunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 蜜蜂粒子参数记录类。
 * <p>
 * 实现 {@link ParticleOptions} 接口，包含以下可选参数：
 * <ul>
 *   <li>{@code movements}——运动序列列表，指定粒子依次执行的运动模式；</li>
 *   <li>{@code flipped}——是否水平翻转纹理。</li>
 * </ul>
 * 两个参数均为可选，不指定时由粒子实例随机决定。
 * <p>
 * 提供静态工厂方法 {@link #curry} 用于柯里化——固定 {@link ParticleType} 后
 * 返回二元函数以简化构造。支持 MapCodec 和 StreamCodec 序列化。
 *
 * @param type      关联的粒子类型
 * @param movements 运动序列列表（可选）
 * @param flipped   是否水平翻转纹理（可选）
 */
public record BeeParticleOption(ParticleType<BeeParticleOption> type,Optional<List<BeeMovement>> movements,Optional<Boolean> flipped) implements ParticleOptions{
	/**
	 * 柯里化工厂方法——将 {@link ParticleType} 固定后返回二元函数。
	 * <p>
	 * 返回的函数接受（{@code movements}, {@code flipped}）两个参数，
	 * 构造一个 {@link BeeParticleOption} 实例，自动注入已固定的粒子类型。
	 *
	 * @param type 要固定的粒子类型
	 * @return 二元函数（movements, flipped）→ BeeParticleOption
	 */
	public static BiFunction<Optional<List<BeeMovement>>,Optional<Boolean>,BeeParticleOption> curry(ParticleType<BeeParticleOption> type){
		return (m,b)->new BeeParticleOption(type,m,b);
	}
	/**
	 * 返回关联的粒子类型。
	 *
	 * @return 此选项关联的 {@link ParticleType}
	 */
	@Override
	public ParticleType<?> getType() {
		return type;
	}
	/**
	 * 获取此粒子选项的 MapCodec。
	 * <p>
	 * 包含两个可选字段：
	 * <ul>
	 *   <li>{@code movements}——运动序列列表；</li>
	 *   <li>{@code flipped}——水平翻转标志。</li>
	 * </ul>
	 *
	 * @param particleType 关联的粒子类型，注入柯里化工厂
	 * @return 编解码器
	 */
    public static MapCodec<BeeParticleOption> codec(ParticleType<BeeParticleOption> particleType) {
        return RecordCodecBuilder.mapCodec(t->t.group(
        	BeeMovement.CODEC.listOf().optionalFieldOf("movements").forGetter(BeeParticleOption::movements),
        	Codec.BOOL.optionalFieldOf("flipped").forGetter(BeeParticleOption::flipped)
        	).apply(t, curry(particleType)));
    }
    /**
     * 获取此粒子选项的网络流编解码器。
     * <p>
     *
     * @param particleType 关联的粒子类型，注入柯里化工厂
     * @return 流编解码器
     */
    public static StreamCodec<ByteBuf,BeeParticleOption> streamCodec(ParticleType<BeeParticleOption> particleType) {
        return StreamCodec.composite(
        	ByteBufCodecs.optional(BeeMovement.STREAM_CODEC.apply(ByteBufCodecs.list())),BeeParticleOption::movements,
        	ByteBufCodecs.BYTE.map(t->switch(t) {
        	case 0->Optional.empty();
        	case 1->Optional.of(false);
        	case 2->Optional.of(true);
        	default->Optional.empty();
        	}, t-> (byte) (t.isEmpty()?0:(t.get()?2:1))),BeeParticleOption::flipped,
        	curry(particleType));
    }
}
