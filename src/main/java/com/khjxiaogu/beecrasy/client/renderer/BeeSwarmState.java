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

import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * 蜂群实体的渲染状态——承载每帧渲染所需的瞬时数据。
 * <p>
 * 继承 {@link EntityRenderState}，当前为空标记类。
 * {@link BeeSwarmEntity} 的渲染主要依赖粒子效果而非传统模型，
 * 因此渲染状态中无需传递额外的渲染参数。
 * 预留以供未来扩展（如颜色、密度等视觉效果参数）。
 */
public class BeeSwarmState extends EntityRenderState {

}
