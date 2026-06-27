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

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.khjxiaogu.beecrasy.client.apistle.lines.Line;
import com.khjxiaogu.beecrasy.client.apistle.lines.UnbakedLine;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;

public record Page(List<UnbakedLine> lines,Optional<Either<Identifier,ItemStackTemplate>> icon,String title,int order) implements UnbakedPage{
	public static final Codec<Page> CODEC=RecordCodecBuilder.create(t->t.group(
			UnbakedLine.CODEC.listOf().optionalFieldOf("lines",List.of()).forGetter(Page::lines),
			Codec.either(Identifier.CODEC, ItemStackTemplate.CODEC).optionalFieldOf("icon").forGetter(Page::icon),
			Codec.STRING.fieldOf("title").forGetter(Page::title),
			Codec.INT.optionalFieldOf("order",0).forGetter(Page::order)
			).apply(t, Page::new));
	public Baked bake(int width) {
		List<HeightedLine> line= lines.stream().map(t->new HeightedLine(t.bake(width))).toList();
		int height=0;
		int curHeight=0;
		for(HeightedLine l:line) {
			curHeight+=l.precalculateHeight();
			height=Math.max(height, curHeight);
		}
		return new Baked(line,height);
	}
	private static class HeightedLine implements Line{
		Line line;
		int height;
		public HeightedLine(Line line) {
			super();
			this.line = line;
			height=line.precalculateHeight();
		}
		@Override
		public int extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int w, int mouseX, int mouseY, Consumer<Component> tooltips) {
			height=line.extractRenderState(graphics, x, y, w, mouseX, mouseY, tooltips);
			return height;
		}
		@Override
		public int precalculateHeight() {
			return height;
		}
	}
	public static record Baked(List<HeightedLine> lines,int height) implements BakedPage{

		@Override
		public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int viewY, int viewHeight, int mouseX, int mouseY, Consumer<Component> tooltips) {
			int curY=0;
			for(Line l:lines) {
				int preCurY=curY;
				curY+=l.precalculateHeight();
				//graphics.fill(x, preCurY+y, x+1*idx, curY+y, 0xffff0000);
				if(curY>=viewY&&preCurY<=viewY+viewHeight) {
					l.extractRenderState(graphics, x, preCurY+y-viewY, w, mouseX, mouseY, tooltips);
				}
				
			}
		}

	}
	@Override
	public void extractIcon(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int mouseX, int mouseY, Consumer<Component> tooltips, boolean over, boolean active) {
		if(icon.isPresent()) {
			if(icon.get().left().isPresent()) {
				graphics.blit(RenderPipelines.GUI_TEXTURED, icon.get().left().get(), x, y, 0, 0, w, h, w, h);
			}else {
				graphics.pose().pushMatrix();
				graphics.pose().translate(x, y);
				graphics.enableScissor(0, 0, w, h);
				graphics.pose().scale(w/16f,h/16f);
				graphics.item(icon.get().right().get().create(), 0, 0);
				graphics.pose().popMatrix();
				graphics.disableScissor();
			}
		}
		if(over) {
			tooltips.accept(Component.literal(title));
		}
	}
}
