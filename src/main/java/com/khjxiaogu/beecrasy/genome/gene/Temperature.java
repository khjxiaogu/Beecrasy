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

import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterSet;
import com.khjxiaogu.beecrasy.beehive.BeeHiveParameters;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;

/**
 * 温度等位基因抽象类，判断给定蜂箱参数（综合温度与湿度）是否符合温度要求。
 */
public abstract class Temperature extends BaseAllele {
	/**
	 * 基于温度值范围的温度等位基因。
	 */
	public static class RangedTemperature extends Temperature{
		/** 温度上限。 */
		protected float upper;
		/** 温度下限。 */
		protected float lower;
		/**
		 * 创建范围型温度等位基因。
		 *
		 * @param id    标识符
		 * @param lower 温度下限
		 * @param upper 温度上限
		 */
		public RangedTemperature(String id, float lower, float upper) {
			super(id);
			this.upper = upper;
			this.lower = lower;
		}
		@Override
		public boolean isValidFor(BeeHiveParameterSet params,Humidity humidity) {
			float temp=params.biome().value().getBaseTemperature();
			float offset=params.getParamValue(BeeHiveParameters.TEMPERATURE);
			temp-=offset;
			return temp>=lower&&temp<=upper&&humidity.isValidFor(params);
		}
	}
	/**
	 * 基于维度类型的温度等位基因。
	 */
	public static class DimensionalTemperature extends Temperature{
		/** 关联的维度类型。 */
		protected final ResourceKey<DimensionType> type;
		/**
		 * 创建维度型温度等位基因。
		 *
		 * @param id   标识符
		 * @param type 维度类型键
		 */
		public DimensionalTemperature(String id,ResourceKey<DimensionType> type) {
			super(id);
			this.type=type;
		}

		@Override
		public boolean isValidFor(BeeHiveParameterSet params,Humidity humidity) {
			return params.type().is(type);
			
		}
		@Override
		public boolean isNatural() {
			return false;
		}
	}
	/**
	 * 全适应型温度等位基因，仅委托湿度进行判断。
	 */
	public static class OmniTemperature extends Temperature{
		/**
		 * 创建全适应型温度等位基因。
		 *
		 * @param id 标识符
		 */
		public OmniTemperature(String id) {
			super(id);
		}

		@Override
		public boolean isValidFor(BeeHiveParameterSet params,Humidity humidity) {
			return humidity.isValidFor(params);
			
		}
		@Override
		public boolean isNatural() {
			return false;
		}
	}
	/**
	 * 创建温度等位基因。
	 *
	 * @param id 标识符
	 */
	public Temperature(String id) {
		super(id);
	}
	/**
	 * 检查温度和湿度对当前环境是否有效。
	 *
	 * @param params   蜂箱参数集合
	 * @param humidity 湿度等位基因
	 * @return 如果当前环境符合温度湿度要求则返回 {@code true}
	 */
	public abstract boolean isValidFor(BeeHiveParameterSet params,Humidity humidity);
	/**
	 * 是否为自然产生的等位基因。
	 *
	 * @return 自然产生的返回 {@code true}（默认）
	 */
	public boolean isNatural() {
		return true;
	}
}
