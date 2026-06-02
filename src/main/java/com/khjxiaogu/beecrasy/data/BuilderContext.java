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
import java.util.List;
import java.util.function.Function;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

public class BuilderContext<T> implements AutoCloseable{
	private static record IdAndBuilder<T>(Identifier id,T builder){}
	Function<String,Identifier> identifier;
	Function<T,Recipe<?>> build;
	RecipeOutput output;
	List<IdAndBuilder<T>> builders=new ArrayList<>();
	public BuilderContext(Function<String, Identifier> identifier, Function<T, Recipe<?>> build, RecipeOutput output) {
		super();
		this.identifier = identifier;
		this.build = build;
		this.output = output;
	}
	public T create(String id,T b) {
		this.builders.add(new IdAndBuilder<>(identifier.apply(id),b));
		return b;
	}
	@Override
	public void close() {
		for(IdAndBuilder<T> i:builders) {
			output.accept(ResourceKey.create(Registries.RECIPE, i.id()), build.apply(i.builder()), null);
		}
	}

}
