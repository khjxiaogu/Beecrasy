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

/**
 * 湿度等位基因抽象类，判断给定蜂箱参数是否符合湿度要求。
 */
public abstract class Humidity extends BaseAllele {
	/**
	 * 基于湿度值范围的湿度等位基因。
	 */
	public static class RangedHumidity extends Humidity{
		/** 湿度上限。 */
		protected float upper;
		/** 湿度下限。 */
		protected float lower;
		/**
		 * 创建范围型湿度等位基因。
		 *
		 * @param id    标识符
		 * @param lower 湿度下限
		 * @param upper 湿度上限
		 */
		public RangedHumidity(String id, float lower, float upper) {
			super(id);
			this.upper = upper;
			this.lower = lower;
		}
		@Override
		public boolean isValidFor(BeeHiveParameterSet params) {
			float humid=params.biome().value().getModifiedClimateSettings().downfall();

			float offset=params.getParamValue(BeeHiveParameters.HUMIDITY);
			humid-=offset;
			return humid>=lower&&humid<=upper;
		}
	}
	/**
	 * 全适应型湿度等位基因，始终返回有效。
	 */
	public static class OmniHumidity extends Humidity{
		/**
		 * 创建全适应型湿度等位基因。
		 *
		 * @param id 标识符
		 */
		public OmniHumidity(String id) {
			super(id);
		}
		@Override
		public boolean isValidFor(BeeHiveParameterSet params) {
			return true;
		}
		@Override
		public boolean isNatural() {
			return false;
		}
	}
	/**
	 * 创建湿度等位基因。
	 *
	 * @param id 标识符
	 */
	public Humidity(String id) {
		super(id);
	}
	/**
	 * 检查湿度对当前环境是否有效。
	 *
	 * @param params 蜂箱参数集合
	 * @return 如果当前环境符合湿度要求则返回 {@code true}
	 */
	public abstract boolean isValidFor(BeeHiveParameterSet params);
	/**
	 * 是否为自然产生的等位基因。
	 *
	 * @return 自然产生的返回 {@code true}（默认）
	 */
	public boolean isNatural() {
		return true;
	}
}
