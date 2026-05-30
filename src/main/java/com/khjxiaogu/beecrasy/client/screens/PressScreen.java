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
import com.khjxiaogu.beecrasy.client.FluidRenderHelper;
import com.khjxiaogu.beecrasy.menu.PressMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class PressScreen extends AbstractContainerScreen<PressMenu> {
	static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Beecrasy.MODID, "textures/gui/honey_press.png");

	public PressScreen(PressMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
        this.inventoryLabelY = imageHeight - 92;
	}
	private ArrayList<Component> tooltip = new ArrayList<>(2);

	@Override
	public void init() {
		super.init();
		this.clearWidgets();


	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor transform, int mouseX, int mouseY, float partial) {
		tooltip.clear();
		
		super.extractRenderState(transform, mouseX, mouseY, partial);

		FluidRenderHelper.handleGuiTank(transform, menu, 0, leftPos + 91, topPos + 9, 17, 34,mouseX,mouseY,tooltip::add);
		if (!tooltip.isEmpty()) {
			transform.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
		}

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
		int process=menu.data.get(0);
		int processMax=menu.data.get(1);
		if (processMax > 0&&process>0) {
			int w = 43-(int) (43 * (process / (float) processMax));
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 73, topPos + 46, 176, 0, w, 23,256,256);
		}
		
	}

	public boolean isMouseIn(int mouseX, int mouseY, int x, int y, int w, int h) {
		return mouseX >= leftPos + x && mouseY >= topPos + y && mouseX < leftPos + x + w && mouseY < topPos + y + h;
	}
}
