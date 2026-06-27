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
	public static class PageButton{
		int index;
		Runnable onClick;
		UnbakedPage page;
		PageButton(int index, Runnable onClick, UnbakedPage page) {
			super();
			this.index = index;
			this.onClick = onClick;
			this.page = page;
		}
	}
	public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Beecrasy.MODID, "textures/gui/apistle_backlay.png");

	public static final Identifier TEXTURE_BUTTON = Identifier.fromNamespaceAndPath(Beecrasy.MODID, "textures/gui/apistle_buttons.png");
	
    protected int imageWidth;
    protected int imageHeight;
    protected int leftPos;
    protected int topPos;
    public final int TABS_PER_PAGE=11;
    public final int PAGE_WIDTH=340-38-10-4;
    public final int PAGE_HEIGHT=225-11-11+6;
	/** 当前页面中最大的标签索引 */
	int maxIndex=0;
	/** 当前页面的起始标签索引 */
	int minIndex=0;
	
	int selected=0;
	BakedPage currentPage;
	List<UnbakedPage> pages;
	List<PageButton> buttons=new ArrayList<>(TABS_PER_PAGE);
	TabPageManager tm;
	String modid;
	public ApistleScreen(String modid,Component title) {
		super(title);
		imageWidth=340;
		imageHeight=225;
		this.modid=modid;

	}


	private ArrayList<Component> tooltip = new ArrayList<>(2);

	@Override
	public void init() {
		super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
		this.clearWidgets();
		pages=List.copyOf(PageRegistry.INSTANCE.getPages(modid));
		if(!pages.isEmpty()) {
			currentPage=pages.get(0).bake(PAGE_WIDTH);
		}
		tm=new TabPageManager(pages.size(), TABS_PER_PAGE);
		if(pages.size()>5) {
			maxIndex=4;
		}else {
			maxIndex=pages.size();
		}
		updatePage();
	}
	public void updatePage() {
		buttons.clear();
		for(int i:tm.getDisplayItems()) {
			if(i==TabPageManager.NEXT)
				buttons.add(new PageButton(i,()->{
					tm.pageForward();
					updatePage();
				},null));
			else if(i==TabPageManager.PREV)
				buttons.add(new PageButton(i,()->{
					tm.pageBackward();
					updatePage();
				},null));
			else
				buttons.add(new PageButton(i,()->{
					selected=i;
					currentPage=pages.get(selected).bake(PAGE_WIDTH);
					viewY=0;
				},pages.get(i)));
		}
	}
	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
		tooltip.clear();
		Consumer<Component> adder=tooltip::add;
		for(int i=0;i<buttons.size();i++) {
			
			int dy=11+i*18;
			boolean over=this.isMouseIn(mouseX, mouseY, 10, dy, 18, 15);
			int texY=0;
			
			PageButton button=buttons.get(i);
			if(button.index==TabPageManager.NEXT) {
				texY=30;
			}else if(button.index==TabPageManager.PREV) {
				texY=15;
			}
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_BUTTON, leftPos+10, topPos+dy, (over  ?23:0)   , texY, 18, 15,64,64);
			

			boolean select=button.index==selected;
			if(button.page!=null) {
				button.page.extractIcon(graphics, leftPos+13, topPos+dy+3, 10, 10, mouseX, mouseY, adder, over, select);
				graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_BUTTON, leftPos+28, topPos+dy, (select?23:0)+18, 0,  5, 15,64,64);
			}
		}
		int dx=leftPos+38;
		int dy=topPos+11;
		if(currentPage!=null) {
			graphics.enableScissor(dx, dy, PAGE_WIDTH+dx, PAGE_HEIGHT+dy);
			currentPage.extractRenderState(graphics, dx, dy, PAGE_WIDTH, PAGE_HEIGHT, (int) viewY, PAGE_HEIGHT, mouseX, mouseY, adder);
			graphics.disableScissor();
		}
		//super.extractRenderState(graphics, mouseX, mouseY, partial);
		//graphics.text(this.font, this.title, 8, 6, -12566464, false);
		if (!tooltip.isEmpty()) {
			graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
		}

	}
	float viewY;

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		if(currentPage!=null&&currentPage.height()>PAGE_HEIGHT) {
			if(isMouseIn((int)x,(int)y, 38, 11, PAGE_WIDTH, PAGE_HEIGHT)) {
				viewY-=scrollY*16;
				viewY=Math.max(0, viewY);
				viewY=Math.min(viewY, currentPage.height()- PAGE_HEIGHT);
				return true;
			}
		}
		return super.mouseScrolled(x, y, scrollX, scrollY);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
			int exW=imageWidth-48-27;
			int exH=imageHeight-20-24;
    		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,leftPos+      0,topPos+      0,   0,   0, 48, 24, 256, 256);
    		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,leftPos+      0,topPos+ 24+exH,   0, 100, 48, 27, 256, 256);
    		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,leftPos+ 48+exW,topPos+      0, 125,   0, 20, 24, 256, 256);
    		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,leftPos+ 48+exW,topPos+ 24+exH, 125, 100, 20, 27, 256, 256);

    		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,leftPos+     48,topPos+      0,  48,   0, exW,  24, 76, 24, 256, 256);//top
    		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,leftPos+      0,topPos+     24,   0,  24,  48, exH, 48, 77, 256, 256);//left
    		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,leftPos+     48,topPos+ 24+exH,  48, 100, exW,  27, 76, 27, 256, 256);//bottom
    		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,leftPos+ 48+exW,topPos+     24, 125,  24,  20, exH, 20, 77, 256, 256);//right

    		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,leftPos+ 48,topPos+ 24, 48,  24,  exW, exH, 77, 76, 256, 256);
		if(currentPage!=null)
			currentPage.extractBackground(graphics, leftPos+40, topPos+13, PAGE_WIDTH, PAGE_HEIGHT, (int) viewY, PAGE_HEIGHT, mouseX, mouseY);
	}
	@Override
    public boolean isPauseScreen() {
        return true;
    }
    @Override
    public boolean isInGameUi() {
        return true;
    }


	public boolean isMouseIn(int mouseX, int mouseY, int x, int y, int w, int h) {
		return mouseX >= leftPos + x && mouseY >= topPos + y && mouseX < leftPos + x + w && mouseY < topPos + y + h;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		int mouseX=(int) event.x(),mouseY=(int) event.y();
		if(isMouseIn(mouseX,mouseY,10,13,18,imageHeight-13)) {
			int pos=mouseY-topPos-11;
			int idx=pos/18;
			if(pos%18<=15&&idx>=0&&idx<buttons.size()) {
				buttons.get(idx).onClick.run();
			}
			
		}
		return super.mouseClicked(event, doubleClick);
	}
}
