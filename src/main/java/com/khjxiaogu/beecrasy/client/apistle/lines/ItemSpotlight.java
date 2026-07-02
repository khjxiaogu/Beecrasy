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

import com.khjxiaogu.beecrasy.client.apistle.GuiInfoCollector;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.HolderSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;

public record ItemSpotlight(List<Either<HolderSet<Item>,List<ItemStackTemplate>>> items,float scale) implements UnbakedLine{
	private static final Codec<Either<HolderSet<Item>,List<ItemStackTemplate>>> ICON_CODEC=Codec.either(Ingredient.NON_AIR_HOLDER_SET_CODEC,ExtraCodecs.nonEmptyList(ExtraCodecs.compactListCodec(ItemStackTemplate.CODEC)));
	public static final MapCodec<ItemSpotlight> CODEC=RecordCodecBuilder.mapCodec(t->t.group(
			Codec.mapEither(ICON_CODEC.fieldOf("items"), Codec.list(ICON_CODEC).fieldOf("items_list")).xmap(o->o.map(List::of,b->b),o->o.size()==1?Either.left(o.get(0)):Either.right(o)).forGetter(ItemSpotlight::items),
			Codec.FLOAT.optionalFieldOf("scale",1f).forGetter(ItemSpotlight::scale)
			).apply(t, ItemSpotlight::new));


	@Override
	public Line bake(int width) {
		
		List<List<ItemStack>> items=this.items.stream().map(o->o.map(t->t.stream().map(ItemStack::new), t->t.stream().map(ItemStackTemplate::create)).toList()).toList();
		float sizef=scale*16;
		float widthf=sizef*items.size();
		int height=Mth.ceil(sizef);
		return new Line() {
			@Override
			public int extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int w, int mouseX, int mouseY,
					GuiInfoCollector tooltips) {
			
				graphics.pose().pushMatrix();
				graphics.pose().translate(x+(w-widthf)/2, y);
				graphics.pose().scale(scale);
				float simMouseX=x+(w-widthf)/2;
				boolean isYrange=mouseY>=y&&mouseY<=y+height;
				float space=1/scale;
				for(List<ItemStack> item:items) {
					ItemStack touse=item.get((int) ((System.currentTimeMillis()/500)%item.size()));
					if(isYrange&&mouseX>simMouseX&&mouseX<simMouseX+sizef) {
						tooltips.accept(touse,Mth.floor(simMouseX),y,Mth.ceil(sizef),Mth.ceil(sizef));
					}
					simMouseX+=sizef+1;
					graphics.item(touse, 0, 0);
					graphics.pose().translate(16+space,0);
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
		return "item";
	}

}
