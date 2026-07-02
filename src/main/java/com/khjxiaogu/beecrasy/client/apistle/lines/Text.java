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
import java.util.Optional;

import com.khjxiaogu.beecrasy.client.apistle.Constants;
import com.khjxiaogu.beecrasy.client.apistle.GuiInfoCollector;
import com.khjxiaogu.beecrasy.utils.StringComponentParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public record Text(List<String> lines,float scale,boolean centered,Optional<Float> lineHeight) implements UnbakedLine{
	public static final MapCodec<Text> CODEC=RecordCodecBuilder.mapCodec(t->t.group(
			ExtraCodecs.compactListCodec(Codec.STRING).fieldOf("text").forGetter(Text::lines),
			Codec.FLOAT.optionalFieldOf("scale",1f).forGetter(Text::scale),
			Codec.BOOL.optionalFieldOf("centered",false).forGetter(Text::centered),
			Codec.FLOAT.optionalFieldOf("lineHeight").forGetter(Text::lineHeight)
			).apply(t, Text::new));
	public Text(List<String> lines,float scale,boolean centered) {
		this(lines,scale,centered,Optional.empty());
	}
	public Text(List<String> lines) {
		this(lines,1f,false,Optional.empty());
	}
	@Override
	public Line bake(int width) {
		Font font=Minecraft.getInstance().font;
		List<FormattedCharSequence> lines=this.lines.stream().flatMap(t->font.split(StringComponentParser.parse(t),(int)(width/scale)).stream()).toList();
		float lineHeight=this.lineHeight.orElse(scale*8+1);
		int height=Mth.ceil(lineHeight*lines.size());
		return new Line() {
			@Override
			public int extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int w, int mouseX, int mouseY,
					GuiInfoCollector tooltips) {
				int initX=x;
				int initY=y;
				int initW=w;
				graphics.pose().pushMatrix();
				if(scale!=1) {
					
					graphics.pose().translate(x, y);
					graphics.pose().scale(scale);
					initX=0;
					initY=0;
					initW=(int)(width/scale);
				}
				for(FormattedCharSequence mc:lines) {
					if(centered)
						graphics.text(font, mc, initX+(initW-font.width(mc))/2, initY, Constants.TEXT_COLOR, false);
					else
						graphics.text(font, mc, initX, initY, Constants.TEXT_COLOR, false);
					graphics.pose().translate(0, lineHeight/scale);
				}
				graphics.pose().popMatrix();
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
