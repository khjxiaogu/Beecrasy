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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Recipes;
import com.khjxiaogu.beecrasy.genome.PartialGenome;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record GenomePresets(Identifier name,Identifier geneticGroup,List<Weighted<PartialGenome>> genome) implements Recipe<RecipeInput> {
	public static final MapCodec<GenomePresets> CODEC=RecordCodecBuilder.mapCodec(t->t
		.group(Identifier.CODEC.fieldOf("name").forGetter(GenomePresets::name),
			Identifier.CODEC.fieldOf("geneticGroup").forGetter(GenomePresets::geneticGroup),
			Codec.list(Weighted.codec(PartialGenome.CODEC)).fieldOf("genomes").forGetter(GenomePresets::genome))
		.apply(t,GenomePresets::new));
	public static final StreamCodec<RegistryFriendlyByteBuf,GenomePresets> STREAM_CODEC=StreamCodec.composite(
			Identifier.STREAM_CODEC, GenomePresets::name,
			Identifier.STREAM_CODEC, GenomePresets::geneticGroup,
			Weighted.streamCodec(PartialGenome.STREAM_CODEC).apply(ByteBufCodecs.list()), GenomePresets::genome,
			GenomePresets::new);
	public static List<WeightedList<PartialGenome>> getPools(ServerLevel level,Identifier name) {
		Collection<RecipeHolder<GenomePresets>> recipes=level.recipeAccess().recipeMap().byType(Recipes.GENOME_PRESET_TYPE.get());
		Map<Identifier,List<Weighted<PartialGenome>>> maps=new LinkedHashMap<>();
		Function<Identifier,List<Weighted<PartialGenome>>> getter=t->maps.computeIfAbsent(t, _->new ArrayList<>(10));
		for(RecipeHolder<GenomePresets> recipe:recipes) {
			if(recipe.value().name().equals(name)) {
				getter.apply(recipe.value().geneticGroup()).addAll(recipe.value().genome());
			}
		}
		List<WeightedList<PartialGenome>> li=new ArrayList<>();
		for(Entry<Identifier, List<Weighted<PartialGenome>>> ent:maps.entrySet()) {
			li.add(WeightedList.of(ent.getValue()));
		}
		return li;
	}
	@Override
	public boolean matches(RecipeInput input, Level level) {
		return false;
	}

	@Override
	public ItemStack assemble(RecipeInput input) {
		return ItemStack.EMPTY;
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
	public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
		return Recipes.GENOME_PRESET.get();
	}

	@Override
	public RecipeType<? extends Recipe<RecipeInput>> getType() {
		return Recipes.GENOME_PRESET_TYPE.get();
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

}
