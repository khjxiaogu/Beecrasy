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

package com.khjxiaogu.beecrasy.client.apistle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.client.screens.SequencerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ApistleScreen extends Screen {
	public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Beecrasy.MODID, "textures/gui/sequencer.png");
    protected int imageWidth;
    protected int imageHeight;
    protected int leftPos;
    protected int topPos;
    public final int TABS_PER_PAGE=5;
    public final int PAGE_WIDTH=100;
	/** 当前页面中最大的标签索引 */
	int maxIndex=0;
	/** 当前页面的起始标签索引 */
	int minIndex=0;
	
	int selected=0;
	BakedPage currentPage;
	List<UnbakedPage> pages;
	public ApistleScreen(String modid,Component title) {
		super(title);
		pages=List.copyOf(PageRegistry.INSTANCE.getPages(modid));
	}


	private ArrayList<Component> tooltip = new ArrayList<>(2);

	@Override
	public void init() {
		super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
		this.clearWidgets();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
		tooltip.clear();
		Consumer<Component> adder=tooltip::add;
		for(int i=0;i<TABS_PER_PAGE;i++) {
			int dy=13+i*18;
			boolean over=this.isMouseIn(mouseX, mouseY, 11, dy, 23, 15);
			graphics.blit(RenderPipelines.GUI_TEXTURED, SequencerScreen.TEXTURE, leftPos+15, topPos+dy, 176+(over  ?23:0)   , 15, 18, 15,256,256);
			
			int idx=i+minIndex;
			if(minIndex>0) {
				if(i==0) {
					//render previous page
					return;
				}
				idx--;
			}
			if(maxIndex<pages.size()-1&&i==TABS_PER_PAGE-1) {
				//render next page
				return;
			}
			UnbakedPage tab=pages.get(idx);
			boolean select=idx==selected;
			graphics.blit(RenderPipelines.GUI_TEXTURED, SequencerScreen.TEXTURE, leftPos+33, topPos+dy, 176+(select?23:0)+18, 15,  5, 15,256,256);
			tab.extractIcon(graphics, leftPos+15, topPos+dy, 15, 15, mouseX, mouseY, adder);
		}
		int dx=leftPos+40;
		int dy=topPos+13;
		currentPage.extractRenderState(graphics, dx, dy, 92, 100, dx, 100, mouseX, mouseY, adder);
		super.extractRenderState(graphics, mouseX, mouseY, partial);
		graphics.text(this.font, this.title, 8, 6, -12566464, false);
		if (!tooltip.isEmpty()) {
			graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
		}

	}


	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight,256,256);
		currentPage.extractBackground(graphics, leftPos+40, topPos+13, 92, 100, 0, 0, mouseX, mouseY);
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
			int idxOff=0;
			if(idx%18<=15) {
				if(minIndex>0&&idx==0) {
					if(minIndex>TABS_PER_PAGE-1) {
						minIndex-=TABS_PER_PAGE+2;
					}else
						minIndex=0;
				}else {
					idxOff=1;
				}
				if(maxIndex<pages.size()-1&&idx==TABS_PER_PAGE-1) {
					minIndex=maxIndex+1;
					maxIndex=Math.min(pages.size()-1, minIndex+TABS_PER_PAGE-1);
				}
				if(idx<TABS_PER_PAGE&&idx>=idxOff) {
					selected=idx+minIndex-idxOff;
					currentPage=pages.get(selected).bake(PAGE_WIDTH);
				}
			}
		}
		return super.mouseClicked(event, doubleClick);
	}
}
