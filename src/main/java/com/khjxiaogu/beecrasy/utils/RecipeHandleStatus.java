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

package com.khjxiaogu.beecrasy.utils;

/**
 * 配方处理状态枚举。
 * <p>
 * 表示配方执行的结果状态：成功、阻塞（等待下次尝试）或失败。
 */
public enum RecipeHandleStatus {
	/** 配方执行成功。 */
	SUCCEED,
	/** 配方执行被阻塞，需要等待下次尝试。 */
	BLOCKED,
	/** 配方执行失败。 */
	FAILED;
	/**
	 * 根据布尔值返回成功或阻塞状态。
	 *
	 * @param isSuccess 是否成功
	 * @return 成功时返回 {@link #SUCCEED}，否则返回 {@link #BLOCKED}
	 */
	public static RecipeHandleStatus success(boolean isSuccess) {
		return isSuccess?SUCCEED:BLOCKED;
	}
	/**
	 * 判断是否需要重置处理进度。
	 * <p>
	 * 当状态为 {@link #SUCCEED} 或 {@link #FAILED} 时返回 {@code true}。
	 *
	 * @return 如果需要重置进度则返回 {@code true}
	 */
	public boolean resetsProcess() {
		return this==SUCCEED||this==FAILED;
	}
}
