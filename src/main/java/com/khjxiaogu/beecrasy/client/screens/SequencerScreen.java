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
import java.util.function.Consumer;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.client.screens.sequencertabs.SequencerTab;
import com.khjxiaogu.beecrasy.client.screens.sequencertabs.SequencerTabs;
import com.khjxiaogu.beecrasy.menu.SequencerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class SequencerScreen<T extends SequencerMenu> extends AbstractContainerScreen<T> {
	public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Beecrasy.MODID, "textures/gui/sequencer.png");

	public SequencerScreen(T menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, 225);
	}
	int selected=0;
	int maxIndex=0;
	int minIndex=0;
	int page=0;
	private ArrayList<Component> tooltip = new ArrayList<>(2);

	@Override
	public void init() {
		super.init();
		this.clearWidgets();
	}
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial,Consumer<Component> adder) {
		
	}
	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
		tooltip.clear();
		Consumer<Component> adder=tooltip::add;
		minIndex=page*5;
		int tabCount=SequencerTabs.getTabs().size();
		maxIndex=Math.min(5, tabCount-minIndex);
		if(selected>=tabCount) {
			selected=tabCount-1;
		}
		if(selected<0) {
			selected=0;
		}
		SequencerTab selectedTab=SequencerTabs.getTabs().get(selected);
		for(int i=0;i<maxIndex;i++) {
			int idx=i+minIndex;
			SequencerTab tab=SequencerTabs.getTabs().get(idx);
			boolean select=idx==selected;
			boolean over=false;
			if(this.isMouseIn(mouseX, mouseY, 11, 13+i*18, 23, 15)) {
				over=true;
				tab.addButtonTooltip(menu, adder);
			}

			tab.extractButton(graphics, menu, leftPos+11, topPos+13+i*18, 23, 15, over, select);
		}
		int dx=leftPos+40;
		int dy=topPos+13;
		selectedTab.extractRenderState(graphics, menu, dx, dy, 92, 100, mouseX, mouseY, partial, adder);
		super.extractRenderState(graphics, mouseX, mouseY, partial);
		extractRenderState(graphics, mouseX, mouseY, partial,adder);
		if (!tooltip.isEmpty()) {
			graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
		}

	}

	@Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
        //graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, -12566464, false);
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
    }

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		SequencerTab selectedTab=SequencerTabs.getTabs().get(selected);
		
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight,256,256);
		selectedTab.extractBackground(graphics, menu, leftPos+40, topPos+13, 92, 100, mouseX, mouseY, a);
	
		
	}

	public boolean isMouseIn(int mouseX, int mouseY, int x, int y, int w, int h) {
		return mouseX >= leftPos + x && mouseY >= topPos + y && mouseX < leftPos + x + w && mouseY < topPos + y + h;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		int mouseX=(int) event.x(),mouseY=(int) event.y();
		if(isMouseIn(mouseX,mouseY,11,13,23,103)) {
			int pos=mouseY-topPos-13;
			int idx=pos/18;
			if(idx%18<=15) {
				if(idx<maxIndex&&idx>=0)
					selected=idx;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}
}
