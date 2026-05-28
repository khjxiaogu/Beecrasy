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

package com.khjxiaogu.beecrasy.components;

import java.util.HashMap;
import java.util.Map;

import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterRegistry;
import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterRegistry.BeehiveParameterType;
import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class BeeHiveArgumentation {
	public static final Codec<BeeHiveArgumentation> CODEC=BeeHiveParameterRegistry.COMPOSITE_CODEC.xmap(BeeHiveArgumentation::new, BeeHiveArgumentation::params);
	public static final StreamCodec<ByteBuf,BeeHiveArgumentation> STREAM_CODEC=BeeHiveParameterRegistry.COMPOSITE_STREAM_CODEC.map(BeeHiveArgumentation::new, BeeHiveArgumentation::params);
	
	private final Map<BeehiveParameterType<?>,Object> params;

	public BeeHiveArgumentation(Map<BeehiveParameterType<?>, Object> params) {
		super();
		this.params = params;
	}
	public Map<BeehiveParameterType<?>, Object> params() {
		return params;
	}
	public static class Builder{
		private Map<BeehiveParameterType<?>,Object> params=new HashMap<>();
		public Builder setParam(BeehiveParameterType<?> key,Object value) {
			params.put(key, value);
			return this;
		}
		public Builder setParams(Map<BeehiveParameterType<?>,Object> params) {
			this.params.putAll(params);
			return this;
		}
		public BeeHiveArgumentation build() {
			return new BeeHiveArgumentation(Map.copyOf(params));
		}

	}
}
