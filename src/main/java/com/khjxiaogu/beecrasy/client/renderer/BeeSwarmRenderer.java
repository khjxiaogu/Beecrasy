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

package com.khjxiaogu.beecrasy.client.renderer;


import com.khjxiaogu.beecrasy.entity.BeeSwarmEntity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;

/**
 * 蜂群实体渲染器——为 {@link BeeSwarmEntity} 提供渲染管线入口。
 * <p>
 * 继承 {@link EntityRenderer}<{@link BeeSwarmEntity}, {@link BeeSwarmState}>，
 * 主要职责是实例化 {@link BeeSwarmState} 作为渲染状态载体。
 * 实际的渲染逻辑由 Minecraft 的实体渲染框架通过控制器和视觉模型完成。
 * <p>
 * 这个类本身非常精简，因为具体渲染工作（粒子管理、模型绑定等）
 * 由 {@link net.minecraft.client.renderer.entity.EntityRenderDispatcher}
 * 和注册的 {@link net.minecraft.client.renderer.entity.EntityRendererProvider} 协作完成。
 */
public class BeeSwarmRenderer extends EntityRenderer<BeeSwarmEntity,BeeSwarmState> {

	public BeeSwarmRenderer(Context context) {
		super(context);
	}

	/**
	 * 创建一个新的空渲染状态实例。
	 * <p>
	 * 此方法由 Minecraft 的实体渲染框架在每帧调用，
	 * 用于收集实体当前帧的渲染数据。
	 *
	 * @return 新的 {@link BeeSwarmState} 实例
	 */
	@Override
	public BeeSwarmState createRenderState() {
		return new BeeSwarmState();
	}


}
