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

package com.khjxiaogu.beecrasy.genome.gene;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.khjxiaogu.beecrasy.genome.Translation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/**
 * 枚举型等位基因类型，管理一组具名等位基因值的注册、索引、Codec编解码和文本展示。实现 {@link Iterable} 接口。
 *
 * @param <T> 等位基因类型
 */
public class EnumAlleleType<T extends Allele> implements Iterable<T>{
	/** 该类型的标识符。 */
	private final Identifier id;

	/** ID到等位基因的映射表。 */
	private Map<String,T> alleleType=new LinkedHashMap<>(10);
	/** 等位基因到翻译记录的映射表。 */
	private Map<T,Translation> alleleName=new IdentityHashMap<>(10);
	/** 按注册顺序排列的ID列表。 */
	private List<String> typelist=new ArrayList<>(10);
	/** 等位基因到整数ID的映射表。 */
	private Reference2IntOpenHashMap<T> typeId=new Reference2IntOpenHashMap<>(10);
	/** 索引是否已排序。 */
	private volatile boolean sorted=false;
	/** 索引构建锁。 */
	private Object lock=new Object();
	/** 等位基因的 {@link Codec} 编解码器，基于字符串ID编解码。 */
	public final Codec<T> CODEC=Codec.STRING.comapFlatMap(this::getAlleleType, t->t.getId());
	/** 等位基因的 {@link StreamCodec} 流编解码器，基于整数ID编解码。 */
	public final StreamCodec<RegistryFriendlyByteBuf,T> STREAM_CODEC=ByteBufCodecs.idMapper(this::getByInt, this::getIntId).cast();
	/**
	 * 创建枚举型等位基因类型。
	 *
	 * @param id 类型标识符
	 */
	public EnumAlleleType(Identifier id) {
		super();
		this.id = id;
	}
	/**
	 * 获取该类型的标识符。
	 *
	 * @return 标识符
	 */
	public Identifier getId() {
		return id;
	}
	/**
	 * 注册一个等位基因值。
	 *
	 * @param <O>    等位基因具体类型
	 * @param allele 待注册的等位基因
	 * @return 注册后的等位基因
	 */
	public synchronized <O extends T> O registerAllele(O allele) {
		String id=allele.getId();
		if(!alleleType.containsKey(id)) {
			synchronized(lock) {
				typelist.add(id);
				sorted=false;
			}
		}
		alleleType.put(id, allele);
		alleleName.put(allele, new Translation(this.id.toLanguageKey("allele", id)));
		return allele;
	}
	/**
	 * 获取指定等位基因的完整语言键。
	 *
	 * @param allele 等位基因
	 * @return 语言键字符串
	 */
	public String getLanguageKey(T allele) {
		return alleleName.getOrDefault(allele,Translation.MISSING).key();
	}
	/**
	 * 获取指定等位基因的简写语言键。
	 *
	 * @param allele 等位基因
	 * @return 简写语言键字符串
	 */
	public String getShortLanguageKey(T allele) {
		return alleleName.getOrDefault(allele,Translation.MISSING).shortKey();
	}
	/**
	 * 获取指定等位基因的可读文本组件。
	 *
	 * @param allele 等位基因
	 * @return 可读文本组件
	 */
	public Component getReadableText(T allele) {
		return (alleleName.getOrDefault(allele,Translation.MISSING).component());
	}
	/**
	 * 获取指定等位基因的简写可读文本组件。
	 *
	 * @param allele 等位基因
	 * @return 简写可读文本组件
	 */
	public Component getShortReadableText(T allele) {
		return (alleleName.getOrDefault(allele,Translation.MISSING).shortComponent());
	}
	/**
	 * 构建按ID排序的索引（延迟初始化）。
	 */
	private void makeIndex() {
		if(!sorted) {
			synchronized(lock) {
				if(!sorted) {
					typelist.sort(null);
					typeId.clear();
					for(int i=0;i<typelist.size();i++) {
						typeId.put(alleleType.get(typelist.get(i)), i);
					}
					sorted=true;
				}
			}
		}
	}
	/**
	 * 根据整数ID获取等位基因。
	 *
	 * @param num 整数ID
	 * @return 等位基因
	 */
	public T getByInt(int num) {
		makeIndex();
		return alleleType.get(typelist.get(num));
	}
	/**
	 * 根据等位基因对象获取其字符串ID。
	 *
	 * @param obj 等位基因
	 * @return 字符串ID，未找到时返回 {@code "unknown"}
	 */
	public String getId(T obj) {
		for(Entry<String, T> pair:alleleType.entrySet()) {
			if(pair.getValue()==obj)
				return pair.getKey();
		}
		return "unknown";
	}
	/**
	 * 根据等位基因获取其整数ID。
	 *
	 * @param obj 等位基因
	 * @return 整数ID，未找到时返回 -1
	 */
	public int getIntId(T obj) {
		makeIndex();
		return typeId.getOrDefault(obj,-1);
	}
	/**
	 * 根据字符串ID解析等位基因（用于Codec编解码）。
	 *
	 * @param id 字符串ID
	 * @return 解析结果
	 */
	public DataResult<T> getAlleleType(String id){
		T type=alleleType.get(id);
		if(type==null)
			return DataResult.error(()->"Allele '"+id+"' not present!");
		return DataResult.success(type);
	}
	@Override
	public Iterator<T> iterator() {
		makeIndex();
		return typelist.stream().map(alleleType::get).iterator();
	}
	public int size() {
		return alleleType.size();
	}
}
