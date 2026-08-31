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
import java.util.Optional;
import java.util.function.Consumer;

import com.khjxiaogu.beecrasy.client.screens.SequencerScreen;
import com.khjxiaogu.beecrasy.genome.Genes;
import com.khjxiaogu.beecrasy.genome.gene.Biotope;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;
import com.khjxiaogu.beecrasy.menu.GMMenu;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * 产物标签页。
 * <p>
*/
public class ProductsGMTab implements GMTab{
	/** 标签页标题 */
	public static final Component title=Component.translatable("tab.sequencer.beecrasy.products");
	/** 表现型标签（母系） */
	public static final Component PHENO=Component.translatable("genome.beecrasy.genome0");
	/** 基因型标签（父系） */
	public static final Component GENO=Component.translatable("genome.beecrasy.genome1");
	@SuppressWarnings("resource")
	@Override
	public void extractRenderState(GuiGraphicsExtractor transform, GMMenu menu, int x, int y, int w, int h, int mouseX, int mouseY, float partial, Consumer<Component> tooltips) {

		GeneticModifier ahm1=menu.getGene1();
		GeneticModifier ahm2=menu.getGene2();
		if(ahm1!=null) {
			List<ProductItem> ah1=ahm1.getAllele(Genes.PRODUCTS);
			List<ProductItem> ah2=null;
			if(ahm2!=null)
				ah2=ahm2.getAllele(Genes.PRODUCTS);
			
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
					tooltips.accept(Genes.BIOTOPE.getReadableText(l.biotope()));
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
						tooltips.accept(Genes.BIOTOPE.getReadableText(l.biotope()));
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
	public void extractBackground(GuiGraphicsExtractor graphics, GMMenu menu, int x, int y, int w, int h, int mouseX, int mouseY, float partial) {
		
	}

	@Override
	public void extractButton(GuiGraphicsExtractor graphics, GMMenu menu, int x, int y, int w, int h, boolean isOver, boolean isActive) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, SequencerScreen.TEXTURE, x   , y, 176+(isOver  ?23:0)   , 15, 18, h,256,256);
		graphics.blit(RenderPipelines.GUI_TEXTURED, SequencerScreen.TEXTURE, x+18, y, 176+(isActive?23:0)+18, 15,  5, h,256,256);
	}

	@Override
	public void addButtonTooltip(GMMenu menu, Consumer<Component> tooltips) {
		tooltips.accept(title);
		
	}
	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int x, int y, int w, int h, GMMenu menu, MouseButtonEvent event, boolean doubleClick) {

		//transform.fill(x+40, y,x+92, y+50, 0x99f18186);
		GeneticModifier ahm1=menu.getGene1();
		GeneticModifier ahm2=menu.getGene2();
		if(ahm1!=null) {
			List<ProductItem> ah=ahm1.getAllele(Genes.PRODUCTS);
			boolean modified=false;
			for(int idx=1;idx<=9;idx++) {
				int cx=x+0+(idx%5)*19;
				int cy=y+15+19*(idx/5);
				if(mouseX>cx&&mouseY>cy&&mouseX<cx+16&&mouseY<cy+16) {
					ah=new ArrayList<>(ah);
					System.out.println(idx);
					if(idx-1>=ah.size()) {
						if(!menu.getCarried().isEmpty()&&event.button()==InputConstants.MOUSE_BUTTON_LEFT)
							ah.add(new ProductItem(Genes.Alleles.WILD,Optional.empty(),ItemStackTemplate.fromNonEmptyStack(menu.getCarried()).withCount(1)));
					}else {
						if(!menu.getCarried().isEmpty()&&event.button()==InputConstants.MOUSE_BUTTON_LEFT) {
							ah.set(idx-1,new ProductItem(Genes.Alleles.WILD,Optional.empty(),ItemStackTemplate.fromNonEmptyStack(menu.getCarried()).withCount(1)));
						}else if(event.button()==InputConstants.MOUSE_BUTTON_MIDDLE) {
							ProductItem bt=ah.get(idx-1);
							Biotope biotope;
							if(bt.biotope()==Genes.Alleles.WILD) {
								biotope=Genes.Alleles.CRAFT;
							}else if(bt.biotope()==Genes.Alleles.CRAFT) {
								biotope=Genes.Alleles.SMELT;
							}else {
								biotope=Genes.Alleles.WILD;
							}
							ah.set(idx-1,new ProductItem(biotope,bt.recipe(),bt.stack()));
						}else {
							ah.remove(idx-1);
						}
					}
					modified=true;
					break;
				}
			}
			if(modified) {
				menu.getGene1().setAllele(Genes.PRODUCTS, ah);
				return true;
			}
			if(ahm2!=null) {
				ah=ahm2.getAllele(Genes.PRODUCTS);
				for(int idx=1;idx<=9;idx++) {
					int cx=x+0+(idx%5)*19;
					int cy=y+15+19*(idx/5);
					ah=new ArrayList<>(ah);
					if(mouseX>cx&&mouseY>cy&&mouseX<cx+16&&mouseY<cy+16) {
						if(idx-1>=ah.size()) {
							if(!menu.getCarried().isEmpty()&&event.button()==InputConstants.MOUSE_BUTTON_LEFT)
								ah.add(new ProductItem(Genes.Alleles.WILD,Optional.empty(),ItemStackTemplate.fromNonEmptyStack(menu.getCarried()).withCount(1)));
						}else {
							if(!menu.getCarried().isEmpty()&&event.button()==InputConstants.MOUSE_BUTTON_LEFT) {
								ah.set(idx-1,new ProductItem(Genes.Alleles.WILD,Optional.empty(),ItemStackTemplate.fromNonEmptyStack(menu.getCarried()).withCount(1)));
							}else if(event.button()==InputConstants.MOUSE_BUTTON_MIDDLE) {
								ProductItem bt=ah.get(idx-1);
								Biotope biotope;
								if(bt.biotope()==Genes.Alleles.WILD) {
									biotope=Genes.Alleles.CRAFT;
								}else if(bt.biotope()==Genes.Alleles.CRAFT) {
									biotope=Genes.Alleles.SMELT;
								}else {
									biotope=Genes.Alleles.WILD;
								}
								ah.set(idx-1,new ProductItem(biotope,bt.recipe(),bt.stack()));
							}else {
								ah.remove(idx-1);
							}
						}
						modified=true;
						break;
					}
				}
				if(modified) {
					menu.getGene2().setAllele(Genes.PRODUCTS, ah);
					return true;
				}
			}
		}
		return false;
	}

}
