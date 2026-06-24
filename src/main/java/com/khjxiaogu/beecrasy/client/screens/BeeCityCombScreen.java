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

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.client.FluidRenderHelper;
import com.khjxiaogu.beecrasy.menu.BeeCityCombMenu;
import com.khjxiaogu.beecrasy.menu.PressMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * 蜂蜜压榨机 GUI 界面。
 * <p>
 * 扩展 {@link AbstractContainerScreen}<{@link PressMenu}>，显示：
 * <ul>
 *   <li>流体储罐（通过 {@link FluidRenderHelper#handleGuiTank} 绘制）；</li>
 *   <li>进度条——表示当前榨取进度；</li>
 *   <li>物品栏标签。</li>
 * </ul>
 */
public class BeeCityCombScreen extends AbstractContainerScreen<BeeCityCombMenu> {
	/** GUI 背景纹理位置 */
	static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Beecrasy.MODID, "textures/gui/bee_city_comb.png");

	/**
	 * 构造压榨机 GUI 界面。
	 *
	 * @param menu      压榨机菜单容器
	 * @param inventory 玩家物品栏
	 * @param title     界面标题
	 */
	public BeeCityCombScreen(BeeCityCombMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
        this.inventoryLabelY = imageHeight - 92;
	}

	@Override
	public void init() {
		super.init();
		this.clearWidgets();


	}
	@Override
	public void extractRenderState(GuiGraphicsExtractor transform, int mouseX, int mouseY, float partial) {

		super.extractRenderState(transform, mouseX, mouseY, partial);

	}
	@Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, -12566464, false);
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
    }
	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);

		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight,256,256);
		
	}

	/**
	 * 判断鼠标是否在指定区域内。
	 *
	 * @param mouseX 鼠标 X 坐标
	 * @param mouseY 鼠标 Y 坐标
	 * @param x      区域左上角相对 X 坐标
	 * @param y      区域左上角相对 Y 坐标
	 * @param w      区域宽度
	 * @param h      区域高度
	 * @return 若鼠标在区域内返回 {@code true}
	 */
	public boolean isMouseIn(int mouseX, int mouseY, int x, int y, int w, int h) {
		return mouseX >= leftPos + x && mouseY >= topPos + y && mouseX < leftPos + x + w && mouseY < topPos + y + h;
	}
}
