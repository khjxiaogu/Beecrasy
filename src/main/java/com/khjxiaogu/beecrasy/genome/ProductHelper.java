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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.genome.gene.Biotope;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public class ProductHelper {
	public static record ProductWithCount(ProductItem product,int count) {}
	public static List<ProductWithCount> pickProduct(Biotope biotope,Collection<ProductItem> product,RandomSource rand,int count) {
		List<ProductItem> products=filterProduct(biotope,product);
		if(products.size()==0)
			return List.of();
		if(products.size()==1) {
			return List.of(new ProductWithCount(products.get(0),count));
		}
		List<ProductWithCount> out=new ArrayList<>(products.size());
		int[] nums=new int[products.size()];
		for(int i=0;i<count;i++) {
			nums[rand.nextInt(nums.length)]++;
		}
		for(int i=0;i<nums.length;i++) {
			if(nums[i]>0) {
				out.add(new ProductWithCount(products.get(i),nums[i]));
			}
		}
		return out;
	}
	public static ProductWithCount pickSingleProduct(Biotope biotope,Collection<ProductItem> product,RandomSource rand,int count) {
		List<ProductItem> products=filterProduct(biotope,product);
		if(products.size()==0)
			return null;
		if(products.size()==1) {
			return new ProductWithCount(products.get(0),count);
		}
		return new ProductWithCount(products.get(rand.nextInt(products.size())),count);
		
	}
	public static List<ProductItem> filterProduct(Biotope biotope,Collection<ProductItem> product) {
		List<ProductItem> products=new ArrayList<>(product.size());
		for(ProductItem pi:products) {
			if(pi.biotope()==biotope)
				products.add(pi);
		}
		return products;
	}
	public static ItemStack createProductComb(ProductWithCount product) {
		ItemStack is=Items.PRODUCT_COMB.toStack(product.count());
		is.set(Components.COMB_PRODUCT,product.product().stack());
		return is;
	}
}
