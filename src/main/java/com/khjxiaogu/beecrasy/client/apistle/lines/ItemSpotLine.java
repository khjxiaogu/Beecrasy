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

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public record ItemSpotLine(List<ItemStackTemplate> items,float scale) implements UnbakedLine{
	public static final MapCodec<ItemSpotLine> CODEC=RecordCodecBuilder.mapCodec(t->t.group(
			ExtraCodecs.compactListCodec(ItemStackTemplate.CODEC).fieldOf("items").forGetter(ItemSpotLine::items),
			Codec.FLOAT.fieldOf("scale").forGetter(ItemSpotLine::scale)
			).apply(t, ItemSpotLine::new));


	@Override
	public Line bake(int width) {
		int size=Mth.ceil(scale*16);
		List<ItemStack> item=items.stream().map(ItemStackTemplate::create).toList();
		return new Line() {
			@Override
			public int extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int w, int mouseX, int mouseY,
					Consumer<Component> tooltips) {
			
				graphics.pose().pushMatrix();
				graphics.pose().translate(x+(w-size)/2, y);
				graphics.pose().scale(scale);
				graphics.item(item.get((int) ((System.currentTimeMillis()/500)%item.size())), 0, 0);
				graphics.pose().popMatrix();
				
				
				return size;
			}

			@Override
			public int precalculateHeight() {
				return size;
			}
		};
	}


	@Override
	public String type() {
		return "item";
	}

}
