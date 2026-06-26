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

import java.util.function.Consumer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;

public record HLine(TextColor color) implements Line, UnbakedLine {
	public static final MapCodec<HLine> CODEC=RecordCodecBuilder.mapCodec(t->t.group(
		TextColor.CODEC.optionalFieldOf("color",TextColor.fromRgb(0x81cfff)).forGetter(HLine::color)
			).apply(t, HLine::new));
	public static final HLine DEFAULT=new HLine(TextColor.fromRgb(0x81cfff));
	public HLine(int color) {
		this(TextColor.fromRgb(color));
	}
	public HLine(String color) {
		this(TextColor.parseColor(color).getOrThrow());
	}
	@Override
	public Line bake(int width) {
		return this;
	}

	@Override
	public int extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int w, int mouseX, int mouseY,
			Consumer<Component> tooltips) {
		graphics.fill(x, y+1, x+w-4, y+2, ARGB.color(1f, color.getValue()));
		return 3;
	}

	@Override
	public int precalculateHeight() {
		return 3;
	}

	@Override
	public String type() {
		return "hr";
	}

}
