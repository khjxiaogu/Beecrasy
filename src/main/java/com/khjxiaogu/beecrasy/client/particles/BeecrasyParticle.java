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

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.RandomSource;

/**
 * Beecrasy 粒子基类。
 * <p>
 * 继承 {@link SingleQuadParticle}，封装了 {@link SpriteSet} 以支持按年龄切换精灵。
 * 将速度分量（{@code xd}、{@code yd}、{@code zd}）和随机源（{@code random}）
 * 通过受保护的 getter/setter 暴露给子类（如 {@link BeeParticle}），
 * 以便在运动计算中直接操作。
 * <p>
 * 渲染类型固定为 {@link ParticleRenderType#SINGLE_QUADS}，
 * 渲染层为 {@link SingleQuadParticle.Layer#TRANSLUCENT}（半透明）。
 */
public class BeecrasyParticle extends SingleQuadParticle {
	/** 精灵集引用，用于在每 tick 按年龄更新精灵 */
	private SpriteSet spriteSet;

	/**
	 * 构造粒子（无初始速度）。
	 *
	 * @param world     客户端世界
	 * @param x         初始 X 坐标
	 * @param y         初始 Y 坐标
	 * @param z         初始 Z 坐标
	 * @param p_sprites 精灵集，取第一个精灵作为初始纹理
	 */
	protected BeecrasyParticle(ClientLevel world, double x, double y, double z,SpriteSet p_sprites) {
		super(world, x, y, z, p_sprites.first());
		this.spriteSet=p_sprites;
		
	}

	/**
	 * 构造粒子（含初始速度）。
	 *
	 * @param world     客户端世界
	 * @param x         初始 X 坐标
	 * @param y         初始 Y 坐标
	 * @param z         初始 Z 坐标
	 * @param motionX   初始 X 速度
	 * @param motionY   初始 Y 速度
	 * @param motionZ   初始 Z 速度
	 * @param p_sprites 精灵集，取第一个精灵作为初始纹理
	 */
	public BeecrasyParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ,SpriteSet p_sprites) {
		super(world, x, y, z, motionX, motionY, motionZ, p_sprites.first());
		this.spriteSet=p_sprites;
	}

	/**
	 * 返回粒子的渲染类型。
	 *
	 * @return {@link ParticleRenderType#SINGLE_QUADS}
	 */
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.SINGLE_QUADS;
	}
	@Override
	public void extract(QuadParticleRenderState particleTypeRenderState, Camera camera, float partialTickTime) {

		super.extract(particleTypeRenderState, camera, partialTickTime);
	}
	/**
	 * 获取 X 轴速度分量。
	 *
	 * @return X 速度
	 */
	public double getXd() {
		return xd;
	}
	/**
	 * 获取 Y 轴速度分量。
	 *
	 * @return Y 速度
	 */
    public double getYd() {
		return yd;
	}
    /**
     * 获取 Z 轴速度分量。
     *
     * @return Z 速度
     */
    public double getZd() {
		return zd;
	}
	/**
	 * 设置 X 轴速度分量。
	 *
	 * @param xd X 速度值
	 */
	public void setXd(double xd) {
		this.xd=xd;
	}
	/**
	 * 设置 Y 轴速度分量。
	 *
	 * @param yd Y 速度值
	 */
    public void setYd(double yd) {
    	this.yd=yd;
	}
    /**
     * 设置 Z 轴速度分量。
     *
     * @param zd Z 速度值
     */
    public void setZd(double zd) {
		this.zd=zd;
	}
    /**
     * 暴露 Minecraft 随机源给子类使用。
     *
     * @return {@link RandomSource} 实例
     */
    public RandomSource random() {
    	return random;
    }
	/**
	 * 每 tick 更新粒子的状态。
	 * <p>
	 * 调用父类 {@code tick()} 执行标准物理更新后，
	 * 根据粒子年龄更新精灵纹理（{@link SpriteSet#get}）。
	 */
	@Override
	public void tick() {
		super.tick();

		if(this.spriteSet!=null)
			this.setSpriteFromAge(this.spriteSet);
	}

	/**
	 * 返回粒子的渲染层。
	 *
	 * @return {@link SingleQuadParticle.Layer#TRANSLUCENT}（半透明层）
	 */
	@Override
	protected Layer getLayer() {
		return SingleQuadParticle.Layer.TRANSLUCENT;
	}


}
