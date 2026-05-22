/** 
* Copyright (c) 2026 khjxiaogu
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class EnumAlleleType<T extends Allele> {
	private Map<String,T> alleleType=new HashMap<>();
	private List<String> typelist=new ArrayList<>();
	private Reference2IntOpenHashMap<T> typeId=new Reference2IntOpenHashMap<>();
	private volatile boolean sorted=false;
	private Object lock=new Object();
	public final Codec<T> CODEC=Codec.STRING.comapFlatMap(this::getAlleleType, t->t.getId());
	public final StreamCodec<RegistryFriendlyByteBuf,T> STREAM_CODEC=ByteBufCodecs.idMapper(this::getByInt, this::getId).cast();
	public synchronized <O extends T> O registerAllele(O allele) {
		String id=allele.getId();
		if(!alleleType.containsKey(id)) {
			synchronized(lock) {
				typelist.add(id);
				sorted=false;
			}
		}
		alleleType.put(id, allele);
		return allele;
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
	public int getId(T obj) {
		makeIndex();
		return typeId.getOrDefault(obj,-1);
	}
	public DataResult<T> getAlleleType(String id){
		T type=alleleType.get(id);
		if(type==null)
			return DataResult.error(()->"Allele '"+id+"' not present!");
		return DataResult.success(type);
	}
}
