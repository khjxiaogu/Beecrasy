/** 
* Copyright (c) 2026 khjxiaogu
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Recipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

public record PressRecipe(SizedIngredient input,List<PossibleOutput> output,int time,Optional<FluidStackTemplate> fluid) implements Recipe<RandomizableRecipeInput> {
	public static final MapCodec<PressRecipe> CODEC=RecordCodecBuilder.mapCodec(t->t
		.group(SizedIngredient.NESTED_CODEC.fieldOf("input").forGetter(PressRecipe::input),
			PossibleOutput.CODEC.listOf().fieldOf("outputs").forGetter(PressRecipe::output),
			Codec.INT.fieldOf("time").forGetter(PressRecipe::time),
			FluidStackTemplate.CODEC.optionalFieldOf("fluid").forGetter(PressRecipe::fluid))
		.apply(t, PressRecipe::new)
		);

	public static final StreamCodec<RegistryFriendlyByteBuf,PressRecipe> STREAM_CODEC=StreamCodec.composite(
		SizedIngredient.STREAM_CODEC,PressRecipe::input,
		PossibleOutput.STREAM_CODEC.apply(ByteBufCodecs.list()),PressRecipe::output,
		ByteBufCodecs.INT,PressRecipe::time,
		ByteBufCodecs.optional(FluidStackTemplate.STREAM_CODEC),PressRecipe::fluid,
		PressRecipe::new
		);
	@Override
	public boolean matches(RandomizableRecipeInput inv, Level worldIn) {
		return input.test(inv.getItem(0));
	}
	public List<ItemStack> getOutputs(RandomizableRecipeInput input){
		List<ItemStack> stacks=new ArrayList<>(output.size());
		for(PossibleOutput out:output) {
			ItemStack created=out.createOutput(input.rnd());
			if(!created.isEmpty()) {
				stacks.add(created);
			}
		}
		return stacks;
	}
	@Override
	public ItemStack assemble(RandomizableRecipeInput input) {
		return output.size()>0?output.get(0).createOutput(input.rnd()):ItemStack.EMPTY;
	}

	@Override
	public boolean showNotification() {
		return false;
	}

	@Override
	public String group() {
		return "";
	}

	@Override
	public PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.CRAFTING_MISC;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public RecipeSerializer<PressRecipe> getSerializer() {
		return Recipes.PRESS_SERIALIZER.get();
	}

	@Override
	public RecipeType<PressRecipe> getType() {
		return Recipes.PRESS_TYPE.get();
	}


}