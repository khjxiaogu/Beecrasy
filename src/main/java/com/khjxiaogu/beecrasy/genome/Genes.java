package com.khjxiaogu.beecrasy.genome;

import java.util.List;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.genome.gene.BaseAllele;
import com.khjxiaogu.beecrasy.genome.gene.EnumAlleleType;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;
import com.mojang.serialization.Codec;


public class Genes {
	public static class Alleles{
		public static final EnumAlleleType<BaseAllele> BIOTOPE=new EnumAlleleType<>();
		public static final BaseAllele WILD=BIOTOPE.registerAllele(new BaseAllele("wild"));
		public static final BaseAllele CRAFT=BIOTOPE.registerAllele(new BaseAllele("craft"));
		public static final BaseAllele SMELT=BIOTOPE.registerAllele(new BaseAllele("smelt"));
		public static final BaseAllele CREATURE=BIOTOPE.registerAllele(new BaseAllele("creature"));
	}
	public static final Gene<Integer> TEMPERATURE=GeneRegistry.register(Beecrasy.rl("temperature"), Codec.INT, ()->5, 100);
	public static final Gene<Integer> HUMIDITY=GeneRegistry.register(Beecrasy.rl("humidity"), Codec.INT, ()->5, 200);
	public static final Gene<Integer> FERTILE=GeneRegistry.register(Beecrasy.rl("fertile"), Codec.INT, ()->2, 300);
	public static final Gene<BaseAllele> BIOTOPE=GeneRegistry.register(Beecrasy.rl("biotope"), Alleles.BIOTOPE.CODEC, ()->Alleles.WILD, 400);
	public static final Gene<List<ProductItem>> PRODUCTS=GeneRegistry.register(Beecrasy.rl("product"), Codec.list(ProductItem.CODEC), ()->List.of(), 500);
	public static final Gene<Integer> YIELD=GeneRegistry.register(Beecrasy.rl("yield"), Codec.INT, ()->1, 600);
	public static final Gene<Integer> LIFESPAN=GeneRegistry.register(Beecrasy.rl("lifespan"), Codec.INT, ()->10, 700);
	
	
	public static void init() {
		
	}
}
