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

import com.khjxiaogu.beecrasy.client.particles.FrameManager.FrameData;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class BeeParticle extends BeecrasyParticle {
	FrameManager<BeeMovement> frame;
	public BeeParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY,
			double motionZ,SpriteSet sprite) {
		super(world, x, y, z, motionX, motionY, motionZ,sprite);
		this.gravity = 0F;
		//this.rCol = this.gCol = this.bCol = (float) (Math.random() * 0.2) + 0.8f;
		this.quadSize = 0.125F;
		this.lifetime = this.random.nextIntBetweenInclusive(100, 200);
		//this.alpha = 0.75f;
		this.friction=1F;
		frame=new FrameManager<BeeMovement>(BeeMovement.RANDOM.createFrame());
		if(motionX==0&&motionY==0&&motionZ==0)
			randomizeSpeed();
	}
	public void randomizeSpeed() {
		this.xd+=super.random.nextFloat()*0.05-0.1;
		this.yd+=super.random.nextFloat()*0.05-0.1;
		this.zd+=super.random.nextFloat()*0.05-0.1;
		this.xd=Mth.clamp(this.xd,-0.05, 0.05);
		this.yd=Mth.clamp(this.yd,-0.05, 0.05);
		this.zd=Mth.clamp(this.zd,-0.05, 0.05);
	}
	public static class Factory implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet spriteSet;

		public Factory(SpriteSet spriteSet) {
			this.spriteSet = spriteSet;
		}

		@Override
		public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z,
				double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
			BeeParticle steamParticle = new BeeParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed,this.spriteSet);
			return steamParticle;
		}
	}

	@Override
	public void tick() {
		FrameData<BeeMovement> fd=frame.getData(age);
		fd.data().tick(fd.length(), this, Axis.X);
		if(this.onGround)
			this.yd=0.05;
		else if(this.stoppedByCollision) {
			this.stoppedByCollision=false;
			this.yd=-0.03;
		}
		super.tick();
		

			
	}
	@Override
	public void extract(QuadParticleRenderState particleTypeRenderState, Camera camera, float partialTickTime) {
		float remain=this.lifetime-this.age-partialTickTime;
		if(remain<20)
			alpha=Math.max(remain/20, 0);
		super.extract(particleTypeRenderState, camera, partialTickTime);
	}
}
