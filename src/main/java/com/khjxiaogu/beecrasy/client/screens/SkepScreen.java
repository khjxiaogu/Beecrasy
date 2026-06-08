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

import java.util.ArrayList;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.beehive.ErrCode;
import com.khjxiaogu.beecrasy.menu.SkepMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * 草编蜂箱（Skep）GUI 界面。
 * <p>
 * 扩展 {@link AbstractContainerScreen}<{@link SkepMenu}>，显示：
 * <ul>
 *   <li><b>工作模式切换按钮</b>——通过 {@link BeeHiveButton} 切换蜂箱的工作模式；</li>
 *   <li><b>错误代码图标</b>——在指定位置显示当前错误码对应的纹理图标；</li>
 *   <li><b>进度条动画</b>——表示当前工作进度；</li>
 *   <li>物品栏标签。</li>
 * </ul>
 */
public class SkepScreen extends AbstractContainerScreen<SkepMenu> {
	/** GUI 背景纹理位置 */
	static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Beecrasy.MODID, "textures/gui/skep.png");

	public SkepScreen(SkepMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);

        this.inventoryLabelY = imageHeight - 92;
	}
	/** 临时提示文本列表 */
	private ArrayList<Component> tooltip = new ArrayList<>(2);
	/** 工作模式切换按钮实例 */
	BeeHiveButton btn;

	/**
	 * 初始化界面——创建工作模式切换按钮。
	 * <p>
	 * 按钮点击时调用 {@link SkepMenu#cycleWork()} 切换工作模式。
	 */
	@Override
	public void init() {
		super.init();
		this.clearWidgets();
		this.addRenderableWidget(btn = new BeeHiveButton(Button.builder(Component.empty(), _ -> {
			menu.cycleWork();
		}).pos(leftPos + 151, topPos + 2).size(18, 16),TEXTURE,()->menu.getWorkBehaviour().ordinal()));


	}

	/**
	 * 提取渲染状态——处理按钮悬停和错误码提示。
	 * <p>
	 * 判断鼠标是否悬停在工作模式按钮或错误码图标上，
	 * 收集对应的提示文本并通过
	 * {@link GuiGraphicsExtractor#setComponentTooltipForNextFrame} 提交。
	 *
	 * @param transform GUI 图形提取器
	 * @param mouseX    鼠标 X 坐标
	 * @param mouseY    鼠标 Y 坐标
	 * @param partial   部分 tick 时间
	 */
	@Override
	public void extractRenderState(GuiGraphicsExtractor transform, int mouseX, int mouseY, float partial) {
		tooltip.clear();
		btn.setOver(this.isMouseIn(mouseX, mouseY, 151, 2, btn.getWidth(), btn.getHeight()));
		if(btn.isOver) {
			tooltip.add(btn.getMessage());
		}
		if(this.isMouseIn(mouseX, mouseY, -20, 22, 26, 29)) {
			ErrCode errCode=menu.getErrCode();
			tooltip.add(errCode.getComponents());
		}
		super.extractRenderState(transform, mouseX, mouseY, partial);
		if (!tooltip.isEmpty()) {
			transform.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
		}

	}

	@Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, -12566464, false);
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
    }

	/**
	 * 提取背景——绘制主纹理、错误码图标和进度条。
	 * <p>
	 * 进度条从底部向上填充，高度根据 {@code process / processMax} 比例计算。
	 *
	 * @param graphics GUI 图形提取器
	 * @param mouseX   鼠标 X 坐标
	 * @param mouseY   鼠标 Y 坐标
	 * @param a        部分 tick 时间
	 */
	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight,256,256);
		ErrCode errCode=menu.getErrCode();
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos-20, topPos+22,( errCode.ordinal()%9)*26, 164+(errCode.ordinal()/9)*29, 26, 29,256,256);
	
		int process=menu.getProcess();
		int processMax=menu.getProcessMax();
		if (processMax > 0&&process>0) {
			int h = 30-(int) (30 * (process / (float) processMax));
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 133, topPos + 25 + h, 177, 0 + h, 4, 30 - h,256,256);
		}
		
	}

	public boolean isMouseIn(int mouseX, int mouseY, int x, int y, int w, int h) {
		return mouseX >= leftPos + x && mouseY >= topPos + y && mouseX < leftPos + x + w && mouseY < topPos + y + h;
	}

}
