package com.khjxiaogu.beecrasy.client.particles;

import net.minecraft.core.Direction.Axis;

public interface MovementHandler {
	void tick(double t,BeeParticle bp,Axis axis);
}
