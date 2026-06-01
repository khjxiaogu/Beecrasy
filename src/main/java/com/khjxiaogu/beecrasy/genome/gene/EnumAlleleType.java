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

public class EnumAlleleType<T extends Allele> implements Iterable<T>{
	private final Identifier id;

	private Map<String,T> alleleType=new LinkedHashMap<>(10);
	private Map<T,Translation> alleleName=new IdentityHashMap<>(10);
	private List<String> typelist=new ArrayList<>(10);
	private Reference2IntOpenHashMap<T> typeId=new Reference2IntOpenHashMap<>(10);
	private volatile boolean sorted=false;
	private Object lock=new Object();
	public final Codec<T> CODEC=Codec.STRING.comapFlatMap(this::getAlleleType, t->t.getId());
	public final StreamCodec<RegistryFriendlyByteBuf,T> STREAM_CODEC=ByteBufCodecs.idMapper(this::getByInt, this::getIntId).cast();
	public EnumAlleleType(Identifier id) {
		super();
		this.id = id;
	}
	public Identifier getId() {
		return id;
	}
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
	public String getLanguageKey(T allele) {
		return alleleName.getOrDefault(allele,Translation.MISSING).key();
	}
	public String getShortLanguageKey(T allele) {
		return alleleName.getOrDefault(allele,Translation.MISSING).shortKey();
	}
	public Component getReadableText(T allele) {
		return (alleleName.getOrDefault(allele,Translation.MISSING).component());
	}
	public Component getShortReadableText(T allele) {
		return (alleleName.getOrDefault(allele,Translation.MISSING).shortComponent());
	}
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
	public T getByInt(int num) {
		makeIndex();
		return alleleType.get(typelist.get(num));
	}
	public String getId(T obj) {
		for(Entry<String, T> pair:alleleType.entrySet()) {
			if(pair.getValue()==obj)
				return pair.getKey();
		}
		return "unknown";
	}
	public int getIntId(T obj) {
		makeIndex();
		return typeId.getOrDefault(obj,-1);
	}
	public DataResult<T> getAlleleType(String id){
		T type=alleleType.get(id);
		if(type==null)
			return DataResult.error(()->"Allele '"+id+"' not present!");
		return DataResult.success(type);
	}
	@Override
	public Iterator<T> iterator() {
		return alleleType.values().iterator();
	}
}
