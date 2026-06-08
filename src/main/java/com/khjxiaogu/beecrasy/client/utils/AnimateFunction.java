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

package com.khjxiaogu.beecrasy.client.utils;

/**
 * 动画函数接口。
 * <p>
 * 泛型函数式接口，接收时间参数 {@code t}（秒）和目标对象 {@code bp}，
 * 每帧由 {@link FrameManager#tick} 调用以更新目标对象的动画状态。
 * 用于驱动粒子的运动计算和方块实体动画。
 *
 * @param <T> 动画作用的目标对象类型
 */
public interface AnimateFunction<T> {
	/**
	 * 执行动画更新。
	 *
	 * @param t  当前帧内时间（秒），范围取决于 {@link FrameData#length}
	 * @param bp 被动画驱动的目标对象
	 */
	void tick(double t,T bp);
}
