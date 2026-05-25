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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public abstract class Humidity extends BaseAllele {
	public static class RangedHumidity extends Humidity{
		protected float upper;
		protected float lower;
		public RangedHumidity(String id, float lower, float upper) {
			super(id);
			this.upper = upper;
			this.lower = lower;
		}
		@Override
		public boolean isValidFor(Level l, BlockPos pos) {
			Holder<Biome> biome=l.getBiomeManager().getNoiseBiomeAtPosition(pos);
			float humid=biome.value().getModifiedClimateSettings().downfall();
			return humid>=lower&&humid<=upper;
		}
	}
	public static class OmniHumidity extends Humidity{
		public OmniHumidity(String id) {
			super(id);
		}
		@Override
		public boolean isValidFor(Level l, BlockPos pos) {
			return true;
		}
		@Override
		public boolean isNatural() {
			return false;
		}
	}
	public Humidity(String id) {
		super(id);
	}
	public abstract boolean isValidFor(Level l, BlockPos pos);
	public boolean isNatural() {
		return true;
	}
}
