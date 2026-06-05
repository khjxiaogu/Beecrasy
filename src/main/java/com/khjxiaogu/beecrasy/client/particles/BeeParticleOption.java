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
import java.util.function.Function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BeeParticleOption(ParticleType<BeeParticleOption> type,Optional<List<BeeMovement>> movements) implements ParticleOptions{
	public static Function<Optional<List<BeeMovement>>,BeeParticleOption> curry(ParticleType<BeeParticleOption> type){
		return m->new BeeParticleOption(type,m);
	}
	@Override
	public ParticleType<?> getType() {
		return type;
	}
    public static MapCodec<BeeParticleOption> codec(ParticleType<BeeParticleOption> particleType) {
        return RecordCodecBuilder.mapCodec(t->t.group(
        	BeeMovement.CODEC.listOf().optionalFieldOf("movements").forGetter(BeeParticleOption::movements)
        	).apply(t, curry(particleType)));
    }
    public static StreamCodec<ByteBuf,BeeParticleOption> streamCodec(ParticleType<BeeParticleOption> particleType) {
        return StreamCodec.composite(
        	ByteBufCodecs.optional(BeeMovement.STREAM_CODEC.apply(ByteBufCodecs.list())),BeeParticleOption::movements,
        	curry(particleType));
    }
}
