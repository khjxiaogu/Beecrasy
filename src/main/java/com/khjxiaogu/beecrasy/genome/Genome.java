package com.khjxiaogu.beecrasy.genome;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.khjxiaogu.beecrasy.genome.GeneRegistry.GeneType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class Genome {
	public static Codec<Genome> CODEC=RecordCodecBuilder.create(t->t
			.group(Codec.dispatchedMap(GeneRegistry.CODEC,GeneType::codec).fieldOf("alleles").forGetter(o->(Map)o.alleles))
			.apply(t,Genome::new));
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
			Map<GeneType, Object> calleles=new HashMap<>();
			for(GeneType gt:GeneRegistry.getGeneTypes()) {
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
