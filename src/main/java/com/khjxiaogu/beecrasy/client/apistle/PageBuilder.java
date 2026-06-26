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

package com.khjxiaogu.beecrasy.client.apistle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.khjxiaogu.beecrasy.client.apistle.lines.HLine;
import com.khjxiaogu.beecrasy.client.apistle.lines.Image;
import com.khjxiaogu.beecrasy.client.apistle.lines.ItemSpotLine;
import com.khjxiaogu.beecrasy.client.apistle.lines.SpaceLine;
import com.khjxiaogu.beecrasy.client.apistle.lines.Text;
import com.khjxiaogu.beecrasy.client.apistle.lines.UnbakedLine;
import com.mojang.datafixers.util.Either;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;

public class PageBuilder {
	List<UnbakedLine> lines=new ArrayList<>();
	Optional<Either<Identifier,ItemStackTemplate>> icon=Optional.empty();
	String title;
	int order;
	HolderLookup.Provider registries;
	public PageBuilder(HolderLookup.Provider registries,String title) {
		super();
		this.title = title;
		this.registries = registries;
	}
	public PageBuilder setIcon(Optional<Either<Identifier, ItemStackTemplate>> icon) {
		this.icon=icon;
		return this;
	}
	public PageBuilder setIcon(Either<Identifier, ItemStackTemplate> icon) {
		return setIcon(Optional.of(icon));
	}
	public PageBuilder setIcon(Identifier icon) {
		return setIcon(Either.left(icon));
	}
	public PageBuilder setIcon(ItemStackTemplate icon) {
		return setIcon(Either.right(icon));
	}
	public PageBuilder setIcon(Item icon) {
		return setIcon(new ItemStackTemplate(icon));
	}
	public PageBuilder setIcon(Holder<Item> icon) {
		return setIcon(new ItemStackTemplate(icon));
	}
	public PageBuilder setOrder(int order) {
		this.order=order;
		return this;
	}
	public PageBuilder addLine(UnbakedLine line) {
		this.lines.add(line);
		return this;
	}
	
	public PageBuilder space() {
		return addLine(SpaceLine.DEFAULT);
	}
	public PageBuilder space(int height) {
		return addLine(new SpaceLine(height));
	}
	public PageBuilder hr() {
		return addLine(HLine.DEFAULT);
	}
	public PageBuilder hr(int color) {
		return addLine(new HLine(color));
	}
	public PageBuilder image(Identifier image,int width,int height) {
		return addLine(new Image(image,width,height));
	}
	public PageBuilder item(Ingredient item,float scale) {
		return addLine(new ItemSpotLine(Either.left(item),scale));
	}
	public PageBuilder item(List<ItemStackTemplate> item,float scale) {
		return addLine(new ItemSpotLine(Either.right(item),scale));
	}
	public PageBuilder item(ItemStackTemplate item,float scale) {
		return item(List.of(item),scale);
	}
	public PageBuilder item(Item item,float scale) {
		return item(new ItemStackTemplate(item),scale);
	}
	public PageBuilder item(Holder<Item> item,float scale) {
		return item(new ItemStackTemplate(item),scale);
	}
	public PageBuilder item(float scale,Item... item) {
		return item(List.of(item).stream().map(ItemStackTemplate::new).toList(),scale);
	}
	@SuppressWarnings("unchecked")
	@SafeVarargs
	public final PageBuilder item(float scale,Holder<Item>... item) {
		return item(List.of(item).stream().map(ItemStackTemplate::new).toList(),scale);
	}
	public PageBuilder item(float scale,ItemStackTemplate... item) {
		return item(List.of(item),scale);
	}

	public PageBuilder item(Ingredient item) {
	    return item(item, 1.0f);
	}

	public PageBuilder item(List<ItemStackTemplate> item) {
	    return item(item, 1.0f);
	}

	public PageBuilder item(ItemStackTemplate item) {
	    return item(item, 1.0f);
	}

	public PageBuilder item(Item item) {
	    return item(Ingredient.of(item), 1.0f);
	}

	public PageBuilder item(Holder<Item> item) {
	    return item(Ingredient.of(item.value()), 1.0f);
	}
	public PageBuilder item(TagKey<Item> item) {
	    return item(Ingredient.of(registries.getOrThrow(item)), 1.0f);
	}
	public PageBuilder item(Item... item) {
	    return item(1.0f, item);
	}

	@SafeVarargs
	@SuppressWarnings("unchecked")
	public final PageBuilder item(Holder<Item>... item) {
	    return item(1.0f, item);
	}

	public PageBuilder item(ItemStackTemplate... item) {
	    return item(1.0f, item);
	}
	public PageBuilder text(List<String> lines,float scale,boolean centered) {
		return addLine(new Text(lines,scale,centered));
	}

	public PageBuilder text(List<String> lines,float scale) {
		return text(lines,scale,false);
	}
	public PageBuilder text(List<String> lines,boolean centered) {
		return text(lines,1f,centered);
	}

	public PageBuilder text(List<String> lines) {
		return text(lines,1f,false);
	}
	public PageBuilder text(String lines,float scale,boolean centered) {
		return text(List.of(lines.split("\n")),scale,centered);
	}
	public PageBuilder text(String lines,float scale) {
		return text(lines,scale,false);
	}
	public PageBuilder text(String lines,boolean centered) {
		return text(lines,1f,centered);
	}
	public PageBuilder text(String lines) {
		return text(lines,1f,false);
	}
	public Page build() {
		return new Page(lines,icon,title,order);
	}
}
