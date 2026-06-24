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

package com.khjxiaogu.beecrasy.data.recipe;

import java.util.List;
import java.util.stream.Stream;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Recipes;
import com.khjxiaogu.beecrasy.beehive.BeeHiveParameters;
import com.khjxiaogu.beecrasy.components.BeeHiveArgumentation;
import com.khjxiaogu.beecrasy.components.BeehiveArgumenter;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

public class PheromoneRecipe extends ShapelessRecipe {
	public static final MapCodec<PheromoneRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i
			.group(Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
					CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(o -> o.bookInfo))
			.apply(i, PheromoneRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, PheromoneRecipe> STREAM_CODEC = StreamCodec.composite(
			Recipe.CommonInfo.STREAM_CODEC, o -> o.commonInfo, CraftingRecipe.CraftingBookInfo.STREAM_CODEC,
			o -> o.bookInfo, PheromoneRecipe::new);

	@Override
	protected PlacementInfo createPlacementInfo() {
		return PlacementInfo.create(Ingredient.of(Items.PHEROMONO));
	}

	@Override
	public List<RecipeDisplay> display() {
		return List.of();
	}

	public PheromoneRecipe(CommonInfo commonInfo, CraftingBookInfo bookInfo) {
		super(commonInfo, bookInfo, new ItemStackTemplate(Items.PHEROMONO),
				List.of(Ingredient.of(Items.PHEROMONO), new Ingredient(new ICustomIngredient() {
					@Override
					public boolean test(ItemStack stack) {
						return !stack.is(Items.PHEROMONO);
					}

					@SuppressWarnings("deprecation")
					@Override
					public Stream<Holder<Item>> items() {
						return BuiltInRegistries.ITEM.listElements().filter(t->!Items.PHEROMONO.is(t)).map(t->t);
					}

					@Override
					public boolean isSimple() {
						return true;
					}
					@Override
					public IngredientType<?> getType() {
						return null;
					}
					
				})));
	}

	public static PheromoneRecipe createDefault(boolean showNotification, CraftingBookCategory category,
			String group) {
		return new PheromoneRecipe(new CommonInfo(showNotification), new CraftingBookInfo(category, group));
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		NonNullList<ItemStack> is= super.getRemainingItems(input);
		for(int i=0;i<input.size();i++) {
			ItemStack stack=input.getItem(i);
			if(!stack.is(Items.PHEROMONO))
				is.set(i, stack);
		}
		return is;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack otherItem = null;
		for (int i = 0; i < input.size(); i++) {
			ItemStack cur = input.getItem(i);
			if (!cur.isEmpty()&&!cur.is(Items.PHEROMONO)) {
				otherItem = cur;
				break;
			}
		}
		ItemStack result=super.assemble(input);
		if (otherItem != null) {
			result.set(Components.ARGUMENTATION,new BeehiveArgumenter(new BeeHiveArgumentation.Builder().addParam(BeeHiveParameters.MUTATION_DIRECTOR, List.of(otherItem.getItem())).build(),true));
			result.set(Components.TINT_STACK,ItemStackTemplate.fromNonEmptyStack(otherItem));
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public RecipeSerializer getSerializer() {
		return Recipes.PHEROMONE.value();
	}

}
