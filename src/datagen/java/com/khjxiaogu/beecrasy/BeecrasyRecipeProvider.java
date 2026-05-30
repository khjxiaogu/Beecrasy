/*
 * Copyright (c) 2024 TeamMoeg
 *
 * This file is part of Caupona.
 *
 * Caupona is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Caupona is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Specially, we allow this software to be used alongside with closed source software Minecraft(R) and Forge or other modloader.
 * Any mods or plugins can also use apis provided by forge or com.teammoeg.caupona.api without using GPL or open source.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caupona. If not, see <https://www.gnu.org/licenses/>.
 */

package com.khjxiaogu.beecrasy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableSet;
import com.khjxiaogu.beecrasy.data.PressRecipe;
import com.mojang.datafixers.util.Pair;


import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForgeMod;

@SuppressWarnings("unused")
public class BeecrasyRecipeProvider extends RecipeProvider {
	public static class Runner extends RecipeProvider.Runner{
		public Runner(PackOutput packOutput, CompletableFuture<Provider> registries) {
			super(packOutput, registries);
		}

		@Override
		public String getName() {
			return Beecrasy.MODID;
		}

		@Override
		protected RecipeProvider createRecipeProvider(Provider registries, RecipeOutput output) {
			return new BeecrasyRecipeProvider(registries,output);
		}
		
	}
	private final HashMap<String, Integer> PATH_COUNT = new HashMap<>();

	static final Fluid water = fluid(mrl("nail_soup")), milk = fluid(mrl("scalded_milk")), stock = fluid(mrl("stock"));
	public static List<Pair<Identifier,Recipe<?>>> recipes = new ArrayList<>();

	public BeecrasyRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
		super(registries, output);
	}
	public HolderSet<Item> createTag(Identifier tagName){
		return registries.lookupOrThrow(Registries.ITEM).getOrThrow(ItemTags.create(tagName));
	}
	public HolderSet<Item> createTag(TagKey<Item> tagName){
		return registries.lookupOrThrow(Registries.ITEM).getOrThrow(tagName);
	}
	
	@Override
	protected void buildRecipes() {
		RecipeOutput outx=this.output;
		BiConsumer<String,Recipe<?>> out = (r1,r2) -> {
			outx.accept(ResourceKey.create(Registries.RECIPE, Beecrasy.rl(r1)),r2,null);
		};
		out.accept("press/honey_drop", new PressRecipe.Builder(BeecrasyRegistries.Items.HONEY_DROP,1).addFluid(BeecrasyRegistries.Fluids.HONEY_STILL.get(),100).setTime(100).create());
		out.accept("press/comb", new PressRecipe.Builder(BeecrasyRegistries.Items.PRODUCT_COMB,1).addOutput(BeecrasyRegistries.Items.BEESWAX.get(),1)
				.addOutput(BeecrasyRegistries.Items.HONEY_DROP.get(),1,.8f)
				.setTime(200).create());
		
	}


	private Fluid cpfluid(String name) {
		return BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath(Beecrasy.MODID, name));
	}

	private Item cpitem(String name) {
		return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(Beecrasy.MODID, name));
	}

	private Item mitem(String name) {
		return BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(name));
	}

	private Item item(Identifier rl) {
		return BuiltInRegistries.ITEM.getValue(rl);
	}

	private static Fluid fluid(Identifier rl) {
		return BuiltInRegistries.FLUID.getValue(rl);
	}

	private static Identifier mrl(String s) {
		return Identifier.fromNamespaceAndPath(Beecrasy.MODID, s);
	}

	private Identifier ftag(String s) {
		return Identifier.fromNamespaceAndPath("c", s);
	}

	private Identifier mcrl(String s) {
		return Identifier.withDefaultNamespace(s);
	}
	private TagKey<Item> rk(Identifier rl){
		return TagKey.create(Registries.ITEM, rl);
	}

	private Identifier rl(String s) {
		if (!s.contains("/"))
			s = "crafting/" + s;
		if (PATH_COUNT.containsKey(s)) {
			int count = PATH_COUNT.get(s) + 1;
			PATH_COUNT.put(s, count);
			return Identifier.fromNamespaceAndPath(Beecrasy.MODID, s + count);
		}
		PATH_COUNT.put(s, 1);
		return Identifier.fromNamespaceAndPath(Beecrasy.MODID, s);
	}



}
