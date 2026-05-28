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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.khjxiaogu.beecrasy.utils.Utils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public class Genome implements AllelesHolder {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Codec<Genome> CODEC=RecordCodecBuilder.create(t->t
			.group(Codec.dispatchedMap(GeneRegistry.CODEC,a->a.codec())
				.fieldOf("alleles")
				.forGetter(o->(Map)o.alleles))
			.apply(t,Genome::new));
	public static final Codec<List<Genome>> LIST_CODEC=CODEC.listOf();
	public static final StreamCodec<RegistryFriendlyByteBuf,Genome> STREAM_CODEC=
			Utils.streamDispatchedMap(GeneRegistry.STREAM_CODEC, Gene::streamCodec).map(Genome::new, Genome::alleles);
	public static final Genome DEFAULT=new Genome(Map.of());
	private Map<Gene<?>, ?> alleles;
	Genome(Map<Gene<?>, ?> alleles) {
		super();
		this.alleles = alleles;
	}
	@SuppressWarnings("rawtypes")
	Map alleles(){
		return alleles;
	}
	public <T> T getAllele(Gene<T> type) {
		@SuppressWarnings("unchecked")
		T alle= (T) alleles.get(type);
		if(alle==null)
			return type.getDefault();
		return alle;
	}
	@Override
	public int hashCode() {
		return Objects.hash(alleles);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Genome other = (Genome) obj;
		return Objects.equals(alleles, other.alleles);
	}
	public Builder createBuilder() {
		return new Builder(this);
	}
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder implements AllelesHolder {
		private Map<Gene<?>, Object> alleles=new IdentityHashMap<>();
		public Builder() {
		}
		public Builder(Genome genome) {
			for(Entry<Gene<?>, ?> ent:genome.alleles.entrySet()) {
				alleles.put(ent.getKey(), ent.getValue());
			}
		}
		public Builder(Builder genome) {
			for(Entry<Gene<?>, ?> ent:genome.alleles.entrySet()) {
				alleles.put(ent.getKey(), ent.getValue());
			}
		}
		public Builder copy() {
			return new Builder(this);
		}
		public <T> Builder add(Gene<T> type,T gene) {
			alleles.put(type, gene);
			return this;
		}
		@SuppressWarnings("unchecked")
		public <T> T get(Gene<T> type) {
			return (T) alleles.get(type);
		}
		public <T> T getAllele(Gene<T> type) {
			@SuppressWarnings("unchecked")
			T alle= (T) alleles.get(type);
			if(alle==null)
				return type.getDefault();
			return alle;
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Genome build() {
			Map<Gene, Object> calleles=new IdentityHashMap<>();
			for(Identifier id:GeneRegistry.getGeneTypes()) {
				Gene gt=GeneRegistry.get(id);
				Object o=alleles.get(gt);
				if(o!=null) {
					calleles.put(gt, o);
				}else {
					calleles.put(gt, gt.getDefault());
				}
			}
			return new Genome((Map)calleles);
			
		}
		
	}
}
