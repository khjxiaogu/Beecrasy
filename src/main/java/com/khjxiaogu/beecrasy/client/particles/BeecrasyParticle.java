/*
 * Copyright (c) 2024 TeamMoeg
 *
 * This file is part of Caupona.
 *
 * Caupona is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Caupona is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Specially, we allow this software to be used alongside with closed source software Minecraft(R) and Forge or other modloader.
 * Any mods or plugins can also use apis provided by forge or com.teammoeg.caupona.api without using GPL or open source.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caupona. If not, see <https://www.gnu.org/licenses/>.
 */

package com.khjxiaogu.beecrasy.client.particles;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.Mth;

public class BeecrasyParticle extends SingleQuadParticle {
	private SpriteSet spriteSet;
	protected BeecrasyParticle(ClientLevel world, double x, double y, double z,SpriteSet p_sprites) {
		super(world, x, y, z, p_sprites.first());
		this.spriteSet=p_sprites;
		
	}

	public BeecrasyParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ,SpriteSet p_sprites) {
		super(world, x, y, z, motionX, motionY, motionZ, p_sprites.first());
		this.spriteSet=p_sprites;
	}

	public ParticleRenderType getRenderType() {
		return ParticleRenderType.SINGLE_QUADS;
	}
	@Override
	public void extract(QuadParticleRenderState particleTypeRenderState, Camera camera, float partialTickTime) {

		super.extract(particleTypeRenderState, camera, partialTickTime);
	}

	@Override
	public void tick() {
		super.tick();

		if(this.spriteSet!=null)
			this.setSpriteFromAge(this.spriteSet);
	}

	@Override
	protected Layer getLayer() {
		return SingleQuadParticle.Layer.TRANSLUCENT;
	}


}
