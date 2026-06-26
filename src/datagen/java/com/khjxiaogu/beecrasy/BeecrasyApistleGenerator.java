package com.khjxiaogu.beecrasy;

import java.util.concurrent.CompletableFuture;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.data.ApistleGenerator;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

public class BeecrasyApistleGenerator extends ApistleGenerator {

	public BeecrasyApistleGenerator(PackOutput output, CompletableFuture<Provider> lookupProvider) {
		super(output, lookupProvider, Beecrasy.MODID);
	}

	@Override
	protected void addPages() {
		/*this.add("example", "Example page")
		.hr(123)
		.image(Identifier.withDefaultNamespace("example"), 100, 100)
		.item(Items.QUEEN_BEE)
		.item(Items.BEESWAX,Items.PRODUCT_COMB)
		.space(7)
		.text("123",2,true)
		.setOrder(100)
		.setIcon(Items.APITE)
		;*/
		
	}

}
