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

package com.khjxiaogu.beecrasy.genome.gene;

import java.util.Optional;

import com.khjxiaogu.beecrasy.genome.Genes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * 产品物品记录，描述蜂巢产出的物品，关联生境、可选配方来源和物品模板。
 *
 * @param biotope 关联的生境
 * @param recipe  可选的合成/熔炼配方来源标识符
 * @param stack   物品模板
 */
public record ProductItem(Biotope biotope,Optional<Identifier> recipe,ItemStackTemplate stack) {
	/** 产品物品的 {@link Codec} 编解码器。 */
	public static final Codec<ProductItem> CODEC=RecordCodecBuilder.create(t->t.group(
			Genes.BIOTOPE.codec().fieldOf("biotope").forGetter(ProductItem::biotope),
			Identifier.CODEC.optionalFieldOf("recipe").forGetter(ProductItem::recipe),
			ItemStackTemplate.CODEC.fieldOf("stack").forGetter(ProductItem::stack)
			).apply(t,ProductItem::new)
			);
	/** 产品物品的 {@link StreamCodec} 流编解码器（不含配方信息）。 */
	public static final StreamCodec<RegistryFriendlyByteBuf,ProductItem> STREAM_CODEC=StreamCodec.composite(
		Genes.BIOTOPE.streamCodec(),
		ProductItem::biotope,
		ItemStackTemplate.STREAM_CODEC,
		ProductItem::stack,
		(biotope,stack)->new ProductItem(biotope,Optional.empty(),stack)
		);
}
