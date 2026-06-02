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

package com.khjxiaogu.beecrasy.genome;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import com.khjxiaogu.beecrasy.genome.Genes.Alleles;
import com.khjxiaogu.beecrasy.genome.gene.Biotope;
import com.khjxiaogu.beecrasy.genome.gene.Humidity;
import com.khjxiaogu.beecrasy.genome.gene.NumericAllele;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;
import com.khjxiaogu.beecrasy.genome.gene.Temperature;
import com.mojang.serialization.Codec;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;

public record PartialGenome(Map<Gene<?>, ?> alleles) implements AllelesHolder{
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Codec<PartialGenome> CODEC=Codec.dispatchedMap(GeneRegistry.CODEC,a->a.codec())
		.xmap(PartialGenome::new,t->(Map)t.alleles());
	public static final StreamCodec<RegistryFriendlyByteBuf,PartialGenome> STREAM_CODEC=new StreamCodec<>() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void encode(RegistryFriendlyByteBuf output, PartialGenome value) {
			output.writeVarInt(value.alleles.size());
			for(Entry<Gene<?>, ?> ent:value.alleles.entrySet()) {
				GeneRegistry.STREAM_CODEC.encode(output, ent.getKey());
				((Gene)ent.getKey()).streamCodec().encode(output, ent.getValue());
			}
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public PartialGenome decode(RegistryFriendlyByteBuf input) {
			int size=input.readVarInt();
			Map<Gene, Object> alleles=new IdentityHashMap<>(size);
			if(size>0)
			for(int i=0;i<size;i++) {
				Gene key=GeneRegistry.STREAM_CODEC.decode(input);
				Object value=key.streamCodec().decode(input);
				alleles.put(key, value);
			}
			return new PartialGenome((Map)alleles);
		}
	};
	public static class Builder{
		Map<Gene<?>, Object> alleles=new HashMap<>();
		public Builder() {
		}
		public Builder(Map<Gene<?>, ?> alleles) {
			super();
			this.alleles.putAll(alleles);
		}
		public <T> Builder add(Gene<T> key,T value) {
			this.alleles.put(key, value);
			return this;
		}
		public Builder temperature(Temperature value) {
			return add(Genes.TEMPERATURE, value);
		}
		public Builder humidity(Humidity value) {
			return add(Genes.HUMIDITY, value);
		}
		public Builder fertility(NumericAllele value) {
			return add(Genes.FERTILITY, value);
		}
		public Builder biotope(Biotope value) {
			return add(Genes.BIOTOPE, value);
		}
		public Builder product(Holder<Item> value) {
			return product(new ItemStackTemplate(value.value()));
		}
		public Builder product(ItemLike value) {
			return product(new ItemStackTemplate(value.asItem()));
		}
		public Builder product(ItemStackTemplate value) {
			return product((Biotope)alleles.getOrDefault(Genes.BIOTOPE, Alleles.WILD),value);
		}
		public Builder product(Biotope bio,ItemStackTemplate value) {
			return product(new ProductItem(bio,Optional.empty(),value));
		}
		@SuppressWarnings("unchecked")
		public Builder product(ProductItem value) {
			List<ProductItem> lp=(List<ProductItem>) this.alleles.get(Genes.PRODUCTS);
			if(lp==null) {
				return product(List.of(value));
			}
			return product(Stream.concat(lp.stream(), Stream.of(value)).toList());
		}
		public Builder product(List<ProductItem> value) {
			return add(Genes.PRODUCTS, value);
		}
		public Builder yld(NumericAllele value) {
			return add(Genes.YIELD, value);
		}
		public Builder lifespan(NumericAllele value) {
			return add(Genes.LIFESPAN, value);
		}
		public PartialGenome build() {
			return new PartialGenome(Map.copyOf(alleles));
		}
		
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAllele(Gene<T> type) {
		return (T) alleles.get(type);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Genome.Builder apply(Genome.Builder builder) {
		for(Entry<Gene<?>, ?> ent:alleles.entrySet()) {
			builder.add((Gene)ent.getKey(), ent.getValue());
		}
		return builder;
	}

}
