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
	}
	public Humidity(String id) {
		super(id);
	}
	public abstract boolean isValidFor(Level l, BlockPos pos);

}
