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


import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import com.google.common.collect.ImmutableSet;
import static com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks.*;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Tags;
import com.khjxiaogu.beecrasy.blocks.BeeNestBlock;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

@SuppressWarnings("unused")
public class BeecrasyBlockTagGenerator extends TagsProvider<Block> {

	public BeecrasyBlockTagGenerator(DataGenerator dataGenerator, String modId,CompletableFuture<HolderLookup.Provider> provider) {
		super(dataGenerator.getPackOutput(), Registries.BLOCK,provider,modId);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void addTags(Provider pProvider) {
		TagAppender<ResourceKey<Block>, Block> pickaxe = tag(BlockTags.MINEABLE_WITH_PICKAXE);
		pickaxe.add(rk(SEQUENCER)).add(rk(HONEY_PRESS));

		TagAppender<ResourceKey<Block>, Block> net = tag(Tags.MINABLE_NET);
		net.add(rk(BEE_NEST_SMALL)).add(rk(BEE_NEST_NASCENT)).add(rk(BEE_NEST_MEDIUM)).add(rk(BEE_NEST_BIG));
	}
	@SuppressWarnings("unchecked")
	@SafeVarargs
	private void adds(TagAppender<ResourceKey<Block>, Block> ta,ResourceKey<? extends Block>... keys) {
		for(ResourceKey<? extends Block> blk:keys)
		ta.add((ResourceKey<Block>) blk);
	}
	private TagAppender<ResourceKey<Block>, Block> tag(String s) {
		return this.tag(BlockTags.create(modId(s)));
	}
	private TagAppender<ResourceKey<Block>, Block> tag(TagKey<Block> s) {
		return TagAppender.forBuilder(super.getOrCreateRawBuilder(s)) ;
	}
	private TagAppender<ResourceKey<Block>, Block> tag(Identifier s) {
		return tag(BlockTags.create(s)) ;
	}
	private ResourceKey<Block> modBlock(String s) {
		return ResourceKey.create(Registries.BLOCK,modId(s));
	}
	private ResourceKey<Block> rk(Block  b) {
		return BuiltInRegistries.BLOCK.getResourceKey(b).get();
	}

	private ResourceKey rk(DeferredHolder it) {
		return it.getKey();
	}

	private Identifier rl(String r) {
		return Identifier.parse(r);
	}

	private TagKey<Block> modTag(String s) {
		return BlockTags.create(modId(s));
	}

	private TagKey<Item> idTag(Identifier s) {
		return ItemTags.create(s);
	}

	private Identifier modId(String s) {
		return Identifier.fromNamespaceAndPath(Beecrasy.MODID, s);
	}

	private Identifier cId(String s) {
		return Identifier.fromNamespaceAndPath("c", s);
	}

	@SuppressWarnings("unused")
	private Identifier mcId(String s) {
		return Identifier.withDefaultNamespace(s);
	}

	@Override
	public String getName() {
		return Beecrasy.MODID + " block tags";
	}


	/*@Override
	protected Path getPath(Identifier id) {
		return super.pathProvider.json("data/" + id.getNamespace() + "/tags/blocks/" + id.getPath() + ".json");
	}*/
}
