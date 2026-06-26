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
