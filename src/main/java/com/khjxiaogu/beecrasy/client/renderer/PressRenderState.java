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

import org.joml.Quaternionfc;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

/**
 * 压榨机渲染状态类。
 * <p>
 * 继承 {@link BlockEntityRenderState}，存储从 {@code PressBlockEntity} 提取的
 * 动画数据，供 {@link PressBlockEntityRenderer#submit} 方法使用。
 * 包含两个关键动画参数：
 * <ul>
 *   <li>{@link #process}——压板位移比例（0.0 ~ 1.0）；</li>
 *   <li>{@link #animateProcess}——螺杆旋转四元数。</li>
 * </ul>
 */
public class PressRenderState extends BlockEntityRenderState {
	/**
	 * 压板位移比例。
	 * <p>
	 * 范围 [0.0, 1.0]，0.0 表示压板位于最高点（原点），
	 * 1.0 表示压板位于最低点（最大下压位置）。
	 */
	public float process;
	/**
	 * 螺杆旋转四元数。
	 * <p>
	 * 由 {@link PressBlockEntityRenderer#extractRenderState} 根据当前角度计算，
	 * 驱动螺杆模型绕中心旋转。
	 */
	public Quaternionfc animateProcess;
}
