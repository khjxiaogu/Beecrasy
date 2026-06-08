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

/**
 * 蜜蜂舞蹈运动模拟器。
 * <p>
 * 为蜜蜂粒子提供三种运动模式的速度计算：
 * <ul>
 *   <li><b>随机漫步</b>——每 0.25 秒对速度施加高斯随机扰动；</li>
 *   <li><b>8字舞</b>——沿 X 轴或 Z 轴的利萨茹曲线式 8 字形运动；</li>
 *   <li><b>圆形舞</b>——沿 X 轴或 Z 轴的圆周运动。</li>
 * </ul>
 * 每种运动模式被封装为 {@link FrameData}，注册在 {@link #MOVEMENTS} 映射表中，
 * 由 {@link BeeParticle} 的帧管理器按时间驱动。
 * <p>
 * 物理参数（半径、角速度等）以物理常量形式定义。
 */
public class BeeDanceSimulator {
	/**
	 * 运动类型到帧数据的映射表。
	 * <p>
	 * 每种 {@link BeeMovement} 枚举值对应一个 {@link FrameData}，
	 * 其中包含帧持续时间和对应的速度计算函数。
	 * <ul>
	 *   <li>随机：持续 20 tick，每 tick 调用 {@link #randomizeMove}</li>
	 *   <li>8字舞 X/Z：持续 40 tick，调用 {@link #figure8DanceVelocity}</li>
	 *   <li>圆形舞 X/Z：持续 20 tick，调用 {@link #circleDanceVelocity}</li>
	 * </ul>
	 */
	public static final Map<BeeMovement,FrameData<BeeParticle>> MOVEMENTS=Map.of(
		BeeMovement.RANDOM,new FrameData<>(20,BeeDanceSimulator::randomizeMove),
		BeeMovement.FIGURE_8_X,new FrameData<>(40,(age,bee)->BeeDanceSimulator.figure8DanceVelocity(age, bee, Axis.X)),
		BeeMovement.FIGURE_8_Z,new FrameData<>(40,(age,bee)->BeeDanceSimulator.figure8DanceVelocity(age, bee, Axis.Z)),
		BeeMovement.CIRCLE_X,new FrameData<>(20,(age,bee)->BeeDanceSimulator.circleDanceVelocity(age, bee, Axis.X)),
		BeeMovement.CIRCLE_Z,new FrameData<>(20,(age,bee)->BeeDanceSimulator.circleDanceVelocity(age, bee, Axis.Z))
		);
	/** 圆形舞的半径（格），默认 2.0 */
	private static final double CIRCLE_RADIUS = 2.0;
	/** 圆形舞的角速度（弧度/秒），默认 \u03c0 */
	private static final double CIRCLE_ANGULAR_VEL = Math.PI; // rad/s

	/** 8字舞在 X 方向的振幅，默认 2.0 */
	private static final double FIGURE8_A = 2.0;
	/** 8字舞在 Y 方向的振幅，默认 1.0 */
	private static final double FIGURE8_B = 1.0;
	/** 8字舞的角速度（弧度/秒），默认 \u03c0 */
	private static final double FIGURE8_ANGULAR_VEL = Math.PI;

	/**
	 * 圆形舞蹈速度计算。
	 * <p>
	 * 在指定轴（X 或 Z）的平面内做圆周运动，
	 * 速度向量为角速度与半径的叉积。
	 *
	 * @param t    当前时间（秒）
	 * @param bp   蜜蜂粒子实例，用于设置速度分量
	 * @param axis 旋转平面法线轴（Axis.X 表示 XZ 平面以 X 为法线，Axis.Z 反之）
	 */
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

	/**
	 * 8字舞蹈速度计算。
	 * <p>
	 * 使用利萨茹曲线参数方程在指定轴平面产生 8 字形轨迹，
	 * 水平和垂直方向频率比为 1:2。
	 *
	 * @param t    当前时间（秒）
	 * @param bp   蜜蜂粒子实例，用于设置速度分量
	 * @param axis 运动平面法线轴
	 */
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

	/**
	 * 判断时间值是否为 0.25 的整数倍（允许 1e-7 浮点误差）。
	 * 用于随机运动的更新节拍控制。
	 *
	 * @param value 时间值（秒）
	 * @return 若为 0.25 的整数倍则返回 {@code true}
	 */
	private static boolean isQuarterMultiple(double value) {
		double scaled = value * 4.0;
		double rounded = Math.round(scaled);
		return Math.abs(scaled - rounded) <= 1e-7;
	}

	/**
	 * 随机漫步速度更新。
	 * <p>
	 * 在每 0.25 秒的节拍点，对粒子的三个速度分量施加均值为 0、
	 * 方差为 0.02 的高斯随机扰动，并将各方向速度限幅在 \u00b10.1 范围内。
	 *
	 * @param t  当前时间（秒），用于判断是否到达更新节拍
	 * @param bp 蜜蜂粒子实例，用于读取和设置速度分量
	 */
	public static void randomizeMove(double t, BeeParticle bp) {
		if (isQuarterMultiple(t)) {
			bp.setXd(Mth.clamp(bp.getXd() + bp.random().nextGaussian() * 0.02, -0.1, 0.1));
			bp.setYd(Mth.clamp(bp.getYd() + bp.random().nextGaussian() * 0.02, -0.1, 0.1));
			bp.setZd(Mth.clamp(bp.getZd() + bp.random().nextGaussian() * 0.02, -0.1, 0.1));
		}
	}
}
