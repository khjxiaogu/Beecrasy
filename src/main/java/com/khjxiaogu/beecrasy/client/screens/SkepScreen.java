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

public class SkepScreen extends AbstractContainerScreen<SkepMenu> {
	static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Beecrasy.MODID, "textures/gui/skep.png");

	public SkepScreen(SkepMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);

        this.inventoryLabelY = imageHeight - 92;
	}
	private ArrayList<Component> tooltip = new ArrayList<>(2);
	BeeHiveButton btn;

	@Override
	public void init() {
		super.init();
		this.clearWidgets();
		this.addRenderableWidget(btn = new BeeHiveButton(Button.builder(Component.empty(), _ -> {
			menu.cycleWork();
		}).pos(leftPos + 151, topPos + 2).size(18, 16),TEXTURE,()->menu.getWorkBehaviour().ordinal()));


	}

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

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight,256,256);
		ErrCode errCode=menu.getErrCode();
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos-20, topPos+22, errCode.ordinal()*26, 164, 26, 29,256,256);
	
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
