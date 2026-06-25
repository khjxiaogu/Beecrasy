package com.khjxiaogu.beecrasy.client.apistle.lines;

import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public record HLine(int color) implements Line, UnbakedLine {
	public static final MapCodec<HLine> CODEC=RecordCodecBuilder.mapCodec(t->t.group(
			Codec.INT.fieldOf("color").forGetter(HLine::color)
			).apply(t, HLine::new));
	@Override
	public Line bake(int width) {
		return this;
	}

	@Override
	public int extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int w, int mouseX, int mouseY,
			Consumer<Component> tooltips) {
		graphics.fill(x, y+1, x+w-4, y+2, color);
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
