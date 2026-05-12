package com.khjxiaogu.beecrasy.genome.gene;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;

public abstract class Temperature extends BaseAllele {
	public static class RangedTemperature extends Temperature{
		protected float upper;
		protected float lower;
		public RangedTemperature(String id, float lower, float upper) {
			super(id);
			this.upper = upper;
			this.lower = lower;
		}
		@Override
		public boolean isValidFor(Level l, BlockPos pos,Humidity humidity) {
			Holder<Biome> biome=l.getBiomeManager().getNoiseBiomeAtPosition(pos);
			float temp=biome.value().getBaseTemperature();
			return temp>=lower&&temp<=upper&&humidity.isValidFor(l, pos);
		}
	}
	public static class DimensionalTemperature extends Temperature{
		protected final ResourceKey<DimensionType> type;
		public DimensionalTemperature(String id,ResourceKey<DimensionType> type) {
			super(id);
			this.type=type;
		}

		@Override
		public boolean isValidFor(Level l, BlockPos pos,Humidity humidity) {
			return l.dimensionTypeRegistration().is(type);
			
		}
	}
	public static class OmniTemperature extends Temperature{
		public OmniTemperature(String id) {
			super(id);
		}

		@Override
		public boolean isValidFor(Level l, BlockPos pos,Humidity humidity) {
			return humidity.isValidFor(l, pos);
			
		}
	}
	public Temperature(String id) {
		super(id);
	}
	public abstract boolean isValidFor(Level l,BlockPos pos,Humidity humidity);
}
