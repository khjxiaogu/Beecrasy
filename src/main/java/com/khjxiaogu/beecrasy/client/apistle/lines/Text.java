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

package com.khjxiaogu.beecrasy.client.apistle.lines;

import java.util.List;
import java.util.function.Consumer;

import com.khjxiaogu.beecrasy.utils.StringComponentParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public record Text(List<String> lines,float scale,boolean centered) implements UnbakedLine{
	public static final MapCodec<Text> CODEC=RecordCodecBuilder.mapCodec(t->t.group(
			ExtraCodecs.compactListCodec(Codec.STRING).fieldOf("text").forGetter(Text::lines),
			Codec.FLOAT.fieldOf("scale").forGetter(Text::scale),
			Codec.BOOL.fieldOf("centered").forGetter(Text::centered)
			).apply(t, Text::new));

	@Override
	public Line bake(int width) {
		Font font=Minecraft.getInstance().font;
		List<FormattedCharSequence> lines=this.lines.stream().flatMap(t->font.split(StringComponentParser.parse(t),(int)(width/scale)).stream()).toList();
		int height=Mth.ceil(scale*7*lines.size());
		return new Line() {
			@Override
			public int extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int w, int mouseX, int mouseY,
					Consumer<Component> tooltips) {
				int initX=x;
				int initY=y;
				int initW=w;
				if(scale!=1) {
					graphics.pose().pushMatrix();
					graphics.pose().translate(x, y);
					graphics.pose().scale(scale);
					initX=0;
					initY=0;
					initW=(int)(width/scale);
				}
				int curY=0;
				for(FormattedCharSequence mc:lines) {
					if(centered)
						graphics.text(font, mc, initX+(initW-font.width(mc))/2, initY+curY, 0xff81cfff, false);
					else
						graphics.text(font, mc, initX, initY+curY, 0xff81cfff, false);
					curY+=7;
				}
				if(scale!=1) {
					graphics.pose().popMatrix();
				}
				return height;
			}

			@Override
			public int precalculateHeight() {
				return height;
			}
		};
	}

	@Override
	public String type() {
		return "text";
	}

}
