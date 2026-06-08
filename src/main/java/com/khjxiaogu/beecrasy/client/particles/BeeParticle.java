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
public class BeeParticle extends BeecrasyParticle {
	/**
	 * 预定义的圆形舞（X 轴）帧序列管理器。
	 * 序列模式：随机 → 圆形舞 X → 随机 → 圆形舞 X → 随机
	 */
	private static final FrameManager<BeeParticle> CIRCLE_X=new FrameManager<>(BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.CIRCLE_X),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.CIRCLE_X),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM));
	/**
	 * 预定义的圆形舞（Z 轴）帧序列管理器。
	 * 序列模式：随机 → 圆形舞 Z → 随机 → 圆形舞 Z → 随机
	 */
	private static final FrameManager<BeeParticle> CIRCLE_Z=new FrameManager<>(BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.CIRCLE_Z),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.CIRCLE_Z),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM));

	/**
	 * 预定义的 8 字舞（X 轴）帧序列管理器。
	 * 序列模式：随机 → 8字舞 X → 随机
	 */
	private static final FrameManager<BeeParticle> FIG8_X=new FrameManager<>(BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.FIGURE_8_X),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM));
	/**
	 * 预定义的 8 字舞（Z 轴）帧序列管理器。
	 * 序列模式：随机 → 8字舞 Z → 随机
	 */
	private static final FrameManager<BeeParticle> FIG8_Z=new FrameManager<>(BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.FIGURE_8_Z),BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM));
	/**
	 * 预定义的纯随机漫步帧序列管理器。
	 */
	private static final FrameManager<BeeParticle> RAND=new FrameManager<>(BeeDanceSimulator.MOVEMENTS.get(BeeMovement.RANDOM));

	/**
	 * 当前粒子使用的帧管理器，按选项配置或随机选择。
	 */
	FrameManager<BeeParticle> frame;

	/**
	 * 是否水平翻转纹理。
	 * 从 {@link BeeParticleOption#flipped()} 获取，若未指定则随机决定。
	 */
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
		this.xd+=super.random.nextFloat()*0.05-0.1;
		this.yd+=super.random.nextFloat()*0.05-0.1;
		this.zd+=super.random.nextFloat()*0.05-0.1;
		this.xd=Mth.clamp(this.xd,-0.05, 0.05);
		this.yd=Mth.clamp(this.yd,-0.05, 0.05);
		this.zd=Mth.clamp(this.zd,-0.05, 0.05);
	}
	/**
	 * 蜜蜂粒子的工厂类。
	 * <p>
	 * 实现 {@link ParticleProvider} 接口，使用给定的 {@link SpriteSet}
	 * 创建 {@link BeeParticle} 实例。
	 */
	public static class Factory implements ParticleProvider<BeeParticleOption> {
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
		public Particle createParticle(BeeParticleOption typeIn, ClientLevel worldIn, double x, double y, double z,
				double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
			BeeParticle steamParticle = new BeeParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed,this.spriteSet,typeIn);
			return steamParticle;
		}
	}

	/**
	 * 每 tick 更新粒子的状态。
	 * <p>
	 * 调用当前帧管理器更新运动。若三个速度分量均接近零（\u22641e-7），
	 * 则触发 {@link #randomizeSpeed()} 添加随机扰动。
	 * 若粒子接地则将 Y 速度设为固定正向值（0.01）以避免卡住。
	 * 最后调用父类的 {@code tick()} 执行标准物理更新。
	 */
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
