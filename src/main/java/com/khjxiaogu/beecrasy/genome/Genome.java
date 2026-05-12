package com.khjxiaogu.beecrasy.genome;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public class Genome {
	public static final Codec<Genome> CODEC=RecordCodecBuilder.create(t->t
			.group(Codec.dispatchedMap(GeneRegistry.CODEC,a->a.codec())
				.fieldOf("alleles")
				.forGetter(o->(Map)o.alleles))
			.apply(t,Genome::new));
	public static final StreamCodec<RegistryFriendlyByteBuf,Genome> STREAM_CODEC=new StreamCodec<>() {
		@Override
		public void encode(RegistryFriendlyByteBuf output, Genome value) {
			output.writeVarInt(value.alleles.size());
			for(Entry<Gene<?>, ?> ent:value.alleles.entrySet()) {
				GeneRegistry.STREAM_CODEC.encode(output, ent.getKey());
				((Gene)ent.getKey()).streamCodec().encode(output, ent.getValue());
			}
		}
		@Override
		public Genome decode(RegistryFriendlyByteBuf input) {
			int size=input.readVarInt();
			Map<Gene, Object> alleles=new IdentityHashMap<>(size);
			if(size>0)
			for(int i=0;i<size;i++) {
				Gene key=GeneRegistry.STREAM_CODEC.decode(input);
				Object value=key.streamCodec().decode(input);
				alleles.put(key, value);
			}
			return new Genome((Map)alleles);
		}
	};
	private Map<Gene<?>, ?> alleles;
	Genome(Map<Gene<?>, ?> alleles) {
		super();
		this.alleles = alleles;
	}
	public <T> T getAllele(Gene<T> type) {
		return (T) alleles.get(type);
	}
	public Builder createBuilder() {
		return new Builder(this);
	}
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder{
		Map<Gene<?>, Object> alleles=new IdentityHashMap<>();
		public Builder() {
		}
		public Builder(Genome genome) {
			for(Entry<Gene<?>, ?> ent:genome.alleles.entrySet()) {
				alleles.put(ent.getKey(), ent.getValue());
			}
		}
		public <T> Builder add(Gene<T> type,T gene) {
			alleles.put(type, gene);
			return this;
		}
		public <T> T get(Gene<T> type) {
			return (T) alleles.get(type);
		}
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
