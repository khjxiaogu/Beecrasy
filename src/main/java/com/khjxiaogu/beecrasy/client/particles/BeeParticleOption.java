package com.khjxiaogu.beecrasy.client.particles;


import java.util.List;
import java.util.Optional;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

public record BeeParticleOption(ParticleType<BeeParticleOption> type,Optional<List<BeeMovement>> movements) implements ParticleOptions{
	@Override
	public ParticleType<?> getType() {
		return type;
	}

}
