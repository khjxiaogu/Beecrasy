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

import java.util.Map;

import com.khjxiaogu.beecrasy.client.utils.FrameManager.FrameData;

import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;

public class BeeDanceSimulator {
	public static final Map<BeeMovement,FrameData<BeeParticle>> MOVEMENTS=Map.of(
		BeeMovement.RANDOM,new FrameData<>(20,BeeDanceSimulator::randomizeMove),
		BeeMovement.FIGURE_8_X,new FrameData<>(40,(age,bee)->BeeDanceSimulator.figure8DanceVelocity(age, bee, Axis.X)),
		BeeMovement.FIGURE_8_Z,new FrameData<>(40,(age,bee)->BeeDanceSimulator.figure8DanceVelocity(age, bee, Axis.Z)),
		BeeMovement.CIRCLE_X,new FrameData<>(20,(age,bee)->BeeDanceSimulator.circleDanceVelocity(age, bee, Axis.X)),
		BeeMovement.CIRCLE_Z,new FrameData<>(20,(age,bee)->BeeDanceSimulator.circleDanceVelocity(age, bee, Axis.Z))
		);
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
			bp.setXd(vx/20f);
		} else {
			bp.setZd(vx/20f);
		}
		bp.setYd(vy/20f);
	}

	public static void figure8DanceVelocity(double t, BeeParticle bp, Axis axis) {
		double omega = FIGURE8_ANGULAR_VEL;
		double A = FIGURE8_A;
		double B = FIGURE8_B;
		double vx = A * omega * Math.cos(omega * t);
		double vy = 2 * B * omega * Math.cos(2 * omega * t);
		if (axis == Axis.X) {
			bp.setXd(vx/20f);
		} else {
			bp.setZd(vx/20f);
		}
		bp.setYd(vy/20f);
	}

	private static boolean isQuarterMultiple(double value) {
		double scaled = value * 4.0;
		double rounded = Math.round(scaled);
		return Math.abs(scaled - rounded) <= 1e-7;
	}

	public static void randomizeMove(double t, BeeParticle bp) {
		if (isQuarterMultiple(t)) {
			bp.setXd(Mth.clamp(bp.getXd() + bp.random().nextGaussian() * 0.02, -0.5, 0.05));
			bp.setYd(Mth.clamp(bp.getYd() + bp.random().nextGaussian() * 0.02, -0.5, 0.05));
			bp.setZd(Mth.clamp(bp.getZd() + bp.random().nextGaussian() * 0.02, -0.5, 0.05));
		}
	}
}