package com.khjxiaogu.beecrasy.genome.gene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;

public record ProductItem(Identifier biotope,Identifier recipe,ItemStackTemplate stack) {
	public static final Codec<ProductItem> CODEC=RecordCodecBuilder.create(t->t.group(
			Identifier.CODEC.fieldOf("biotope").forGetter(ProductItem::biotope),
			Identifier.CODEC.fieldOf("recipe").forGetter(ProductItem::recipe),
			ItemStackTemplate.CODEC.fieldOf("stack").forGetter(ProductItem::stack)
			).apply(t,ProductItem::new)
			);
}
