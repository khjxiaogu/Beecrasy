package com.khjxiaogu.beecrasy.genome.gene;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public class EnumAlleleType<T extends Allele> {
	Map<String,T> alleleType=new HashMap<>();

	public final Codec<T> CODEC=Codec.STRING.comapFlatMap(this::getAlleleType, t->t.getId());
	public T registerAllele(T allele) {
		alleleType.put(allele.getId(), allele);
		return allele;
	}
	public DataResult<T> getAlleleType(String id){
		T type=alleleType.get(id);
		if(type==null)
			return DataResult.error(()->"Allele '"+id+"' not present!");
		return DataResult.success(type);
	}
}
