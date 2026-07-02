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

package com.khjxiaogu.beecrasy.client.apistle.lines;

import com.khjxiaogu.beecrasy.client.apistle.GuiInfoCollector;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface Line {
	
	/**
	 * 渲染基因信息行。
	 *
	 * @param graphics GUI 图形提取器
	 * @param x        行左上角 X 坐标
	 * @param y        行左上角 Y 坐标
	 * @param mouseX   鼠标相对 X 坐标
	 * @param mouseY   鼠标相对 Y 坐标
	 * @param tooltips 提示文本消费者，用于收集悬停时显示的提示信息
	 * @return 该行占用的高度（像素），用于下一行的 Y 坐标偏移
	 */
	int extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int w, int mouseX, int mouseY, GuiInfoCollector tooltips);

	int precalculateHeight();
}