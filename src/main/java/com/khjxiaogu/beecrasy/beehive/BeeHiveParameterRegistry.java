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

package com.khjxiaogu.beecrasy.beehive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

import com.khjxiaogu.beecrasy.utils.Utils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class BeeHiveParameterRegistry {
	public static record BeehiveParameterType<T>(Identifier id, Codec<T> codec,BinaryOperator<T> merge,Supplier<T> defaultValueSupplier){
		public T getDefault() {
			return defaultValueSupplier.get();
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void mergeTo(Map<BeehiveParameterType<?>,Object> params,Object value) {
			params.merge((BeehiveParameterType)this, value, (BinaryOperator)merge());
		}
	}
	private static Map<Identifier,BeehiveParameterType<?>> idMap=new HashMap<>();
	private static List<Identifier> typeIdMap=new ArrayList<>();
	private static Reference2IntOpenHashMap<BeehiveParameterType<?>> typeId=new Reference2IntOpenHashMap<>();
	private static volatile boolean sorted=false;
	private static Object lock=new Object();
	public static final Codec<BeehiveParameterType<?>> CODEC=Identifier.CODEC
			.comapFlatMap(BeeHiveParameterRegistry::getType, BeehiveParameterType::id);
	public static final Codec<Map<BeehiveParameterType<?>, Object>> COMPOSITE_CODEC=Codec.dispatchedMap(CODEC, BeehiveParameterType::codec);
	public static final StreamCodec<ByteBuf,BeehiveParameterType<?>> STREAM_CODEC=ByteBufCodecs.
			idMapper(BeeHiveParameterRegistry::getByInt, BeeHiveParameterRegistry::getIntId);
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final StreamCodec<ByteBuf,Map<BeehiveParameterType<?>, Object>> COMPOSITE_STREAM_CODEC=Utils.streamDispatchedMap(BeeHiveParameterRegistry.STREAM_CODEC,Util.memoize(t->ByteBufCodecs.fromCodec((Codec)t.codec())));
	
	public static BeehiveParameterType<Float> registerNumeric(Identifier id) {
		return register(id,Codec.FLOAT,(a,b)->a+b,()->0f);
	}
	public synchronized static <T> BeehiveParameterType<T> register(Identifier id, Codec<T> codec,BinaryOperator<T> merge,Supplier<T> defaultValueSupplier) {
		BeehiveParameterType<T> gt=new BeehiveParameterType<>(id,codec,merge,defaultValueSupplier);
		if(!idMap.containsKey(id)) {
			synchronized(lock) {
				typeIdMap.add(id);
				sorted=false;
			}
		}
		idMap.put(id, gt);
		return gt;
	}

	private static void makeIndex() {
		if(!sorted) {
			synchronized(lock) {
				if(!sorted) {
					typeIdMap.sort(Identifier::compareNamespaced);
					typeId.clear();
					for(int i=0;i<typeIdMap.size();i++) {
						typeId.put(idMap.get(typeIdMap.get(i)), i);
					}
					sorted=true;
				}
			}
		}
	}
	public static BeehiveParameterType<?> getByInt(int num){
		makeIndex();
		BeehiveParameterType<?> type=idMap.get(typeIdMap.get(num));
		return type;
	}
	public static int getIntId(BeehiveParameterType<?> type){
		makeIndex();
		return typeId.getOrDefault(type,-1);
	}
	public static BeehiveParameterType<?> get(Identifier id){
		BeehiveParameterType<?> type=idMap.get(id);
		return type;
	}
	private static DataResult<BeehiveParameterType<?>> getType(Identifier id){
		BeehiveParameterType<?> type=idMap.get(id);
		if(type==null)
			return DataResult.error(()->"BeehiveParam type '"+id+"' not present!");
		return DataResult.success(type);
	}
}
