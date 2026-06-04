package com.khjxiaogu.beecrasy.client.particles;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class BeeParticleType extends ParticleType<BeeParticleOption> {

	protected BeeParticleType(boolean overrideLimiter) {
		super(overrideLimiter);
	}

	@Override
	public MapCodec<BeeParticleOption> codec() {
		return null;
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, BeeParticleOption> streamCodec() {
		return null;
	}

}
