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

import java.util.List;
import java.util.function.Consumer;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.client.screens.SequencerScreen;
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.genome.Genes;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;
import com.khjxiaogu.beecrasy.menu.SequencerMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 产物标签页。
 * <p>
*/
public class ProductsTab implements SequencerTab{
	/** 标签页标题 */
	public static final Component title=Component.translatable("tab.sequencer.beecrasy.products");
	/** 表现型标签（母系） */
	public static final Component PHENO=Component.translatable("genome.beecrasy.genome0");
	/** 基因型标签（父系） */
	public static final Component GENO=Component.translatable("genome.beecrasy.genome1");
	@SuppressWarnings("resource")
	@Override
	public void extractRenderState(GuiGraphicsExtractor transform, SequencerMenu menu, int x, int y, int w, int h, int mouseX, int mouseY, float partial, Consumer<Component> tooltips) {
		ItemStack is=menu.getSlot(0).getItem();
		GenomeComponent comp=is.get(Components.GENOME);
		if(comp!=null&&comp.isInspected()) {
			List<ProductItem> ah1=comp.getGenome(0).getAllele(Genes.PRODUCTS);
			List<ProductItem> ah2=null;
			if(comp.size()>1)
				ah2=comp.getGenome(1).getAllele(Genes.PRODUCTS);
			
			transform.text(Minecraft.getInstance().font, Genes.PRODUCTS.getShortReadableText(), x, y, 0xff81cfff);
			drawSlots(transform,x-1,y+14);
			transform.blit(RenderPipelines.GUI_TEXTURED,SequencerScreen.TEXTURE, x-1, y+16, 176, 150, 17, 14, 256, 256);
			
			//transform.fill(x+40, y,x+92, y+50, 0x99f18186);
			int idx=1;
			for(ProductItem l:ah1) {
				ItemStack stack=l.stack().create();
				int cx=x+0+(idx%5)*19;
				int cy=y+15+19*(idx/5);
				if(mouseX>cx&&mouseY>cy&&mouseX<cx+16&&mouseY<cy+16) {
					Screen.getTooltipFromItem(Minecraft.getInstance(), stack).forEach(tooltips);
					tooltips.accept(PHENO);
				}
				transform.item(stack,cx,cy);
				idx++;
			}
			idx=1;
			if(ah2!=null) {
				//transform.fill(x+40, y+50,x+92, y+100, 0x99b45ba4);
				drawSlots(transform,x-1,y+59);
				transform.blit(RenderPipelines.GUI_TEXTURED,SequencerScreen.TEXTURE, x-1, y+61, 176, 164, 17, 14, 256, 256);
				
				for(ProductItem l:ah2) {
					ItemStack stack=l.stack().create();
					int cx=x+0+(idx%5)*19;
					int cy=y+60+19*(idx/5);
					if(mouseX>cx&&mouseY>cy&&mouseX<cx+16&&mouseY<cy+16) {
						Screen.getTooltipFromItem(Minecraft.getInstance(), stack).forEach(tooltips);
						tooltips.accept(GENO);
					}
					transform.item(stack,cx,cy);
					idx++;
				}
			}
		}
		
	}
	private static void drawSlots(GuiGraphicsExtractor graphics, int x, int y) {
		for(int i=1;i<10;i++) {
			graphics.blit(RenderPipelines.GUI_TEXTURED,SequencerScreen.TEXTURE, x+(i%5)*19, y+(i/5)*19, 176, 132, 18, 18, 256, 256);
		}
	}
	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, SequencerMenu menu, int x, int y, int w, int h, int mouseX, int mouseY, float partial) {
		
	}

	@Override
	public void extractButton(GuiGraphicsExtractor graphics, SequencerMenu menu, int x, int y, int w, int h, boolean isOver, boolean isActive) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, SequencerScreen.TEXTURE, x   , y, 176+(isOver  ?23:0)   , 15, 18, h,256,256);
		graphics.blit(RenderPipelines.GUI_TEXTURED, SequencerScreen.TEXTURE, x+18, y, 176+(isActive?23:0)+18, 15,  5, h,256,256);
	}

	@Override
	public void addButtonTooltip(SequencerMenu menu, Consumer<Component> tooltips) {
		tooltips.accept(title);
		
	}

}
