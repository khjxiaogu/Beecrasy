package com.khjxiaogu.beecrasy.genome;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.resources.Identifier;

public class GeneRegistry {
	static record GeneType<T>(Identifier id, Codec<T> codec,Supplier<T> defaultValueSupplier,int priority,int order)  implements Gene<T>{
		public T getDefault() {
			return defaultValueSupplier.get();
		}
	}
	private static Map<Identifier,GeneType<?>> geneticsMap=new HashMap<>();
	private static PriorityQueue<GeneType<?>> geneticsList=new PriorityQueue<>(Comparator.<GeneType<?>>comparingInt(GeneType::priority).thenComparingInt(GeneType::order));
	public static final Codec<GeneType<?>> CODEC=Identifier.CODEC.comapFlatMap(GeneRegistry::getGeneType, GeneType::id);
	public synchronized static <T> Gene<T> register(Identifier id, Codec<T> codec,Supplier<T> defaultValueSupplier,int priority) {
		GeneType<T> gt=new GeneType<>(id,codec,defaultValueSupplier,priority,geneticsMap.size());
		geneticsMap.put(id, gt);
		geneticsList.add(gt);
		return gt;
	}
	public static DataResult<GeneType<?>> getGeneType(Identifier id){
		GeneType<?> type=geneticsMap.get(id);
		if(type==null)
			return DataResult.error(()->"Genetic type '"+id+"' not present!");
		return DataResult.success(type);
	}
	static Iterable<GeneType<?>> getGeneTypes(){
		return geneticsList;
	}
}
