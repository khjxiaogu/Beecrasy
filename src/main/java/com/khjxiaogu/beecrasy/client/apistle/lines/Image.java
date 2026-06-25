package com.khjxiaogu.beecrasy.client.apistle.lines;

import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public record Image(Identifier image,int width,int height) implements UnbakedLine,Line{
	public static final MapCodec<Image> CODEC=RecordCodecBuilder.mapCodec(t->t.group(

			Identifier.CODEC.fieldOf("color").forGetter(Image::image),
			Codec.INT.fieldOf("width").forGetter(Image::width),
			Codec.INT.fieldOf("height").forGetter(Image::height)
			).apply(t, Image::new));

	@Override
	public Line bake(int width) {
		return this;
	}

	@Override
	public int extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int w, int mouseX, int mouseY,
			Consumer<Component> tooltips) {
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
