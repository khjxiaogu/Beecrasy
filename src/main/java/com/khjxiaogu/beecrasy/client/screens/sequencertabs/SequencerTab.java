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

import com.khjxiaogu.beecrasy.menu.SequencerMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * 测序仪标签页接口。
 * <p>
 * 定义标签页的四个生命周期方法，由 {@link SequencerScreen} 在渲染时调用：
 * <ul>
 *   <li>{@link #extractRenderState}——渲染标签页内容；</li>
 *   <li>{@link #extractBackground}——渲染标签页背景；</li>
 *   <li>{@link #extractButton}——渲染侧边标签按钮；</li>
 *   <li>{@link #addButtonTooltip}——添加按钮悬停提示。</li>
 * </ul>
 * 实现类需要提供具体的渲染逻辑和提示信息。
 */
public interface SequencerTab {
	
	/**
	 * 渲染标签页的内容区域。
	 *
	 * @param graphics GUI 图形提取器
	 * @param menu     测序仪菜单
	 * @param x        内容区域 X 坐标
	 * @param y        内容区域 Y 坐标
	 * @param w        内容区域宽度
	 * @param h        内容区域高度
	 * @param mouseX   鼠标 X 坐标
	 * @param mouseY   鼠标 Y 坐标
	 * @param partial  部分 tick 时间
	 * @param tooltips 提示文本消费者
	 */
	public void extractRenderState(GuiGraphicsExtractor graphics,SequencerMenu menu,int x,int y,int w,int h,int mouseX, int mouseY, float partial,Consumer<Component> tooltips);

	/**
	 * 渲染标签页的背景区域。
	 *
	 * @param graphics GUI 图形提取器
	 * @param menu     测序仪菜单
	 * @param x        背景区域 X 坐标
	 * @param y        背景区域 Y 坐标
	 * @param w        背景区域宽度
	 * @param h        背景区域高度
	 * @param mouseX   鼠标 X 坐标
	 * @param mouseY   鼠标 Y 坐标
	 * @param partial  部分 tick 时间
	 */
	public void extractBackground(GuiGraphicsExtractor graphics,SequencerMenu menu,int x,int y,int w,int h, int mouseX, int mouseY, float partial);
	
	/**
	 * 渲染左侧的标签按钮。
	 *
	 * @param graphics GUI 图形提取器
	 * @param menu     测序仪菜单
	 * @param x        按钮 X 坐标
	 * @param y        按钮 Y 坐标
	 * @param w        按钮宽度
	 * @param h        按钮高度
	 * @param isOver   鼠标是否悬停
	 * @param isActive 是否是当前选中标签
	 */
	public void extractButton(GuiGraphicsExtractor graphics,SequencerMenu menu,int x,int y,int w,int h,boolean isOver,boolean isActive);

	/**
	 * 添加按钮的悬停提示。
	 *
	 * @param menu     测序仪菜单
	 * @param tooltips 提示文本消费者
	 */
	public void addButtonTooltip(SequencerMenu menu,Consumer<Component> tooltips);

}
