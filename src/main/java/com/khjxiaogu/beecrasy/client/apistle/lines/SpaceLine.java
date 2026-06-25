package com.khjxiaogu.beecrasy.client.apistle.lines;

import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public record SpaceLine(int height) implements Line, UnbakedLine {
	public static final MapCodec<SpaceLine> CODEC=RecordCodecBuilder.mapCodec(t->t.group(
			Codec.INT.fieldOf("height").forGetter(SpaceLine::height)
			).apply(t, SpaceLine::new));
	@Override
	public Line bake(int width) {
		return this;
	}

	@Override
	public int extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int w, int mouseX, int mouseY,
			Consumer<Component> tooltips) {
		return height;
	}

	@Override
	public int precalculateHeight() {
		return height;
	}

	@Override
	public String type() {
		return "space";
	}

}
