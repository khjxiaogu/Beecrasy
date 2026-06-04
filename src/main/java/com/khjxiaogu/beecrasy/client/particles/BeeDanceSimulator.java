package com.khjxiaogu.beecrasy.client.particles;

import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;

public class BeeDanceSimulator {

	private static final double CIRCLE_RADIUS = 2.0;
	private static final double CIRCLE_ANGULAR_VEL = Math.PI; // rad/s

	private static final double FIGURE8_A = 2.0;
	private static final double FIGURE8_B = 1.0;
	private static final double FIGURE8_ANGULAR_VEL = Math.PI;

	public static void circleDanceVelocity(double t, BeeParticle bp, Axis axis) {
		double omega = CIRCLE_ANGULAR_VEL;
		double R = CIRCLE_RADIUS;
		double vx = -R * omega * Math.sin(omega * t);
		double vy = R * omega * Math.cos(omega * t);
		if (axis == Axis.X) {
			bp.setXd(vx);
		} else {
			bp.setZd(vx);
		}
		bp.setYd(vy);
	}

	public static void figure8DanceVelocity(double t, BeeParticle bp, Axis axis) {
		double omega = FIGURE8_ANGULAR_VEL;
		double A = FIGURE8_A;
		double B = FIGURE8_B;
		double vx = A * omega * Math.cos(omega * t);
		double vy = 2 * B * omega * Math.cos(2 * omega * t);
		if (axis == Axis.X) {
			bp.setXd(vx);
		} else {
			bp.setZd(vx);
		}
		bp.setYd(vy);
	}

	private static boolean isQuarterMultiple(double value) {
		double scaled = value * 4.0;
		double rounded = Math.round(scaled);
		return Math.abs(scaled - rounded) <= 1e-7;
	}

	public static void randomizeMove(double t, BeeParticle bp, Axis axis) {
		if (isQuarterMultiple(t)) {
			bp.setXd(Mth.clamp(bp.getXd() + bp.random().nextGaussian() * 0.02, -0.5, 0.05));
			bp.setYd(Mth.clamp(bp.getYd() + bp.random().nextGaussian() * 0.02, -0.5, 0.05));
			bp.setZd(Mth.clamp(bp.getZd() + bp.random().nextGaussian() * 0.02, -0.5, 0.05));
		}
	}
}