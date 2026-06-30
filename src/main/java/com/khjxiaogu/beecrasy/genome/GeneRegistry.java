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
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
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

/**
 * 基因类型注册表，管理所有基因类型的注册、索引构建、按ID/整数查找等。
 * <p>
 * 支持有序迭代和显示排序。
 */
public class GeneRegistry {
	/**
	 * 基因类型的内部实现记录，实现 {@link Gene} 接口。
	 *
	 * @param id                  基因标识符
	 * @param codec               等位基因值的Codec编解码器
	 * @param streamCodec         等位基因值的StreamCodec流编解码器
	 * @param toReadableName      等位基因值转可读文本的函数
	 * @param toShortName         等位基因值转简写可读文本的函数
	 * @param defaultValueSupplier 默认等位基因值的供应者
	 * @param priority            显示优先级
	 * @param <T>                 等位基因值类型
	 */
	static record GeneType<T>(Identifier id, Codec<T> codec,StreamCodec<RegistryFriendlyByteBuf,T> streamCodec,Function<T,Component> toReadableName,Function<T,Component> toShortName,Supplier<T> defaultValueSupplier,long priority)  implements Gene<T>{
		@Override
		public T getDefault() {
			return defaultValueSupplier.get();
		}
		@Override
		public String getLanguageKey() {
			return id.toLanguageKey("gene");
		}

		@Override
		public String getShortLanguageKey() {
			return id.toLanguageKey("gene","short");
		}
		@Override
		public Component getReadableText(T allele) {
			return toReadableName.apply(allele);
		}
		@Override
		public Component getShortReadableText(T allele) {
			return toShortName.apply(allele);
			
			
		}
		@Override
		public Component getReadableText() {
			return Component.translatable(getLanguageKey());
		}
		@Override
		public Component getShortReadableText() {
			return Component.translatable(getShortLanguageKey());
		}
	}
	/** 基因类型映射表（ID → GeneType）。 */
	private static Map<Identifier,GeneType<?>> geneticsMap=new HashMap<>();

	private static Map<Identifier,EnumAlleleType<?>> enumAllelesMap=new HashMap<>();
	/** 按命名空间排序的基因ID列表。 */
	private static List<Identifier> typelist=new ArrayList<>();
	/** 按显示优先级排序的基因ID列表。 */
	private static List<Identifier> displaylist=new ArrayList<>();
	/** 基因类型到整数ID的映射表。 */
	private static Reference2IntOpenHashMap<GeneType<?>> typeId=new Reference2IntOpenHashMap<>();
	/** typelist索引是否已排序。 */
	private static volatile boolean sorted=false;
	/** displaylist是否已排序。 */
	private static volatile boolean displaySorted=false;
	/** 索引构建锁。 */
	private static Object lock=new Object();
	/** 基因类型的 {@link Codec} 编解码器，基于Identifier编解码。 */
	public static final Codec<Gene<?>> CODEC=Identifier.CODEC.comapFlatMap(GeneRegistry::getGeneType, Gene::id);
	/** 基因类型的 {@link StreamCodec} 流编解码器，基于整数ID编解码。 */
	public static final StreamCodec<ByteBuf,Gene<?>> STREAM_CODEC=ByteBufCodecs.idMapper(GeneRegistry::getByInt, GeneRegistry::getIntId);
	/**
	 * 注册一个基因类型（长短名称相同）。
	 *
	 * @param <T>               等位基因值类型
	 * @param id                基因标识符
	 * @param codec             等位基因值的Codec
	 * @param stream            等位基因值的StreamCodec
	 * @param toReadableName    等位基因值转可读文本的函数（同时用于短名称）
	 * @param defaultValueSupplier 默认等位基因值的供应者
	 * @param priority          显示优先级
	 * @return 注册后的基因类型
	 */
	public synchronized static <T> Gene<T> register(Identifier id, Codec<T> codec,StreamCodec<RegistryFriendlyByteBuf,T> stream,Function<T,Component> toReadableName,Supplier<T> defaultValueSupplier,int priority) {
		return register(id,codec,stream,toReadableName,toReadableName,defaultValueSupplier,priority);
	}
	/**
	 * 注册一个基因类型。
	 *
	 * @param <T>               等位基因值类型
	 * @param id                基因标识符
	 * @param codec             等位基因值的Codec
	 * @param stream            等位基因值的StreamCodec
	 * @param toReadableName    等位基因值转可读文本的函数
	 * @param toShortName       等位基因值转简写可读文本的函数
	 * @param defaultValueSupplier 默认等位基因值的供应者
	 * @param priority          显示优先级
	 * @return 注册后的基因类型
	 */
	public synchronized static <T> Gene<T> register(Identifier id, Codec<T> codec,StreamCodec<RegistryFriendlyByteBuf,T> stream,Function<T,Component> toReadableName,Function<T,Component> toShortName,Supplier<T> defaultValueSupplier,int priority) {
		GeneType<T> gt=new GeneType<>(id,codec,stream,toReadableName,toShortName,defaultValueSupplier,(((long)priority)<<32)|geneticsMap.size());
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

	/**
	 * 构建按命名空间排序的索引（延迟初始化）。
	 */
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
	/**
	 * 从枚举Allele类型注册基因。
	 *
	 * @param <T>                  等位基因类型
	 * @param type                 枚举等位基因类型
	 * @param defaultValueSupplier 默认等位基因供应者
	 * @param priority             显示优先级
	 * @return 注册后的基因类型
	 */
	public static <T extends Allele> Gene<T> register(EnumAlleleType<T> type,Supplier<T> defaultValueSupplier,int priority) {
		enumAllelesMap.put(type.getId(), type);
		return register(type.getId(),type.CODEC,type.STREAM_CODEC,type::getReadableText,type::getShortReadableText,defaultValueSupplier,priority);
	
	}
	/**
	 * 根据整数ID获取基因类型。
	 *
	 * @param num 整数ID
	 * @return 基因类型
	 */
	public static Gene<?> getByInt(int num){
		makeIndex();
		Gene<?> type=geneticsMap.get(typelist.get(num));
		return type;
	}
	/**
	 * 根据基因类型获取其整数ID。
	 *
	 * @param gene 基因类型
	 * @return 整数ID，未找到时返回 -1
	 */
	public static int getIntId(Gene<?> gene){
		makeIndex();
		return typeId.getOrDefault(gene,-1);
	}
	/**
	 * 根据标识符获取基因类型。
	 *
	 * @param id 基因标识符
	 * @return 基因类型，未找到时返回 {@code null}
	 */
	public static Gene<?> get(Identifier id){
		Gene<?> type=geneticsMap.get(id);
		return type;
	}
	/**
	 * 根据标识符获取基因类型的Codec支持方法（用于Codec解析）。
	 *
	 * @param id 基因标识符
	 * @return 包含基因类型的DataResult
	 */
	private static DataResult<Gene<?>> getGeneType(Identifier id){
		GeneType<?> type=geneticsMap.get(id);
		if(type==null)
			return DataResult.error(()->"Genetic type '"+id+"' not present!");
		return DataResult.success(type);
	}
	/**
	 * 构建按priority排序的显示列表（延迟初始化）。
	 */
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
	
	/**
	 * 获取按命名空间排序的基因类型ID列表。
	 *
	 * @return 可迭代的基因ID列表
	 */
	public static Iterable<Identifier> getGeneTypes(){
		makeIndex();
		return typelist;
	}

	/**
	 * 获取所有已注册的基因类型（无序）。
	 *
	 * @return 基因类型集合
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Collection<Gene<?>> getGeneTypesUnordered(){
		return (Collection)geneticsMap.values();
	}
	public static Map<Identifier, EnumAlleleType<?>> getEnumTypes(){
		return enumAllelesMap;
	}
	/**
	 * 获取按显示优先级排序的基因类型ID列表。
	 *
	 * @return 按优先级排序的基因ID列表
	 */
	public static Iterable<Identifier> getDisplayOrder(){
		makeDisplayList();
		return typelist;
	}

	/**
	 * 获取已注册基因类型的总数。
	 *
	 * @return 基因类型数量
	 */
	public static int size() {
		return geneticsMap.size();
	}
}
