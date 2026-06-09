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

import org.joml.Vector4fc;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * 蜂群粒子——模拟蜜蜂围绕一个中心点进行轨道运动的粒子。
 * <p>
 * 继承自 {@link BeecrasyParticle}，使用轨道物理而非预定义动画：
 * 每 5 tick 将速度分解为径向和切向分量，径向施加弹簧力将粒子拉向目标轨道半径，
 * 切向调整至目标速率，从而形成环绕运动。
 * <p>
 * 支持：
 * <ul>
 *   <li>通过 {@link BeeSwarmParticleOption} 指定轨道中心 (x,y,z) 和半径 (w)；</li>
 *   <li>纹理水平翻转渲染（{@link #flipped} 控制 UV 交换）；</li>
 *   <li>生命周期最后 1 秒（20 tick）线性渐隐；</li>
 *   <li>落地时轻微弹起。</li>
 * </ul>
 */
public class BeeSwarmParticle extends BeecrasyParticle {
	float centerX,centerY,centerZ,radius;
	boolean flipped;
	/**
	 * 构造一个蜂群粒子实例。
	 * <p>
	 * 初始化粒子属性：重力为 0、尺寸 0.125、摩擦力为 1，生命周期固定 80 tick。
	 * 根据选项中的轨道中心（{@link BeeSwarmParticleOption#center()}）设置
	 * {@link #centerX}、{@link #centerY}、{@link #centerZ} 和 {@link #radius}；
	 * 若选项未提供中心，则以粒子生成位置为中心、半径默认为 1。
	 * 纹理翻转标志 {@link #flipped} 取自选项或随机决定。
	 *
	 * @param world   客户端世界
	 * @param x       初始 X 坐标
	 * @param y       初始 Y 坐标
	 * @param z       初始 Z 坐标
	 * @param motionX 初始 X 速度
	 * @param motionY 初始 Y 速度
	 * @param motionZ 初始 Z 速度
	 * @param sprite  精灵集，用于按年龄切换纹理
	 * @param option  粒子选项，包含轨道中心/半径和翻转参数
	 */
	public BeeSwarmParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY,
			double motionZ,SpriteSet sprite,BeeSwarmParticleOption option) {
		super(world, x, y, z, motionX, motionY, motionZ,sprite);
		this.gravity = 0F;
		//this.rCol = this.gCol = this.bCol = (float) (Math.random() * 0.2) + 0.8f;
		this.quadSize = 0.125F;
		//this.alpha = 0.75f;
		this.friction=1F;
		if(option.center().isPresent()) {
			Vector4fc center=option.center().get();
			this.centerX=center.x();
			this.centerY=center.y();
			this.centerZ=center.z();
			this.radius=center.w();
		}else {
			this.centerX=(float)x;
			this.centerY=(float)y;
			this.centerZ=(float)z;
			this.radius=1f;
		}

		this.flipped=option.flipped().orElseGet(this.random::nextBoolean);
		this.lifetime = 80;
		
	}
	/**
	 * 获取纹理左侧 U 坐标（水平翻转时与右侧交换）。
	 * <p>
	 * 当 {@link #flipped} 为 true 时，返回父类的右侧 U 坐标 {@code getU1()}，
	 * 从而实现纹理水平镜像翻转。
	 *
	 * @return 纹理左侧 U 坐标
	 */
    protected float getU0() {
        return flipped?super.getU1():super.getU0();
    }

    /**
     * 获取纹理右侧 U 坐标（水平翻转时与左侧交换）。
     * <p>
     * 当 {@link #flipped} 为 true 时，返回父类的左侧 U 坐标 {@code getU0()}，
     * 与 {@link #getU0()} 配合实现水平镜像效果。
     *
     * @return 纹理右侧 U 坐标
     */
    protected float getU1() {
        return flipped?super.getU0():super.getU1();
    }
	/**
	 * 随机设置三轴速度。
	 * <p>
	 * 在每个轴上生成 [-0.1, -0.05) 范围的随机值，然后限幅到 [-0.05, 0.05] 范围内。
	 * 当粒子恰好位于轨道中心（距离中心 < 1e-4）时由 {@link #tick()} 调用，
	 * 给予粒子一个初始速度以脱离中心点。
	 */
	public void randomizeSpeed() {
		this.xd=super.random.nextFloat()*0.05-0.1;
		this.yd=super.random.nextFloat()*0.05-0.1;
		this.zd=super.random.nextFloat()*0.05-0.1;
		this.xd=Mth.clamp(this.xd,-0.05, 0.05);
		this.yd=Mth.clamp(this.yd,-0.05, 0.05);
		this.zd=Mth.clamp(this.zd,-0.05, 0.05);
	}
	/**
	 * {@link BeeSwarmParticle} 的工厂类。
	 * <p>
	 * 实现 {@link ParticleProvider}<{@link BeeSwarmParticleOption}> 接口，
	 * 使用给定的 {@link SpriteSet} 为创建的粒子提供纹理。
	 */
	public static class Factory implements ParticleProvider<BeeSwarmParticleOption> {
		private final SpriteSet spriteSet;

		/**
		 * 构造工厂实例。
		 *
		 * @param spriteSet 精灵集，创建粒子时通过它按年龄选取纹理帧
		 */
		public Factory(SpriteSet spriteSet) {
			this.spriteSet = spriteSet;
		}

		/**
		 * 创建一个新的蜂群粒子实例。
		 *
		 * @param typeIn   粒子选项（轨道中心/半径 + 翻转标志）
		 * @param worldIn  客户端世界
		 * @param x        X 坐标
		 * @param y        Y 坐标
		 * @param z        Z 坐标
		 * @param xSpeed   X 速度
		 * @param ySpeed   Y 速度
		 * @param zSpeed   Z 速度
		 * @param random   Minecraft 随机源
		 * @return 创建的 {@link BeeSwarmParticle} 实例
		 */
		@Override
		public Particle createParticle(BeeSwarmParticleOption typeIn, ClientLevel worldIn, double x, double y, double z,
				double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
			return new BeeSwarmParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed,this.spriteSet,typeIn);
		}
	}
	public static final double targetTangentialSpeed=0.1f;
	public static final double tangentialAdjustRate=0.04f;
	public static final double radialStiffness=0.04f;
	@Override
	public void tick() {
		if(age%5==0) {
			
		    double rx=x-centerX;
		    double ry=y-centerY;
		    double rz=z-centerZ;
		    double r = Math.sqrt(rx*rx+ry*ry+rz*rz);
		    // 若恰好在球心，随机生成一个速度使其脱离中心
		    if (r < 1e-4) {
		    	randomizeSpeed();
		    }else {
			    // 径向单位向量
			    double invR = 1.0 / r;
			    double ux = rx * invR;
			    double uy = ry * invR;
			    double uz = rz * invR;
			    // 将当前速度分解为径向分量和切向分量（绕轨道运动）
			    double vx = xd;
			    double vy = yd;
			    double vz = zd;
			    double vr = vx * ux + vy * uy + vz * uz;
			    double vtan_x = vx - vr * ux;// 切向速度向量
			    double vtan_y = vy - vr * uy;
			    double vtan_z = vz - vr * uz;
		
			    // 用弹簧模型计算径向速度：将粒子拉向目标轨道半径
			    double radialSpeedNew = -radialStiffness * (r - radius);
			    double vtan = Math.sqrt(vtan_x * vtan_x + vtan_y * vtan_y + vtan_z * vtan_z);
			    double targetVtan;
			    if (vtan > targetTangentialSpeed) {
			    	targetVtan = Math.max(targetTangentialSpeed, vtan - tangentialAdjustRate);
		        } else {
		        	targetVtan = Math.min(targetTangentialSpeed, vtan + tangentialAdjustRate);
		        }
		        // 保持原切向方向，调整速度大小至目标值
		        double scale = targetVtan / vtan;
		        vtan_x *= scale;
		        vtan_y *= scale;
		        vtan_z *= scale;
			    // 合成总速度：径向分量（弹簧恢复力）+ 切向分量（轨道运动）
			    this.xd = vtan_x + radialSpeedNew * ux;
			    this.yd = vtan_y + radialSpeedNew * uy;
			    this.zd = vtan_z + radialSpeedNew * uz;
		    }
		}
		if(this.onGround)
			this.yd=0.01;
		else if(this.stoppedByCollision) {
			this.stoppedByCollision=false;
		}
		super.tick();
	}
	/**
	 * 提取粒子渲染状态（含渐隐效果）。
	 * <p>
	 * 在粒子生命周期的最后 20 tick（约 1 秒）内，
	 * 将透明度 {@code alpha} 从当前值线性衰减至 0，实现平滑消失。
	 *
	 * @param particleTypeRenderState 粒子渲染状态
	 * @param camera                  渲染相机
	 * @param partialTickTime         部分 tick 时间（插值用）
	 */
	@Override
	public void extract(QuadParticleRenderState particleTypeRenderState, Camera camera, float partialTickTime) {
		float remain=this.lifetime-this.age-partialTickTime;
		if(remain<20)
			alpha=Math.max(remain/20, 0);
		super.extract(particleTypeRenderState, camera, partialTickTime);
	}
}
