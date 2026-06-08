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

package com.khjxiaogu.beecrasy.client.screens.sequencertabs;

import java.util.function.Consumer;

import com.khjxiaogu.beecrasy.genome.AllelesHolder;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * 基因信息显示行接口。
 * <p>
 * 定义在 GUI 中渲染单行基因信息的方法签名。
 * 实现类需要接收父系和母系的 {@link AllelesHolder}，在指定位置渲染文本，
 * 并返回占用的高度用于后续行的定位。
 */
public interface Line {

	/**
	 * 渲染基因信息行。
	 *
	 * @param graphics GUI 图形提取器
	 * @param ah1      母系等位基因持有者（或唯一基因组）
	 * @param ah2      父系等位基因持有者（可为 null）
	 * @param x        行左上角 X 坐标
	 * @param y        行左上角 Y 坐标
	 * @param mouseX   鼠标相对 X 坐标
	 * @param mouseY   鼠标相对 Y 坐标
	 * @param tooltips 提示文本消费者，用于收集悬停时显示的提示信息
	 * @return 该行占用的高度（像素），用于下一行的 Y 坐标偏移
	 */
	int extractRenderState(GuiGraphicsExtractor graphics, AllelesHolder ah1, AllelesHolder ah2, int x, int y, int mouseX, int mouseY, Consumer<Component> tooltips);


}