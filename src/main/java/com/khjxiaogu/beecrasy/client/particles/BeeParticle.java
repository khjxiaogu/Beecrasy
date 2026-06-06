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

import com.khjxiaogu.beecrasy.client.utils.FrameManager;
import com.khjxiaogu.beecrasy.client.utils.FrameManager.FrameData;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class BeeParticle extends BeecrasyParticle {
	private static final FrameManager<BeeParticle> CIRCLE_X=new FrameManager<>(BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.CIRCLE_X),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.CIRCLE_X),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM));
	private static final FrameManager<BeeParticle> CIRCLE_Z=new FrameManager<>(BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.CIRCLE_Z),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.CIRCLE_Z),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM));

	private static final FrameManager<BeeParticle> FIG8_X=new FrameManager<>(BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.FIGURE_8_X),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM));
	private static final FrameManager<BeeParticle> FIG8_Z=new FrameManager<>(BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.FIGURE_8_Z),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM));
	private static final FrameManager<BeeParticle> RAND=new FrameManager<>(BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM));
	FrameManager<BeeParticle> frame;
	boolean flipped;
	public BeeParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY,
			double motionZ,SpriteSet sprite,BeeParticleOption option) {
		super(world, x, y, z, motionX, motionY, motionZ,sprite);
		this.gravity = 0F;
		//this.rCol = this.gCol = this.bCol = (float) (Math.random() * 0.2) + 0.8f;
		this.quadSize = 0.125F;
		//this.alpha = 0.75f;
		this.friction=1F;
		this.flipped=option.flipped().orElseGet(this.random::nextBoolean);
		if(option.movements().isPresent()) {
			List<BeeMovement> moves=option.movements().get();
			@SuppressWarnings("unchecked")
			FrameData<BeeParticle>[] movements=new FrameData[moves.size()];
			
			for(int i=0;i<movements.length;i++) {
				movements[i]=BeeDanceSimulator.MOVEMENTS.get(moves.get(i));
			}
			frame=new FrameManager<BeeParticle>(movements);
			this.lifetime = frame.getTotalLength();
		}else{
			float rate=random.nextFloat();
			if(rate<0.1) {
				frame=random.nextBoolean()?FIG8_X:FIG8_Z;
				this.lifetime = frame.getTotalLength()+this.random.nextIntBetweenInclusive(20, 100);
			}else if(rate<0.3) {
				frame=random.nextBoolean()?CIRCLE_X:CIRCLE_Z;
				this.lifetime = frame.getTotalLength()+this.random.nextIntBetweenInclusive(20, 100);
			}else {
				frame=RAND;
				this.lifetime = this.random.nextIntBetweenInclusive(100, 200);
			}
			
		}
		
	}
    protected float getU0() {
        return flipped?super.getU1():super.getU0();
    }

    protected float getU1() {
        return flipped?super.getU0():super.getU1();
    }
	public void randomizeSpeed() {
		this.xd+=super.random.nextFloat()*0.05-0.1;
		this.yd+=super.random.nextFloat()*0.05-0.1;
		this.zd+=super.random.nextFloat()*0.05-0.1;
		this.xd=Mth.clamp(this.xd,-0.05, 0.05);
		this.yd=Mth.clamp(this.yd,-0.05, 0.05);
		this.zd=Mth.clamp(this.zd,-0.05, 0.05);
	}
	public static class Factory implements ParticleProvider<BeeParticleOption> {
		private final SpriteSet spriteSet;

		public Factory(SpriteSet spriteSet) {
			this.spriteSet = spriteSet;
		}

		@Override
		public Particle createParticle(BeeParticleOption typeIn, ClientLevel worldIn, double x, double y, double z,
				double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
			BeeParticle steamParticle = new BeeParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed,this.spriteSet,typeIn);
			return steamParticle;
		}
	}

	@Override
	public void tick() {
		frame.tick(age, this);
		/*for(Direction d:Direction.values()) {
			BlockPos bp=BlockPos.containing(this.x+d.getStepX(), this.y+d.getStepY(), this.z+d.getStepZ());
			if(super.level.getBlockState(bp).isFaceSturdy(level, bp, d.getOpposite())) {
				switch(d.getAxis()) {
				case X:if(Mth.sign(this.xd)==d.getAxisDirection().getStep())this.xd=0;break;
				case Y:if(Mth.sign(this.yd)==d.getAxisDirection().getStep())this.yd=0;break;
				case Z:if(Mth.sign(this.zd)==d.getAxisDirection().getStep())this.zd=0;break;
				}
			}
		}*/
		if(xd>=-1E-7&&xd<=1E-7&&yd>=-1E-7&&yd<=1E-7&&zd>=-1E-7&&zd<=1E-7)
			randomizeSpeed();
		if(this.onGround)
			this.yd=0.01;
		else if(this.stoppedByCollision) {
			this.stoppedByCollision=false;
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
