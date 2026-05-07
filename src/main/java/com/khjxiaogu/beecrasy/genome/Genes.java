package com.khjxiaogu.beecrasy.genome;

import java.util.List;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;
import com.mojang.serialization.Codec;

import net.minecraft.resources.Identifier;


public class Genes {
	public static final Gene<Integer> TEMPERATURE=GeneRegistry.register(Beecrasy.rl("temperature"), Codec.INT, ()->5, 100);
	public static final Gene<Integer> HUMIDITY=GeneRegistry.register(Beecrasy.rl("humidity"), Codec.INT, ()->5, 200);
	public static final Gene<Integer> FERTILE=GeneRegistry.register(Beecrasy.rl("fertile"), Codec.INT, ()->2, 300);
	public static final Gene<Identifier> BIOTOPE=GeneRegistry.register(Beecrasy.rl("biotope"), Identifier.CODEC, ()->Beecrasy.rl("wild"), 400);
	public static final Gene<List<ProductItem>> PRODUCTS=GeneRegistry.register(Beecrasy.rl("product"), Codec.list(ProductItem.CODEC), ()->List.of(), 500);
	public static final Gene<Integer> YIELD=GeneRegistry.register(Beecrasy.rl("yield"), Codec.INT, ()->1, 600);
	public static final Gene<Integer> LIFESPAN=GeneRegistry.register(Beecrasy.rl("lifespan"), Codec.INT, ()->10, 700);
	
	
	public static void init() {
		
	}
}
