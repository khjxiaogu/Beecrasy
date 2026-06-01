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

package com.khjxiaogu.beecrasy.genome;

import java.util.List;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.genome.gene.Biotope;
import com.khjxiaogu.beecrasy.genome.gene.EnumAlleleType;
import com.khjxiaogu.beecrasy.genome.gene.Humidity;
import com.khjxiaogu.beecrasy.genome.gene.NumericAllele;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;
import com.khjxiaogu.beecrasy.genome.gene.Temperature;
import com.khjxiaogu.beecrasy.genome.mutation.MutationAppendSequence;
import com.khjxiaogu.beecrasy.genome.mutation.MutationCrafting;
import com.khjxiaogu.beecrasy.genome.mutation.MutationDecreaseSequence;
import com.khjxiaogu.beecrasy.genome.mutation.MutationSmelting;
import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;


public class Genes {
	public static class Alleles{
		public static final EnumAlleleType<Temperature> TEMPERATURE=new EnumAlleleType<>(Beecrasy.rl("temperature"));
		public static final Temperature ENDER_TEMPERATURE=TEMPERATURE.registerAllele(new Temperature.DimensionalTemperature("ender",BuiltinDimensionTypes.END));
		public static final Temperature NETHER_TEMPERATURE=TEMPERATURE.registerAllele(new Temperature.DimensionalTemperature("nether",BuiltinDimensionTypes.NETHER));
		public static final Temperature FREEZE_TEMPERATURE=TEMPERATURE.registerAllele(new Temperature.RangedTemperature("freeze",-2f,-.15f));
		public static final Temperature COLD_TEMPERATURE=TEMPERATURE.registerAllele(new Temperature.RangedTemperature("cold",-.15f,.4f));
		public static final Temperature MODERATE_TEMPERATURE=TEMPERATURE.registerAllele(new Temperature.RangedTemperature("moderate",.4f,.85f));
		public static final Temperature ARDENT_TEMPERATURE=TEMPERATURE.registerAllele(new Temperature.RangedTemperature("ardent",.85f,2f));
		public static final Temperature OMNI_TEMPERATURE=TEMPERATURE.registerAllele(new Temperature.OmniTemperature("omni"));
		

		public static final EnumAlleleType<Humidity> HUMIDITY=new EnumAlleleType<>(Beecrasy.rl("humidity"));
		public static final Humidity DRY_HUMIDITY=HUMIDITY.registerAllele(new Humidity.RangedHumidity("dry",0,.35f));
		public static final Humidity MODERATE_HUMIDITY=HUMIDITY.registerAllele(new Humidity.RangedHumidity("moderate",.35f,.65f));
		public static final Humidity WET_HUMIDITY=HUMIDITY.registerAllele(new Humidity.RangedHumidity("wet",.65f,1f));
		public static final Humidity OMNI_HUMIDITY=HUMIDITY.registerAllele(new Humidity.OmniHumidity("omni"));
		

		
		public static final EnumAlleleType<Biotope> BIOTOPE=new EnumAlleleType<>(Beecrasy.rl("biotope"));
		public static final Biotope WILD=BIOTOPE.registerAllele(new Biotope("wild"));
		public static final Biotope CRAFT=BIOTOPE.registerAllele(new Biotope("craft"));
		public static final Biotope SMELT=BIOTOPE.registerAllele(new Biotope("smelt"));
		public static final Biotope CREATURE=BIOTOPE.registerAllele(new Biotope("creature"));
		
		public static final EnumAlleleType<NumericAllele> YIELD=new EnumAlleleType<>(Beecrasy.rl("yield"));
		public static final NumericAllele MEAGER_YIELD=YIELD.registerAllele(new NumericAllele("meager",.5f));
		public static final NumericAllele MODERATE_YIELD=YIELD.registerAllele(new NumericAllele("moderate",1f));
		public static final NumericAllele ABUNDANT_YIELD=YIELD.registerAllele(new NumericAllele("abundant",3f));
		public static final NumericAllele BUMPER_YIELD=YIELD.registerAllele(new NumericAllele("bumper",5f));
		
		public static final EnumAlleleType<NumericAllele> FERTILITY=new EnumAlleleType<>(Beecrasy.rl("fertility"));
		public static final NumericAllele POOR_FERTILITY=FERTILITY.registerAllele(new NumericAllele("poor",1));
		public static final NumericAllele MODERATE_FERTILITY=FERTILITY.registerAllele(new NumericAllele("moderate",2));
		public static final NumericAllele HIGH_FERTILITY=FERTILITY.registerAllele(new NumericAllele("high",4));
		public static final NumericAllele PROLIFIC_FERTILITY=FERTILITY.registerAllele(new NumericAllele("prolific",6));
		
		public static final EnumAlleleType<NumericAllele> LIFESPAN=new EnumAlleleType<>(Beecrasy.rl("lifespan"));
		public static final NumericAllele LIMITED_LIFESPAN=LIFESPAN.registerAllele(new NumericAllele("limited",0.5f));
		public static final NumericAllele AVERAGE_LIFESPAN=LIFESPAN.registerAllele(new NumericAllele("average",1));
		public static final NumericAllele EXTENDED_LIFESPAN=LIFESPAN.registerAllele(new NumericAllele("extended",2));
		public static final NumericAllele EXCEPTIONAL_LIFESPAN=LIFESPAN.registerAllele(new NumericAllele("exceptional",4));
	}
	public static class Mutations{
		public static final Mutation APPEND_SEQUENCE=MutationRegistry.register(Beecrasy.rl("append"), new MutationAppendSequence());
		public static final Mutation REMOVE_SEQUENCE=MutationRegistry.register(Beecrasy.rl("remove"), new MutationDecreaseSequence());
		public static final Mutation CRAFTING=MutationRegistry.register(Beecrasy.rl("crafting"), new MutationCrafting());
		public static final Mutation SMELTING=MutationRegistry.register(Beecrasy.rl("smelting"), new MutationSmelting());
	}
	public static final Gene<Temperature> TEMPERATURE=GeneRegistry.register(Alleles.TEMPERATURE, ()->Alleles.MODERATE_TEMPERATURE, 100);
	public static final Gene<Humidity> HUMIDITY=GeneRegistry.register(Alleles.HUMIDITY, ()->Alleles.MODERATE_HUMIDITY, 200);
	public static final Gene<NumericAllele> FERTILITY=GeneRegistry.register(Alleles.FERTILITY, ()->Alleles.MODERATE_FERTILITY, 300);
	public static final Gene<Biotope> BIOTOPE=GeneRegistry.register(Alleles.BIOTOPE, ()->Alleles.WILD, 400);
	public static final Gene<List<ProductItem>> PRODUCTS=GeneRegistry.register(Beecrasy.rl("product"),
		Codec.list(ProductItem.CODEC),
		ProductItem.STREAM_CODEC.apply(ByteBufCodecs.list()),
		(t)->{
				if(t.isEmpty())
					return Component.translatable("allele.beecrasy.product.empty");
				else
					return t.get(0).stack().get(DataComponents.ITEM_NAME);
			}
		, ()->List.of(), 500);
	public static final Gene<NumericAllele> YIELD=GeneRegistry.register(Alleles.YIELD, ()->Alleles.MEAGER_YIELD, 600);
	public static final Gene<NumericAllele> LIFESPAN=GeneRegistry.register(Alleles.LIFESPAN, ()->Alleles.AVERAGE_LIFESPAN, 700);
	
	
	public static void init() {
		
	}
}
