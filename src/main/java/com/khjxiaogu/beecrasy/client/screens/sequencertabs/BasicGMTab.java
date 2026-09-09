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
import com.khjxiaogu.beecrasy.menu.GMMenu;
import com.khjxiaogu.beecrasy.menu.SequencerMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 核心基因标签页。
 * <p>
 * 显示被测序物品的基因组基本信息
 * 每项以 {@link SingleLine} 形式展示表现型和母系/父系等位基因数据。
 */
public class BasicGMTab implements GMTab {
	/** 基因显示行列表 */
	List<Line> lines=new ArrayList<>();
	/** 标签页标题 */
	public static final Component title=Component.translatable("tab.beecrasy.basic");
	public BasicGMTab() {
		super();
		addLines();
	}
	/**
	 * 注册所有基础基因行。
	 * <p>
	 * 添加温度、湿度、生态位、繁殖力、寿命、产量六项基因的显示行。
	 */
	public void addLines() {

		lines.add(new SingleGMLine(Genes.TEMPERATURE));
		lines.add(new SingleGMLine(Genes.HUMIDITY));
		lines.add(new SingleGMLine(Genes.BIOTOPE));
		lines.add(new SingleGMLine(Genes.FERTILITY));
		lines.add(new SingleGMLine(Genes.LIFESPAN));
		lines.add(new SingleGMLine(Genes.YIELD));
	}
	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, GMMenu menu, int x, int y, int w, int h, int mouseX, int mouseY, float partial, Consumer<Component> tooltips) {

		GeneticModifier ah1=menu.getGene1();
		GeneticModifier ah2=menu.getGene2();
		if(ah1!=null) {
			int dy=y;
			for(Line l:lines) {
				dy+=l.extractRenderState(graphics, ah1, ah2, x, dy, mouseX-x, mouseY-dy, tooltips)+2;
			}
		}
		
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, GMMenu menu, int x, int y, int w, int h, int mouseX, int mouseY, float partial) {
		
	}

	@Override
	public void extractButton(GuiGraphicsExtractor graphics, GMMenu menu, int x, int y, int w, int h, boolean isOver, boolean isActive) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, SequencerScreen.TEXTURE, x   , y, 176+(isOver  ?23:0)   , 0, 18, h,256,256);
		graphics.blit(RenderPipelines.GUI_TEXTURED, SequencerScreen.TEXTURE, x+18, y, 176+(isActive?23:0)+18, 0,  5, h,256,256);
		
	}

	@Override
	public void addButtonTooltip(GMMenu menu, Consumer<Component> tooltips) {
		tooltips.accept(title);
		
	}
	@Override
	public boolean mouseClicked(int mouseX, int mouseY,int x, int y, int w, int h, GMMenu menu, MouseButtonEvent event, boolean doubleClick) {
		int dy=y;
		GeneticModifier ah1=menu.getGene1();
		GeneticModifier ah2=menu.getGene2();
		if(ah1!=null) {
			for(Line l:lines){
				if(l instanceof InteractiveLine il) {
					if(il.mouseClicked(mouseX-x, mouseY-dy, ah1, ah2, event, doubleClick))
						return true;
				}
				dy+=10;
			}
		}
		return false;
	}

}
