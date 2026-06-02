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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.client.screens.SequencerScreen;
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.genome.Genes;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.menu.SequencerMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class BasicGeneticsTab implements SequencerTab {
	List<Line> lines=new ArrayList<>();
	public static final Component title=Component.translatable("tab.sequencer.beecrasy.basic");
	public BasicGeneticsTab() {
		super();
		addLines();
	}
	public void addLines() {

		lines.add(new SingleLine(Genes.TEMPERATURE));
		lines.add(new SingleLine(Genes.HUMIDITY));
		lines.add(new SingleLine(Genes.BIOTOPE));
		lines.add(new SingleLine(Genes.FERTILITY));
		lines.add(new SingleLine(Genes.LIFESPAN));
		lines.add(new SingleLine(Genes.YIELD));
	}
	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, SequencerMenu menu, int x, int y, int w, int h, int mouseX, int mouseY, float partial, Consumer<Component> tooltips) {
		ItemStack is=menu.getSlot(0).getItem();
		GenomeComponent comp=is.get(Components.GENOME);
		if(comp!=null&&comp.isInspected()) {
			int dy=y;
			Genome ah1=comp.getGenome(0);
			Genome ah2=null;
			if(comp.size()>1)
				ah2=comp.getGenome(1);
			for(Line l:lines) {
				dy+=l.extractRenderState(graphics, ah1, ah2, x, dy, mouseX-x, mouseY-dy, tooltips)+2;
			}
		}
		
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, SequencerMenu menu, int x, int y, int w, int h, int mouseX, int mouseY, float partial) {
		
	}

	@Override
	public void extractButton(GuiGraphicsExtractor graphics, SequencerMenu menu, int x, int y, int w, int h, boolean isOver, boolean isActive) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, SequencerScreen.TEXTURE, x   , y, 176+(isOver  ?23:0)   , 0, 18, h,256,256);
		graphics.blit(RenderPipelines.GUI_TEXTURED, SequencerScreen.TEXTURE, x+18, y, 176+(isActive?23:0)+18, 0,  5, h,256,256);
		
	}

	@Override
	public void addButtonTooltip(SequencerMenu menu, Consumer<Component> tooltips) {
		tooltips.accept(title);
		
	}

}
