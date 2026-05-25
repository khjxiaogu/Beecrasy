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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.khjxiaogu.beecrasy.genome.gene.Allele;
import com.khjxiaogu.beecrasy.genome.gene.EnumAlleleType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public class GeneRegistry {
	static record GeneType<T>(Identifier id, Codec<T> codec,StreamCodec<RegistryFriendlyByteBuf,T> streamCodec,BiConsumer<T,Consumer<Component>> toReadableName,Supplier<T> defaultValueSupplier,long priority)  implements Gene<T>{
		@Override
		public T getDefault() {
			return defaultValueSupplier.get();
		}
		@Override
		public String getLanguageKey() {
			return id.toLanguageKey("gene");
		}
		
		@Override
		public void getReadableText(T allele, Consumer<Component> text) {
			List<Component> tl=new ArrayList<>();
			toReadableName.accept(allele, tl::add);
			if(tl.size()==0)
				return;
			if(tl.size()==1) {
				text.accept(Component.translatable(id.toLanguageKey("gene"), tl.get(0)));
			}else {
				text.accept(Component.translatable(id.toLanguageKey("gene"), ""));
				tl.forEach(text::accept);
			}
		}
	}
	private static Map<Identifier,GeneType<?>> geneticsMap=new HashMap<>();
	private static List<Identifier> typelist=new ArrayList<>();
	private static List<Identifier> displaylist=new ArrayList<>();
	private static Reference2IntOpenHashMap<GeneType<?>> typeId=new Reference2IntOpenHashMap<>();
	private static volatile boolean sorted=false;
	private static volatile boolean displaySorted=false;
	private static Object lock=new Object();
	public static final Codec<Gene<?>> CODEC=Identifier.CODEC.comapFlatMap(GeneRegistry::getGeneType, Gene::id);
	public static final StreamCodec<ByteBuf,Gene<?>> STREAM_CODEC=ByteBufCodecs.idMapper(GeneRegistry::getByInt, GeneRegistry::getIntId);
	public synchronized static <T> Gene<T> register(Identifier id, Codec<T> codec,StreamCodec<RegistryFriendlyByteBuf,T> stream,BiConsumer<T,Consumer<Component>> toReadableName,Supplier<T> defaultValueSupplier,int priority) {
		GeneType<T> gt=new GeneType<>(id,codec,stream,toReadableName,defaultValueSupplier,(((long)priority)<<32)|geneticsMap.size());
		if(!geneticsMap.containsKey(id)) {
			synchronized(lock) {
				typelist.add(id);
				sorted=false;
				displaySorted=false;
			}
		}
		geneticsMap.put(id, gt);
		return gt;
	}

	private static void makeIndex() {
		if(!sorted) {
			synchronized(lock) {
				if(!sorted) {
					typelist.sort(Identifier::compareNamespaced);
					typeId.clear();
					for(int i=0;i<typelist.size();i++) {
						typeId.put(geneticsMap.get(typelist.get(i)), i);
					}
					sorted=true;
				}
			}
		}
	}
	public static <T extends Allele> Gene<T> register(EnumAlleleType<T> type,Supplier<T> defaultValueSupplier,int priority) {
		return register(type.getId(),type.CODEC,type.STREAM_CODEC,type::getReadableText,defaultValueSupplier,priority);
	
	}
	public static Gene<?> getByInt(int num){
		makeIndex();
		Gene<?> type=geneticsMap.get(typelist.get(num));
		return type;
	}
	public static int getIntId(Gene<?> gene){
		makeIndex();
		return typeId.getOrDefault(gene,-1);
	}
	public static Gene<?> get(Identifier id){
		Gene<?> type=geneticsMap.get(id);
		return type;
	}
	private static DataResult<Gene<?>> getGeneType(Identifier id){
		GeneType<?> type=geneticsMap.get(id);
		if(type==null)
			return DataResult.error(()->"Genetic type '"+id+"' not present!");
		return DataResult.success(type);
	}
	private static void makeDisplayList() {
		if(!displaySorted) {
			synchronized(lock) {
				if(!displaySorted) {
					displaylist.clear();
					displaylist.addAll(typelist);
					displaylist.sort(Comparator.comparingLong(t->geneticsMap.get(t).priority));
					displaySorted=true;
				}
			}
		}
	}
	
	public static Iterable<Identifier> getGeneTypes(){
		makeIndex();
		return typelist;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Collection<Gene<?>> getGeneTypesUnordered(){
		return (Collection)geneticsMap.values();
	}
	public static Iterable<Identifier> getDisplayOrder(){
		makeDisplayList();
		return typelist;
	}

	public static int size() {
		return geneticsMap.size();
	}
}
