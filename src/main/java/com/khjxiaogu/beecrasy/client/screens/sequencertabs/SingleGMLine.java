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

import java.util.function.Consumer;

import com.khjxiaogu.beecrasy.genome.AllelesHolder;
import com.khjxiaogu.beecrasy.genome.Gene;
import com.khjxiaogu.beecrasy.genome.GeneRegistry;
import com.khjxiaogu.beecrasy.genome.gene.Allele;
import com.khjxiaogu.beecrasy.genome.gene.EnumAlleleType;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * 单基因显示行记录类。
 * <p>
 * 实现 {@link Line} 接口，在 GUI 中显示单行基因信息，分为三列：
 * <ul>
 *   <li><b>左侧</b>（青色）——基因名称；</li>
 *   <li><b>中间</b>（红色）——母系等位基因的表现型数据；</li>
 *   <li><b>右侧</b>（紫色）——父系等位基因的基因型数据（若存在）。</li>
 * </ul>
 * 支持三区域鼠标悬停提示：基因说明、表现型详情、基因型详情。
 *
 * @param gene 当前行显示的基因
 */
public record SingleGMLine(Gene<?> gene) implements InteractiveLine {
	/** 表现型标签（母系） */
	public static final Component PHENO=Component.translatable("genome.beecrasy.genome0");
	/** 基因型标签（父系） */
	public static final Component GENO=Component.translatable("genome.beecrasy.genome1");

	public static final Component CYCLE_TAB=Component.translatable("gui.beecrasy.synthesizer.cycle_alleles");
	/**
	 * 渲染单行基因信息。
	 * <p>
	 * 绘制三列文本（名称、母系数据、父系数据），行高固定为 8px。
	 * 根据鼠标相对位置提供不同的悬停提示：
	 * <ul>
	 *   <li>X &lt; 42：基因说明；</li>
	 *   <li>42 &le; X &lt; 67：母系表现型说明；</li>
	 *   <li>X &ge; 67：父系基因型说明。</li>
	 * </ul>
	 *
	 * @param graphics GUI 图形提取器
	 * @param ah1      母系等位基因持有者
	 * @param ah2      父系等位基因持有者（可为 null）
	 * @param x        行左上角 X 坐标
	 * @param y        行左上角 Y 坐标
	 * @param mouseX   鼠标相对 X 坐标
	 * @param mouseY   鼠标相对 Y 坐标
	 * @param tooltips 提示文本消费者
	 * @return 固定返回 8（行高像素）
	 */
	@SuppressWarnings("resource")
	@Override
	public int extractRenderState(GuiGraphicsExtractor graphics,AllelesHolder ah1,AllelesHolder ah2,int x,int y,int mouseX, int mouseY, Consumer<Component> tooltips) {
		graphics.text(Minecraft.getInstance().font, gene.getShortReadableText(), x, y, 0xff81cfff);
		graphics.text(Minecraft.getInstance().font, gene.getShortReadableText(ah1), x+42, y, 0xfff18186);
		if(ah2!=null)
			graphics.text(Minecraft.getInstance().font, gene.getShortReadableText(ah2), x+67, y, 0xffb45ba4);
		if(mouseY<8&&mouseY>0&&mouseX>0&&mouseX<92) {
			if(mouseX<42) {
				tooltips.accept(gene.getReadableText());
			}else if(mouseX<67) {
				tooltips.accept(gene.getReadableText(ah1));
				tooltips.accept(PHENO);
				tooltips.accept(CYCLE_TAB);
			}else if(ah2!=null) {
				tooltips.accept(gene.getReadableText(ah2));
				tooltips.accept(GENO);
				tooltips.accept(CYCLE_TAB);
			}
		}
		return 8;
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, GeneticModifier ah1, GeneticModifier ah2, MouseButtonEvent event, boolean doubleClick) {
		if(mouseY<8&&mouseY>0&&mouseX>0&&mouseX<92) {
			if(mouseX<42) {
			}else if(mouseX<67) {
				EnumAlleleType alleles=GeneRegistry.getEnumTypes().get(gene.id());
				Object origin=ah1.getAllele(gene);
				int num=alleles.getIntId((Allele)origin);
				if(event.button()==InputConstants.MOUSE_BUTTON_RIGHT) {
					num--;
				}else if(event.button()==InputConstants.MOUSE_BUTTON_LEFT) {
					num++;
				}
				if(num<0) {
					num+=alleles.size();
				}
				num=num%alleles.size();
				
				ah1.setAllele((Gene)gene, alleles.getByInt(num));
				return true;
			}else if(ah2!=null) {
				EnumAlleleType alleles=GeneRegistry.getEnumTypes().get(gene.id());
				Object origin=ah2.getAllele(gene);
				int num=alleles.getIntId((Allele) origin);
				if(event.button()==InputConstants.MOUSE_BUTTON_RIGHT) {
					num--;
				}else if(event.button()==InputConstants.MOUSE_BUTTON_LEFT) {
					num++;
				}
				if(num<0) {
					num+=alleles.size();
				}
				num=num%alleles.size();
				ah2.setAllele((Gene)gene, alleles.getByInt(num));
				return true;
			}
		}
		return false;
	}
}