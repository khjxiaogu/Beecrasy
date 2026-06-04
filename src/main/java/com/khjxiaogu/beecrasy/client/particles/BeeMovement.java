package com.khjxiaogu.beecrasy.client.particles;

import com.khjxiaogu.beecrasy.client.particles.FrameManager.FrameData;

import net.minecraft.core.Direction.Axis;


public enum BeeMovement{
	RANDOM(20,BeeDanceSimulator::randomizeMove),FIGURE8(40,BeeDanceSimulator::figure8DanceVelocity),CIRCLE(20,BeeDanceSimulator::circleDanceVelocity);
	final int length;
	final MovementHandler movement;
	private BeeMovement(int length, MovementHandler movement) {
		this.length = length;
		this.movement = movement;
	}
	public void tick(int len,BeeParticle part,Axis axis) {
		movement.tick(len/20d, part,axis);
	}
	public FrameData<BeeMovement> createFrame(){
		return new FrameData<>(length,this);
	}
}
