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

package com.khjxiaogu.beecrasy.data;

import java.util.List;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Recipes;
import com.khjxiaogu.beecrasy.item.LarvaItem;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class RoyalJellyRecipe extends ShapelessRecipe {
	public static final MapCodec<RoyalJellyRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i
			.group(Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
					CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(o -> o.bookInfo))
			.apply(i, RoyalJellyRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, RoyalJellyRecipe> STREAM_CODEC = StreamCodec.composite(
			Recipe.CommonInfo.STREAM_CODEC, o -> o.commonInfo, CraftingRecipe.CraftingBookInfo.STREAM_CODEC,
			o -> o.bookInfo, RoyalJellyRecipe::new);

	public RoyalJellyRecipe(CommonInfo commonInfo, CraftingBookInfo bookInfo) {
		super(commonInfo, bookInfo, new ItemStackTemplate(Items.QUEEN_BEE),
				List.of(Ingredient.of(Items.LARVA), Ingredient.of(Items.ROYAL_JELLY)));
	}

	public static RoyalJellyRecipe createDefault(boolean showNotification, CraftingBookCategory category,
			String group) {
		return new RoyalJellyRecipe(new CommonInfo(showNotification), new CraftingBookInfo(category, group));
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack larvaItem = null;
		for (int i = 0; i < input.size(); i++) {
			ItemStack cur = input.getItem(i);
			if (cur.is(Items.LARVA)) {
				larvaItem = cur;
				break;
			}
		}
		if (larvaItem != null)
			return LarvaItem.convertToQueen(larvaItem);
		return super.assemble(input);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public RecipeSerializer getSerializer() {
		return Recipes.ROYAL_JELLY.value();
	}

}
