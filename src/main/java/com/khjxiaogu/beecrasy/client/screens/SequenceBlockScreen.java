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

import java.util.function.Consumer;

import com.khjxiaogu.beecrasy.blocks.SequencerBlockEntity;
import com.khjxiaogu.beecrasy.client.FluidRenderHelper;
import com.khjxiaogu.beecrasy.menu.SequencerMenuBlock;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 测序仪方块 GUI 界面。
 * <p>
 * 继承 {@link SequencerScreen}<{@link SequencerMenuBlock}>，额外显示以下元素：
 * <ul>
 *   <li><b>流体储罐</b>——通过 {@link FluidRenderHelper#handleGuiTank} 绘制；</li>
 *   <li><b>能量条</b>——显示当前能量值，能量充足时图标切换颜色。</li>
 * </ul>
 * 覆盖 {@link #extractBackground} 和 {@link #extractRenderState} 以添加方块特有的元素。
 */
public class SequenceBlockScreen extends SequencerScreen<SequencerMenuBlock> {

	/**
	 * 构造测序仪方块 GUI。
	 *
	 * @param menu      测序仪方块菜单
	 * @param inventory 玩家物品栏
	 * @param title     界面标题
	 */
	public SequenceBlockScreen(SequencerMenuBlock menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	/**
	 * 提取背景——在主背景之上绘制储罐背景和能量指示器。
	 * <p>
	 * 能量充足时（{@code energy >= workEnergy}）显示高亮图标，否则显示灰色图标。
	 *
	 * @param graphics GUI 图形提取器
	 * @param mouseX   鼠标 X 坐标
	 * @param mouseY   鼠标 Y 坐标
	 * @param a        部分 tick 时间
	 */
	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos+142, topPos+85, 176, 90, 32, 42,256,256);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos+11, topPos+113, (menu.getEnergy()>=menu.getWorkEnergy()?199:176), 45, 12, 8,256,256);
		
	}

	/**
	 * 提取渲染状态——绘制流体储罐和能量提示。
	 * <p>
	 * 调用 {@link FluidRenderHelper#handleGuiTank} 绘制储罐内流体，
	 * 并检查鼠标是否悬停在能量图标上以显示 FE 数值提示。
	 *
	 * @param graphics GUI 图形提取器
	 * @param mouseX   鼠标 X 坐标
	 * @param mouseY   鼠标 Y 坐标
	 * @param partial  部分 tick 时间
	 * @param adder    提示文本消费者，用于收集悬停提示
	 */
	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial,Consumer<Component> adder) {
		FluidRenderHelper.handleGuiTank(graphics, menu, 0, leftPos+152, topPos+89, 16, 34, mouseX, mouseY, adder);
		if(super.isMouseIn(mouseX, mouseY, 11, 113, 12, 8)) {
			adder.accept(Component.literal(menu.getEnergy()+"/"+(menu.getWorkEnergy()*SequencerBlockEntity.ENERGY_BUFF)+" FE"));
		}
	}

	

}
