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

import com.khjxiaogu.beecrasy.client.apistle.lines.Line;
import com.khjxiaogu.beecrasy.client.apistle.lines.UnbakedLine;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;

public record Page(List<UnbakedLine> lines,Optional<Either<Identifier,ItemStackTemplate>> icon,String title,int order) {
	public static final Codec<Page> CODEC=RecordCodecBuilder.create(t->t.group(
			UnbakedLine.CODEC.listOf().optionalFieldOf("lines",List.of()).forGetter(Page::lines),
			Codec.either(Identifier.CODEC, ItemStackTemplate.CODEC).optionalFieldOf("icon").forGetter(Page::icon),
			Codec.STRING.fieldOf("title").forGetter(Page::title),
			Codec.INT.fieldOf("order").forGetter(Page::order)
			).apply(t, Page::new));
	public List<Line> bake(int width) {
		return lines.stream().map(t->t.bake(width)).toList();
	}
}
