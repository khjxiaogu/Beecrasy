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

public class ProductsTab implements SequencerTab{
	public static final Component title=Component.translatable("tab.sequencer.beecrasy.products");
	@Override
	public void extractRenderState(GuiGraphicsExtractor transform, SequencerMenu menu, int x, int y, int w, int h, int mouseX, int mouseY, float partial, Consumer<Component> tooltips) {
		ItemStack is=menu.getSlot(0).getItem();
		GenomeComponent comp=is.get(Components.GENOME);
		if(comp!=null&&comp.isInspected()) {
			int dy=y;
			List<ProductItem> ah1=comp.getGenome(0).getAllele(Genes.PRODUCTS);
			List<ProductItem> ah2=null;
			if(comp.size()>1)
				ah2=comp.getGenome(1).getAllele(Genes.PRODUCTS);
			int idx=0;
			transform.text(Minecraft.getInstance().font, Genes.PRODUCTS.getShortReadableText(), x, y, 0xff81cfff);
			transform.fill(x+40, y,x+92, y+50, 0x99f18186);
			for(ProductItem l:ah1) {
				ItemStack stack=l.stack().create();
				int cx=x+40+(idx%3)*16;
				int cy=y+16*(idx/3);
				if(mouseX>cx&&mouseY>cy&&mouseX<cx+16&&mouseY<cy+16)
					Screen.getTooltipFromItem(Minecraft.getInstance(), stack).forEach(tooltips);
				transform.item(stack,cx,cy);
				idx++;
			}
			idx=0;
			if(ah2!=null) {
				transform.fill(x+40, y+50,x+92, y+100, 0x99b45ba4);
				for(ProductItem l:ah2) {
					ItemStack stack=l.stack().create();
					int cx=x+40+(idx%3)*16;
					int cy=y+50+16*(idx/3);
					if(mouseX>cx&&mouseY>cy&&mouseX<cx+16&&mouseY<cy+16)
						Screen.getTooltipFromItem(Minecraft.getInstance(), stack).forEach(tooltips);
					transform.item(stack,cx,cy);
					idx++;
				}
			}
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
