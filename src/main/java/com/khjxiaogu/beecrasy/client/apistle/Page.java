package com.khjxiaogu.beecrasy.client.apistle;

import java.util.List;

import com.khjxiaogu.beecrasy.client.apistle.lines.UnbakedLine;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Page(List<UnbakedLine> lines) {
	public static final Codec<Page> pages=RecordCodecBuilder.create(t->t.group(
			UnbakedLine.CODEC.listOf().fieldOf("lines").forGetter(Page::lines)
			).apply(t, Page::new));
}
