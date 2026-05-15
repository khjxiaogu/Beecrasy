package com.khjxiaogu.beecrasy.genome.gene;

import java.util.Optional;

import com.khjxiaogu.beecrasy.genome.Genes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;

public record ProductItem(Biotope biotope,Optional<Identifier> recipe,ItemStackTemplate stack) {
	public static final Codec<ProductItem> CODEC=RecordCodecBuilder.create(t->t.group(
			Genes.BIOTOPE.codec().fieldOf("biotope").forGetter(ProductItem::biotope),
			Identifier.CODEC.optionalFieldOf("recipe").forGetter(ProductItem::recipe),
			ItemStackTemplate.CODEC.fieldOf("stack").forGetter(ProductItem::stack)
			).apply(t,ProductItem::new)
			);
	public static final StreamCodec<RegistryFriendlyByteBuf,ProductItem> STREAM_CODEC=StreamCodec.composite(
		Genes.BIOTOPE.streamCodec(),
		ProductItem::biotope,
		ItemStackTemplate.STREAM_CODEC,
		ProductItem::stack,
		(biotope,stack)->new ProductItem(biotope,Optional.empty(),stack)
		);
}
