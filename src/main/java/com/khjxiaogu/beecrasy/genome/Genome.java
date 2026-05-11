package com.khjxiaogu.beecrasy.genome;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.khjxiaogu.beecrasy.genome.GeneRegistry.GeneType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public class Genome {
	public static final Codec<Genome> CODEC=RecordCodecBuilder.create(t->t
			.group(Codec.dispatchedMap(GeneRegistry.CODEC,GeneType::codec).fieldOf("alleles").forGetter(o->(Map)o.alleles))
			.apply(t,Genome::new));
	public static final StreamCodec<RegistryFriendlyByteBuf,Genome> STREAM_CODEC=new StreamCodec<>() {
		@Override
		public void encode(RegistryFriendlyByteBuf output, Genome value) {
			output.writeVarInt(value.alleles.size());
			for(Entry<GeneType<?>, ?> ent:value.alleles.entrySet()) {
				GeneRegistry.STREAM_CODEC.encode(output, ent.getKey());
				((GeneType)ent.getKey()).streamCodec().encode(output, ent.getValue());
			}
			
		}
		@Override
		public Genome decode(RegistryFriendlyByteBuf input) {
			int size=input.readVarInt();
			Map<GeneType, Object> alleles=new IdentityHashMap<>(size);
			if(size>0)
			for(int i=0;i<size;i++) {
				GeneType key=GeneRegistry.STREAM_CODEC.decode(input);
				Object value=key.streamCodec().decode(input);
				alleles.put(key, value);
			}
			return new Genome((Map)alleles);
		}
	};
	private Map<GeneType<?>, ?> alleles;
	Genome(Map<GeneType<?>, ?> alleles) {
		super();
		this.alleles = alleles;
	}
	public Builder createBuilder() {
		return new Builder(this);
	}
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder{
		Map<GeneType, Object> alleles=new HashMap<>();
		public Builder() {
		}
		public Builder(Genome genome) {
			for(Entry<GeneType<?>, ?> ent:genome.alleles.entrySet()) {
				alleles.put(ent.getKey(), ent.getValue());
			}
		}
		public <T> Builder add(GeneType<T> type,T gene) {
			alleles.put(type, gene);
			return this;
		}
		public Genome build() {
			Map<GeneType, Object> calleles=new IdentityHashMap<>();
			for(Identifier id:GeneRegistry.getGeneTypes()) {
				GeneType gt=GeneRegistry.get(id);
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
