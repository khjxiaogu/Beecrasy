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

package com.khjxiaogu.beecrasy.client;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.client.particles.BeeParticleType;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 粒子类型注册。
 * <p>
 * 管理 Beecrasy 模组所有自定义粒子类型的注册。
 */
public class BeecrasyParticles {
	/**
	 * 粒子类型的延迟注册器，注册于 {@link Registries#PARTICLE_TYPE} 注册表，
	 * 命名空间为模组 ID {@link Beecrasy#MODID}。
	 */
	public static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister
			.create(Registries.PARTICLE_TYPE, Beecrasy.MODID);

	/**
	 * 蜜蜂粒子类型的注册引用（名称为 "bee"）。
	 * 该粒子类型用于生成蜜蜂粒子效果，支持运动动画和翻转渲染。
	 */
	public static final DeferredHolder<ParticleType<?>,BeeParticleType> BEE = REGISTER.register("bee",
			() -> new BeeParticleType(false));
}
