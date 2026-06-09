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

import com.khjxiaogu.beecrasy.client.utils.FrameManager;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * 蜜蜂粒子实体类。
 * <p>
 * 继承自 {@link BeecrasyParticle}，使用 {@link FrameManager} 驱动运动动画。
 * 根据 {@link BeeParticleOption} 中的运动列表或随机权重选择运动序列
 * （10% 概率 8 字舞、20% 概率圆形舞、70% 概率随机漫步）。
 * <p>
 * 支持纹理水平镜像翻转渲染、生命周期最后 1 秒线性渐隐，
 * 以及速度分量为零时的随机扰动。
 */
public class BeeSwarmParticle extends BeecrasyParticle {
	float centerX,centerY,centerZ,radius;
	boolean flipped;
	/**
	 * 构造一个蜜蜂粒子实例。
	 * <p>
	 * 初始化粒子属性：重力为 0、尺寸 0.125、摩擦力为 1。
	 * 根据选项决定运动序列：
	 * <ul>
	 *   <li>若选项包含运动列表，则按列表构建 {@link FrameManager}；</li>
	 *   <li>否则按概率随机选择：10% 8字舞、20% 圆形舞、70% 随机漫步。</li>
	 * </ul>
	 * 生命周期由所选运动序列的总时长加上随机扩展决定。
	 *
	 * @param world   客户端世界
	 * @param x       初始 X 坐标
	 * @param y       初始 Y 坐标
	 * @param z       初始 Z 坐标
	 * @param motionX 初始 X 速度
	 * @param motionY 初始 Y 速度
	 * @param motionZ 初始 Z 速度
	 * @param sprite  精灵集，用于按年龄切换纹理
	 * @param option  粒子选项，包含运动序列和翻转参数
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
	 * 获取纹理的左侧 U 坐标。
	 * <p>
	 * 若设置了水平翻转（{@link #flipped}），则交换左右 UV 坐标。
	 *
	 * @return 左侧 U 坐标
	 */
    protected float getU0() {
        return flipped?super.getU1():super.getU0();
    }

    /**
     * 获取纹理的右侧 U 坐标。
     * <p>
     * 若设置了水平翻转（{@link #flipped}），则交换左右 UV 坐标。
     *
     * @return 右侧 U 坐标
     */
    protected float getU1() {
        return flipped?super.getU0():super.getU1();
    }
	/**
	 * 随机扰动速度分量。
	 * <p>
	 * 对三个速度分量各施加一个 \u00b10.05 范围内的随机偏移，
	 * 并将每个分量限幅在 \u00b10.05 内。
	 * 当速度过零时由 {@link #tick()} 调用。
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
	 * 蜜蜂粒子的工厂类。
	 * <p>
	 * 实现 {@link ParticleProvider} 接口，使用给定的 {@link SpriteSet}
	 * 创建 {@link BeeSwarmParticle} 实例。
	 */
	public static class Factory implements ParticleProvider<BeeSwarmParticleOption> {
		private final SpriteSet spriteSet;

		/**
		 * 使用指定的精灵集构造工厂。
		 *
		 * @param spriteSet 精灵集，用于创建粒子时分配纹理
		 */
		public Factory(SpriteSet spriteSet) {
			this.spriteSet = spriteSet;
		}

		/**
		 * 创建一个新的蜜蜂粒子实例。
		 *
		 * @param typeIn   粒子选项参数
		 * @param worldIn  客户端世界
		 * @param x        X 坐标
		 * @param y        Y 坐标
		 * @param z        Z 坐标
		 * @param xSpeed   X 速度
		 * @param ySpeed   Y 速度
		 * @param zSpeed   Z 速度
		 * @param random   随机源
		 * @return 创建的蜜蜂粒子实例
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
		    // 若恰好在球心，随机生成一个向外的速度
		    if (r < 1e-4) {
		    	randomizeSpeed();
		    }else {
			    // 径向单位向量
			    double invR = 1.0 / r;
			    double ux = rx * invR;
			    double uy = ry * invR;
			    double uz = rz * invR;
			    // 将当前速度分解为径向分量和切向分量
			    double vx = xd;
			    double vy = yd;
			    double vz = zd;
			    double vr = vx * ux + vy * uy + vz * uz;
			    double vtan_x = vx - vr * ux;// 切向速度向量
			    double vtan_y = vy - vr * uy;
			    double vtan_z = vz - vr * uz;
		
			    // 径向速度大小
			    double radialSpeedNew = -radialStiffness * (r - radius);
			    double vtan = Math.sqrt(vtan_x * vtan_x + vtan_y * vtan_y + vtan_z * vtan_z);
			    double targetVtan;
			    if (vtan > targetTangentialSpeed) {
			    	targetVtan = Math.max(targetTangentialSpeed, vtan - tangentialAdjustRate);
		        } else {
		        	targetVtan = Math.min(targetTangentialSpeed, vtan + tangentialAdjustRate);
		        }
		        // 保持原切向方向，缩放长度
		        double scale = targetVtan / vtan;
		        vtan_x *= scale;
		        vtan_y *= scale;
		        vtan_z *= scale;
			    // 合成总速度：v = 径向分量 + 切向分量
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
	 * 提取渲染状态。
	 * <p>
	 * 在粒子生命周期的最后 1 秒（20 tick）内，
	 * 线性渐隐透明度 {@code alpha} 至 0。
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
