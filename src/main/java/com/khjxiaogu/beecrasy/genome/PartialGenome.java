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

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

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
