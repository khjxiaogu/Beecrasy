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


import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import com.google.common.collect.ImmutableSet;
import static com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks.*;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Tags;
import com.khjxiaogu.beecrasy.blocks.bee.BeeNestBlock;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
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
public abstract class BeecrasyTagGenerator<T> extends TagsProvider<T> {

	public BeecrasyTagGenerator(DataGenerator dataGenerator,ResourceKey<Registry<T>> registry, String modId,CompletableFuture<HolderLookup.Provider> provider) {
		super(dataGenerator.getPackOutput(), registry,provider,modId);
	}

	@SuppressWarnings("unchecked")
	protected void adds(TagAppender<ResourceKey<T>, T> ta,ResourceKey<? extends T>... keys) {
		for(ResourceKey<? extends T> blk:keys)
			ta.add((ResourceKey<T>) blk);
	}
	protected TagAppender<ResourceKey<T>, T> tag(String s) {
		return this.tag(modTag(s));
	}
	protected TagAppender<ResourceKey<T>, T> tag(TagKey<T> s) {
		return TagAppender.forBuilder(super.getOrCreateRawBuilder(s)) ;
	}
	protected TagAppender<ResourceKey<T>, T> tag(Identifier s) {
		return tag(TagKey.create(registryKey, s)) ;
	}
	protected ResourceKey<T> modResource(String s) {
		return ResourceKey.create(registryKey,modId(s));
	}
	protected ResourceKey<T> rk(T b) {
		return registry().getResourceKey(b).get();
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Registry<T> registry() {
		return BuiltInRegistries.REGISTRY.getValueOrThrow((ResourceKey)registryKey);
	}
	@SuppressWarnings("rawtypes")
	protected ResourceKey rk(DeferredHolder it) {
		return it.getKey();
	}

	protected Identifier rl(String r) {
		return Identifier.parse(r);
	}

	protected TagKey<T> modTag(String s) {
		return TagKey.create(registryKey, modId(s));
	}

	protected TagKey<Item> idTag(Identifier s) {
		return ItemTags.create(s);
	}

	protected Identifier modId(String s) {
		return Identifier.fromNamespaceAndPath(super.modId, s);
	}

	protected Identifier cId(String s) {
		return Identifier.fromNamespaceAndPath("c", s);
	}

	protected Identifier mcId(String s) {
		return Identifier.withDefaultNamespace(s);
	}
}
