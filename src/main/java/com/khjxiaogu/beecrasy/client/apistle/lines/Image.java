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

import com.khjxiaogu.beecrasy.client.apistle.GuiInfoCollector;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public record Image(Identifier image,int width,int height) implements UnbakedLine,Line{
	public static final MapCodec<Image> CODEC=RecordCodecBuilder.mapCodec(t->t.group(

			Identifier.CODEC.fieldOf("image").forGetter(Image::image),
			Codec.INT.fieldOf("width").forGetter(Image::width),
			Codec.INT.fieldOf("height").forGetter(Image::height)
			).apply(t, Image::new));

	@Override
	public Line bake(int width) {
		return this;
	}

	@Override
	public int extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int w, int mouseX, int mouseY,
			GuiInfoCollector tooltips) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, image, x+(w-width)/2, y, 0, 0, width, height, width, height);
		return height;
	}

	@Override
	public int precalculateHeight() {
		return height;
	}

	@Override
	public String type() {
		return "image";
	}

}
