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

package com.khjxiaogu.beecrasy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableSet;
import com.khjxiaogu.beecrasy.data.BuilderContext;
import com.khjxiaogu.beecrasy.data.GenomePresets;
import com.khjxiaogu.beecrasy.data.PressRecipe;
import com.khjxiaogu.beecrasy.data.RoyalJellyRecipe;
import com.khjxiaogu.beecrasy.genome.Genes.Alleles;
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
import net.minecraft.world.item.crafting.CraftingBookCategory;
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
			outx.accept(ResourceKey.create(Registries.RECIPE, rl(r1)),r2,null);
		};
		out.accept("press/honey_drop", new PressRecipe.Builder(BeecrasyRegistries.Items.HONEY_DROP,1).addFluid(BeecrasyRegistries.Fluids.HONEY_STILL.get(),100).setTime(100).create());
		out.accept("press/comb", new PressRecipe.Builder(BeecrasyRegistries.Items.PRODUCT_COMB,1).addOutput(BeecrasyRegistries.Items.BEESWAX.get(),1)
				.addOutput(BeecrasyRegistries.Items.HONEY_DROP.get(),1,.8f)
				.setTime(200).create());
		out.accept("royal_jelly", RoyalJellyRecipe.createDefault(true, CraftingBookCategory.MISC, ""));
		try(BuilderContext<GenomePresets.Builder> builder=presetBuilder()){
			builder.create("genome/base", new GenomePresets.Builder(Constants.BASE_ID))
			.group("yield")
			.item(3, t->t.yld(Alleles.MEAGER_YIELD))
			.item(1, t->t.yld(Alleles.MODERATE_YIELD));
			builder.create("genome/woods", new GenomePresets.Builder(Constants.FOREST_ID))
			.group("products")
			.item(2, t->t.product(Items.OAK_LOG))
			.item(1, t->t.product(Items.BIRCH_LOG));
			builder.create("genome/flowers", new GenomePresets.Builder(Constants.FLOWER_ID))
			.group("products")
			.item(1, t->t.product(Items.POPPY))
			.item(1, t->t.product(Items.BLUE_ORCHID))
		    .item(1, t->t.product(Items.ALLIUM))
		    .item(1, t->t.product(Items.AZURE_BLUET))
		    .item(1, t->t.product(Items.RED_TULIP))
		    .item(1, t->t.product(Items.ORANGE_TULIP))
		    .item(1, t->t.product(Items.WHITE_TULIP))
		    .item(1, t->t.product(Items.PINK_TULIP))
		    .item(1, t->t.product(Items.OXEYE_DAISY))
		    .item(1, t->t.product(Items.CORNFLOWER))
		    .item(1, t->t.product(Items.LILY_OF_THE_VALLEY))
		    .item(1, t->t.product(Items.WITHER_ROSE))
		    .item(1, t->t.product(Items.TORCHFLOWER))
		    .item(1, t->t.product(Items.PITCHER_PLANT))
		    .item(1, t->t.product(Items.SPORE_BLOSSOM))
		    .item(1, t->t.product(Items.BROWN_MUSHROOM))
		    .item(1, t->t.product(Items.RED_MUSHROOM))
		    .item(1, t->t.product(Items.CRIMSON_FUNGUS))
		    .item(1, t->t.product(Items.WARPED_FUNGUS))
		    .item(1, t->t.product(Items.CRIMSON_ROOTS))
		    .item(1, t->t.product(Items.WARPED_ROOTS))
		    .item(1, t->t.product(Items.NETHER_SPROUTS))
		    .item(1, t->t.product(Items.WEEPING_VINES))
		    .item(1, t->t.product(Items.TWISTING_VINES))
		    .item(1, t->t.product(Items.SUGAR_CANE));
			builder.create("genome/mines", new GenomePresets.Builder("stones"))
			.group("products")
			.item(60, t->t.product(Items.COBBLESTONE))
			.item(12, t->t.product(Items.GRANITE))
			.item(12, t->t.product(Items.DIORITE))
			.item(12, t->t.product(Items.ANDESITE))
			.item(12, t->t.product(Items.CALCITE))
			.item(8, t->t.product(Items.COAL_ORE))
			.item(4, t->t.product(Items.IRON_ORE))
			.item(4, t->t.product(Items.COPPER_ORE))
			.item(1, t->t.product(Items.GOLD_ORE))
			.item(1, t->t.product(Items.LAPIS_ORE))
			;
		}
	}
	protected BuilderContext<GenomePresets.Builder> presetBuilder(){
		return new BuilderContext<>(this::rl,t->t.build(),this.output);
	}

	protected Fluid modfluid(String name) {
		return fluid(Identifier.fromNamespaceAndPath(Beecrasy.MODID, name));
	}

	protected Item moditem(String name) {
		return item(Identifier.fromNamespaceAndPath(Beecrasy.MODID, name));
	}

	protected Item mcitem(String name) {
		return item(Identifier.withDefaultNamespace(name));
	}

	protected Item item(Identifier rl) {
		return BuiltInRegistries.ITEM.getValue(rl);
	}

	protected static Fluid fluid(Identifier rl) {
		return BuiltInRegistries.FLUID.getValue(rl);
	}

	protected static Identifier modrl(String s) {
		return Beecrasy.rl(s);
	}

	protected Identifier ctag(String s) {
		return Identifier.fromNamespaceAndPath("c", s);
	}

	private static Identifier mcrl(String s) {
		return Identifier.withDefaultNamespace(s);
	}
	private static TagKey<Item> rk(Identifier rl){
		return TagKey.create(Registries.ITEM, rl);
	}

	protected Identifier rl(String s) {
		if (!s.contains("/"))
			s = "crafting/" + s;
		if (PATH_COUNT.containsKey(s)) {
			int count = PATH_COUNT.get(s) + 1;
			PATH_COUNT.put(s, count);
			return Beecrasy.rl(s + count);
		}
		PATH_COUNT.put(s, 1);
		return Beecrasy.rl(s);
	}



}
