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

public class BeeParticleType extends ParticleType<BeeParticleOption> {
	private final MapCodec<BeeParticleOption> codec = BeeParticleOption.codec(this);
    private final StreamCodec<ByteBuf, BeeParticleOption> streamCodec = BeeParticleOption.streamCodec(this);
	private final BeeParticleOption RANDOM=new BeeParticleOption(this,Optional.empty(),Optional.empty());
    public BeeParticleType(boolean overrideLimiter) {
		super(overrideLimiter);
	}
	public BeeParticleOption random() {
		return RANDOM;
	}
	public BeeParticleOption create(List<BeeMovement> list) {
		return new BeeParticleOption(this,Optional.of(list),Optional.empty());
	}
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
