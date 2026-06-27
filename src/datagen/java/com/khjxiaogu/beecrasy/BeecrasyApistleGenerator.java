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

package com.khjxiaogu.beecrasy;

import java.util.concurrent.CompletableFuture;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
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
		this.addCommon("example", "Example page1")
		.text("1")
		.hr()
		.image(Identifier.withDefaultNamespace("example"), 100, 100)
		.item(Items.QUEEN_BEE)
		.item(Items.BEESWAX,Items.PRODUCT_COMB)
		.space(200)
		.text("123",2,true)
		.setOrder(100)
		.setIcon(Items.APITE)
		;
		this.addCommon("example2", "Example page1")
		.text("2")
		.hr()
		.image(Identifier.withDefaultNamespace("example"), 100, 100)
		.item(Items.QUEEN_BEE)
		.item(Items.BEESWAX,Items.PRODUCT_COMB)
		.space(200)
		.text("123",2,true)
		.setOrder(100)
		.setIcon(Items.APITE)
		;
		this.addCommon("example3", "Example page1")
		.text("3")
		.hr()
		.image(Identifier.withDefaultNamespace("example"), 100, 100)
		.item(Items.QUEEN_BEE)
		.item(Items.BEESWAX,Items.PRODUCT_COMB)
		.space(200)
		.text("123",2,true)
		.setOrder(100)
		.setIcon(Items.APITE)
		;
		this.addCommon("example4", "Example page1")
		.text("4")
		.hr()
		.image(Identifier.withDefaultNamespace("example"), 100, 100)
		.item(Items.QUEEN_BEE)
		.item(Items.BEESWAX,Items.PRODUCT_COMB)
		.space(200)
		.text("123",2,true)
		.setOrder(100)
		.setIcon(Items.APITE)
		;
		this.addCommon("example5", "Example page1")
		.text("5")
		.hr()
		.image(Identifier.withDefaultNamespace("example"), 100, 100)
		.item(Items.QUEEN_BEE)
		.item(Items.BEESWAX,Items.PRODUCT_COMB)
		.space(200)
		.text("123",2,true)
		.setOrder(100)
		.setIcon(Items.APITE)
		;
		this.addCommon("example6", "Example page1")
		.text("6")
		.hr()
		.image(Identifier.withDefaultNamespace("example"), 100, 100)
		.item(Items.QUEEN_BEE)
		.item(Items.BEESWAX,Items.PRODUCT_COMB)
		.space(200)
		.text("123",2,true)
		.setOrder(100)
		.setIcon(Items.APITE)
		;
		this.addCommon("example7", "Example page1")
		.text("7")
		.hr()
		.image(Identifier.withDefaultNamespace("example"), 100, 100)
		.item(Items.QUEEN_BEE)
		.item(Items.BEESWAX,Items.PRODUCT_COMB)
		.space(200)
		.text("123",2,true)
		.setOrder(100)
		.setIcon(Items.APITE)
		;
		this.addCommon("example8", "Example page1")
		.text("8")
		.hr()
		.image(Identifier.withDefaultNamespace("example"), 100, 100)
		.item(Items.QUEEN_BEE)
		.item(Items.BEESWAX,Items.PRODUCT_COMB)
		.space(200)
		.text("123",2,true)
		.setOrder(100)
		.setIcon(Items.APITE)
		;
		this.addCommon("example9", "Example page1")
		.text("9")
		.hr()
		.image(Identifier.withDefaultNamespace("example"), 100, 100)
		.item(Items.QUEEN_BEE)
		.item(Items.BEESWAX,Items.PRODUCT_COMB)
		.space(200)
		.text("123",2,true)
		.setOrder(100)
		.setIcon(Items.APITE)
		;
		this.addCommon("example10", "Example page1")
		.text("10")
		.hr()
		.image(Identifier.withDefaultNamespace("example"), 100, 100)
		.item(Items.QUEEN_BEE)
		.item(Items.BEESWAX,Items.PRODUCT_COMB)
		.space(200)
		.text("123",2,true)
		.setOrder(100)
		.setIcon(Items.PRODUCT_COMB)
		;
		this.addCommon("example11", "Example page1")
		.text("11")
		.hr()
		.image(Identifier.withDefaultNamespace("example"), 100, 100)
		.item(Items.QUEEN_BEE)
		.item(Items.BEESWAX,Items.PRODUCT_COMB)
		.space(200)
		.text("123",2,true)
		.setOrder(100)
		.setIcon(Blocks.HONEY_PRESS.asItem())
		;
		this.addCommon("example12", "Example page1")
		.text("12")
		.hr()
		.image(Identifier.withDefaultNamespace("example"), 100, 100)
		.item(Items.QUEEN_BEE)
		.item(Items.BEESWAX,Items.PRODUCT_COMB)
		.space(200)
		.text("123",2,true)
		.setOrder(100)
		.setIcon(Items.SEQUENCER.asItem())
		;
		this.addCommon("example13", "Example page1")
		.text("13")
		.hr()
		.image(Identifier.withDefaultNamespace("example"), 100, 100)
		.item(Items.QUEEN_BEE)
		.item(Items.BEESWAX,Items.PRODUCT_COMB)
		.space(200)
		.text("123",2,true)
		.setOrder(100)
		.setIcon(Blocks.SKEP.asItem())
		;
	}

}
