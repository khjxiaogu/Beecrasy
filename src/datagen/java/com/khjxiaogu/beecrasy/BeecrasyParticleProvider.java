package com.khjxiaogu.beecrasy;

import com.khjxiaogu.beecrasy.client.BeecrasyParticles;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.data.ParticleDescriptionProvider;

public class BeecrasyParticleProvider extends ParticleDescriptionProvider {

	protected BeecrasyParticleProvider(PackOutput output) {
		super(output);
	}

	@Override
	protected void addDescriptions() {
		super.spriteSet(BeecrasyParticles.BEE.get(), Beecrasy.rl("bee"));
	}

}
