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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterRegistry.BeehiveParameterType;
import com.khjxiaogu.beecrasy.genome.gene.Biotope;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public record BeeHiveParameterSet(ServerLevel level,BlockPos position,Set<Identifier> disabledMutation,Map<BeehiveParameterType<?>,Object> params,Set<Biotope> activeBiotopes) {
	public static class BeehiveWorkingParams implements ValueIOSerializable{
		private Map<BeehiveParameterType<?>,Object> params=new HashMap<>();
		
		public void addParams(Map<BeehiveParameterType<?>,Object> params) {
			for(Entry<BeehiveParameterType<?>, Object> ent:params.entrySet())
				ent.getKey().mergeTo(params,ent.getValue());
		}

		@Override
		public void serialize(ValueOutput output) {
			output.store("params", BeeHiveParameterRegistry.COMPOSITE_CODEC, params);
			
		}

		@Override
		public void deserialize(ValueInput input) {
			Optional<Map<BeehiveParameterType<?>, Object>> op=input.read("params", BeeHiveParameterRegistry.COMPOSITE_CODEC);
			if(op.isPresent()) {
				params=op.get();
			}
		}
	}
	public static class Builder{
		ServerLevel level;
		BlockPos position;
		Set<Identifier> disabledMutation=new HashSet<>();
		Map<BeehiveParameterType<?>,Object> params=new HashMap<>();
		Set<Biotope> activeBiotopes=new HashSet<>();
		public Builder(ServerLevel level, BlockPos position) {
			super();
			this.level = level;
			this.position = position;
		}
		public Builder disableMutation(Identifier id) {
			disabledMutation.add(id);
			return this;
		};
		public Builder addParams(Map<BeehiveParameterType<?>,Object> params) {
			for(Entry<BeehiveParameterType<?>, Object> ent:params.entrySet())
				ent.getKey().mergeTo(this.params,ent.getValue());
			return this;
		}
		public Builder setParams(Map<BeehiveParameterType<?>,Object> params) {
			this.params.putAll(params);
			return this;
		}
		public Builder addBiotopes(Collection<Biotope> biotopes) {
			activeBiotopes.addAll(biotopes);
			return this;
		}
		public Builder addBiotopes(Biotope biotope) {
			activeBiotopes.add(biotope);
			return this;
		}
		@SuppressWarnings("unchecked")
		public <T> T getParamValue(BeehiveParameterType<T> type) {
			return (T) params.get(type);
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public BeeHiveParameterSet build() {
			for(Entry<BeehiveParameterType<?>, Object> ent:params.entrySet()) {
				ent.setValue(((BeehiveParameterType)ent.getKey()).mergeToDefault(ent.getValue()));
			}
			return new BeeHiveParameterSet(level,position,Set.copyOf(disabledMutation),Map.copyOf(params),activeBiotopes);
		}
	}
	@SuppressWarnings("unchecked")
	public <T> T getParamValue(BeehiveParameterType<T> type) {
		T val= (T) params.get(type);
		if(val==null)
			val=type.getDefault();
		return val;
	}
	public boolean hasBiotope(Set<Biotope> current,Biotope biotope) {
		return activeBiotopes.contains(biotope)||current.contains(biotope);
	}
}
