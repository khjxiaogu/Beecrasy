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

package com.khjxiaogu.beecrasy.client.screens;

import java.util.function.IntSupplier;

import com.khjxiaogu.beecrasy.beehive.WorkBehaviour;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * 蜂箱工作模式切换按钮。
 * <p>
 * 继承 {@link Button}，显示当前选中 {@link WorkBehaviour} 对应的纹理图标。
 * 纹理使用大图集索引定位，支持悬停高亮状态（悬停时垂直偏移 16 像素）。
 * 按钮显示关联工作行为的本地化文本作为消息。
 */
public class BeeHiveButton extends Button {
	/** 按钮纹理的标识符 */
	Identifier texture;
	/** 当前工作行为在 {@link WorkBehaviour#values()} 中的索引 */
	IntSupplier slot;
	/** 鼠标是否悬停在该按钮上 */
	boolean isOver;
	/**
	 * 构造工作模式切换按钮。
	 *
	 * @param builder 按钮构建器
	 * @param texture 按钮纹理标识符
	 * @param slot    工作行为索引提供者
	 */
	public BeeHiveButton(Builder builder, Identifier texture, IntSupplier slot) {
		super(builder);
		this.texture = texture;
		this.slot = slot;
	}

	/**
	 * 返回鼠标是否悬停在此按钮上。
	 *
	 * @return 悬停状态
	 */
	public boolean isOver() {
		return isOver;
	}

	/**
	 * 设置鼠标悬停状态。
	 *
	 * @param isOver 悬停状态
	 */
	public void setOver(boolean isOver) {
		this.isOver = isOver;
	}

	/**
	 * 获取按钮显示的文本——当前 {@link WorkBehaviour} 的本地化组件。
	 *
	 * @return 工作行为的本地化 {@link Component}
	 */
	@Override
	public Component getMessage() {
		return WorkBehaviour.values()[slot.getAsInt()].getComponents();
	}

	/**
	 * 渲染按钮纹理。
	 * <p>
	 * 从纹理图集中截取对应工作行为的图标区域（18x16），
	 * 根据悬停状态垂直偏移 16 像素以显示高亮版本。
	 *
	 * @param graphics GUI 图形提取器
	 * @param mouseX   鼠标 X 坐标
	 * @param mouseY   鼠标 Y 坐标
	 * @param a        部分 tick 时间
	 */
	@Override
	protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, super.getX(), super.getY(),
				181+18*slot.getAsInt(), isOver?16:0, 18, 16, 256, 256);

	}

}
