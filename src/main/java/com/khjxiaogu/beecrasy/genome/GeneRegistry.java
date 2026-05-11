package com.khjxiaogu.beecrasy.genome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.khjxiaogu.beecrasy.genome.gene.Allele;
import com.khjxiaogu.beecrasy.genome.gene.EnumAlleleType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public class GeneRegistry {
	static record GeneType<T>(Identifier id, Codec<T> codec,StreamCodec<RegistryFriendlyByteBuf,T> streamCodec,Supplier<T> defaultValueSupplier,int priority,int order)  implements Gene<T>{
		public T getDefault() {
			return defaultValueSupplier.get();
		}
	}
	private static Map<Identifier,GeneType<?>> geneticsMap=new HashMap<>();
	private static List<Identifier> typelist=new ArrayList<>();
	private static Reference2IntOpenHashMap<GeneType<?>> typeId=new Reference2IntOpenHashMap<>();
	private static boolean isSorted=false;
	public static final Codec<GeneType<?>> CODEC=Identifier.CODEC.comapFlatMap(GeneRegistry::getGeneType, GeneType::id);
	public static final StreamCodec<ByteBuf,GeneType<?>> STREAM_CODEC=ByteBufCodecs.idMapper(GeneRegistry::getByInt, GeneRegistry::getIntId);
	public synchronized static <T> Gene<T> register(Identifier id, Codec<T> codec,StreamCodec<RegistryFriendlyByteBuf,T> stream,Supplier<T> defaultValueSupplier,int priority) {
		GeneType<T> gt=new GeneType<>(id,codec,stream,defaultValueSupplier,priority,geneticsMap.size());
		if(!geneticsMap.containsKey(id)) {
			typelist.add(id);
			isSorted=false;
		}
		geneticsMap.put(id, gt);
		return gt;
	}

	public static void makeIndex() {
		if(!isSorted) {
			typelist.sort(Identifier::compareNamespaced);
			typeId.clear();
			for(int i=0;i<typelist.size();i++) {
				typeId.put(geneticsMap.get(typelist.get(i)), i);
			}
			isSorted=true;
		}
	}
	public static <T extends Allele> Gene<T> register(Identifier id, EnumAlleleType<T> type,Supplier<T> defaultValueSupplier,int priority) {
		return register(id,type.CODEC,type.STREAM_CODEC,defaultValueSupplier,priority);
	
	}
	public static GeneType<?> getByInt(int num){
		makeIndex();
		GeneType<?> type=geneticsMap.get(typelist.get(num));
		return type;
	}
	public static int getIntId(GeneType<?> gene){
		makeIndex();
		return typeId.getOrDefault(gene,-1);
	}
	public static GeneType<?> get(Identifier id){
		GeneType<?> type=geneticsMap.get(id);
		return type;
	}
	public static DataResult<GeneType<?>> getGeneType(Identifier id){
		GeneType<?> type=geneticsMap.get(id);
		if(type==null)
			return DataResult.error(()->"Genetic type '"+id+"' not present!");
		return DataResult.success(type);
	}
	static Iterable<Identifier> getGeneTypes(){
		makeIndex();
		return typelist;
	}
}
