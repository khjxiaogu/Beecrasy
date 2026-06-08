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


/**
 * Beecrasy 基因与突变常量定义类，集中声明所有内置基因类型、等位基因值和突变类型，并触发注册。
 */
public class Genes {
	/**
	 * 等位基因类型及具体值定义。
	 */
	public static class Alleles{
		/** 温度等位基因类型。 */
		public static final EnumAlleleType<Temperature> TEMPERATURE=new EnumAlleleType<>(Beecrasy.rl("temperature"));
		/** 末地维度温度（适应末地）。 */
		public static final Temperature ENDER_TEMPERATURE=TEMPERATURE.registerAllele(new Temperature.DimensionalTemperature("ender",BuiltinDimensionTypes.END));
		/** 下界维度温度（适应下界）。 */
		public static final Temperature NETHER_TEMPERATURE=TEMPERATURE.registerAllele(new Temperature.DimensionalTemperature("nether",BuiltinDimensionTypes.NETHER));
		/** 冰冻温度（-2.0 ~ -0.15）。 */
		public static final Temperature FREEZE_TEMPERATURE=TEMPERATURE.registerAllele(new Temperature.RangedTemperature("freeze",-2f,-.15f));
		/** 寒冷温度（-0.15 ~ 0.4）。 */
		public static final Temperature COLD_TEMPERATURE=TEMPERATURE.registerAllele(new Temperature.RangedTemperature("cold",-.15f,.4f));
		/** 适中温度（0.4 ~ 0.85）。 */
		public static final Temperature MODERATE_TEMPERATURE=TEMPERATURE.registerAllele(new Temperature.RangedTemperature("moderate",.4f,.85f));
		/** 炎热温度（0.85 ~ 2.0）。 */
		public static final Temperature ARDENT_TEMPERATURE=TEMPERATURE.registerAllele(new Temperature.RangedTemperature("ardent",.85f,2f));
		/** 全适应温度（非天然）。 */
		public static final Temperature OMNI_TEMPERATURE=TEMPERATURE.registerAllele(new Temperature.OmniTemperature("omni"));
		

		/** 湿度等位基因类型。 */
		public static final EnumAlleleType<Humidity> HUMIDITY=new EnumAlleleType<>(Beecrasy.rl("humidity"));
		/** 干燥湿度（0 ~ 0.35）。 */
		public static final Humidity DRY_HUMIDITY=HUMIDITY.registerAllele(new Humidity.RangedHumidity("dry",0,.35f));
		/** 适中湿度（0.35 ~ 0.65）。 */
		public static final Humidity MODERATE_HUMIDITY=HUMIDITY.registerAllele(new Humidity.RangedHumidity("moderate",.35f,.65f));
		/** 湿润湿度（0.65 ~ 1.0）。 */
		public static final Humidity WET_HUMIDITY=HUMIDITY.registerAllele(new Humidity.RangedHumidity("wet",.65f,1f));
		/** 全适应湿度（非天然）。 */
		public static final Humidity OMNI_HUMIDITY=HUMIDITY.registerAllele(new Humidity.OmniHumidity("omni"));
		

		
		/** 生境等位基因类型。 */
		public static final EnumAlleleType<Biotope> BIOTOPE=new EnumAlleleType<>(Beecrasy.rl("biotope"));
		/** 野生生境。 */
		public static final Biotope WILD=BIOTOPE.registerAllele(new Biotope("wild"));
		/** 合成生境。 */
		public static final Biotope CRAFT=BIOTOPE.registerAllele(new Biotope("craft"));
		/** 熔炼生境。 */
		public static final Biotope SMELT=BIOTOPE.registerAllele(new Biotope("smelt"));
		/** 共生生境。 */
		public static final Biotope CREATURE=BIOTOPE.registerAllele(new Biotope("creature"));
		
		/** 产量等位基因类型。 */
		public static final EnumAlleleType<NumericAllele> YIELD=new EnumAlleleType<>(Beecrasy.rl("yield"));
		/** 贫乏产量（倍率0.5）。 */
		public static final NumericAllele MEAGER_YIELD=YIELD.registerAllele(new NumericAllele("meager",.5f));
		/** 适中产量（倍率1.0）。 */
		public static final NumericAllele MODERATE_YIELD=YIELD.registerAllele(new NumericAllele("moderate",1f));
		/** 丰富产量（倍率3.0）。 */
		public static final NumericAllele ABUNDANT_YIELD=YIELD.registerAllele(new NumericAllele("abundant",3f));
		/** 丰收产量（倍率5.0）。 */
		public static final NumericAllele BUMPER_YIELD=YIELD.registerAllele(new NumericAllele("bumper",5f));
		
		/** 繁殖力等位基因类型。 */
		public static final EnumAlleleType<NumericAllele> FERTILITY=new EnumAlleleType<>(Beecrasy.rl("fertility"));
		/** 低繁殖力（1子代）。 */
		public static final NumericAllele POOR_FERTILITY=FERTILITY.registerAllele(new NumericAllele("poor",1));
		/** 中繁殖力（2子代）。 */
		public static final NumericAllele MODERATE_FERTILITY=FERTILITY.registerAllele(new NumericAllele("moderate",2));
		/** 高繁殖力（4子代）。 */
		public static final NumericAllele HIGH_FERTILITY=FERTILITY.registerAllele(new NumericAllele("high",4));
		/** 超高繁殖力（6子代）。 */
		public static final NumericAllele PROLIFIC_FERTILITY=FERTILITY.registerAllele(new NumericAllele("prolific",6));
		
		/** 寿命等位基因类型。 */
		public static final EnumAlleleType<NumericAllele> LIFESPAN=new EnumAlleleType<>(Beecrasy.rl("lifespan"));
		/** 有限寿命（倍率0.5）。 */
		public static final NumericAllele LIMITED_LIFESPAN=LIFESPAN.registerAllele(new NumericAllele("limited",0.5f));
		/** 平均寿命（倍率1）。 */
		public static final NumericAllele AVERAGE_LIFESPAN=LIFESPAN.registerAllele(new NumericAllele("average",1));
		/** 较长寿命（倍率2）。 */
		public static final NumericAllele EXTENDED_LIFESPAN=LIFESPAN.registerAllele(new NumericAllele("extended",2));
		/** 极长寿命（倍率4）。 */
		public static final NumericAllele EXCEPTIONAL_LIFESPAN=LIFESPAN.registerAllele(new NumericAllele("exceptional",4));
	}
	/**
	 * 突变类型注册。
	 */
	public static class Mutations{
		/** 序列追加突变。 */
		public static final Mutation APPEND_SEQUENCE=MutationRegistry.register(Beecrasy.rl("append"), new MutationAppendSequence());
		/** 序列削减突变。 */
		public static final Mutation REMOVE_SEQUENCE=MutationRegistry.register(Beecrasy.rl("remove"), new MutationDecreaseSequence());
		/** 合成突变。 */
		public static final Mutation CRAFTING=MutationRegistry.register(Beecrasy.rl("crafting"), new MutationCrafting());
		/** 熔炼突变。 */
		public static final Mutation SMELTING=MutationRegistry.register(Beecrasy.rl("smelting"), new MutationSmelting());
	}
	/** 温度基因。 */
	public static final Gene<Temperature> TEMPERATURE=GeneRegistry.register(Alleles.TEMPERATURE, ()->Alleles.MODERATE_TEMPERATURE, 100);
	/** 湿度基因。 */
	public static final Gene<Humidity> HUMIDITY=GeneRegistry.register(Alleles.HUMIDITY, ()->Alleles.MODERATE_HUMIDITY, 200);
	/** 繁殖力基因。 */
	public static final Gene<NumericAllele> FERTILITY=GeneRegistry.register(Alleles.FERTILITY, ()->Alleles.MODERATE_FERTILITY, 300);
	/** 生境基因。 */
	public static final Gene<Biotope> BIOTOPE=GeneRegistry.register(Alleles.BIOTOPE, ()->Alleles.WILD, 400);
	/** 产品序列基因。 */
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
	/** 产量基因。 */
	public static final Gene<NumericAllele> YIELD=GeneRegistry.register(Alleles.YIELD, ()->Alleles.MEAGER_YIELD, 600);
	/** 寿命基因。 */
	public static final Gene<NumericAllele> LIFESPAN=GeneRegistry.register(Alleles.LIFESPAN, ()->Alleles.AVERAGE_LIFESPAN, 700);
	
	
	/**
	 * 初始化方法，触发类加载以执行所有静态常量定义中的注册操作。
	 */
	public static void init() {
		
	}
}
