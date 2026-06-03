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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Tags;
import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterSet;
import com.khjxiaogu.beecrasy.events.BeeEnvironmentValidateEvent;
import com.khjxiaogu.beecrasy.genome.gene.Biotope;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;
import com.khjxiaogu.beecrasy.utils.BeecrasyMath;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.NeoForge;

public class GenomeWorkHelper {
	public static <T extends AllelesHolder> long checkSimilarityPoint(T[] template,T[] subject) {
		return (GenomeWorkHelper.checkSimilarityPoint(template[0],subject[0])<<32)+GenomeWorkHelper.checkSimilarityPoint(template[1],subject[1]);
	}
	public static long checkSimilarityPoint(AllelesHolder template,AllelesHolder subject) {
		int point=0;
		List<ProductItem> tproduct=template.getAllele(Genes.PRODUCTS);
		List<ProductItem> sproduct=subject.getAllele(Genes.PRODUCTS);
		if(Objects.equals(tproduct, sproduct))
			point+=100000;
		else {
			point+=10000*BeecrasyMath.countCommonElements(tproduct, sproduct);
		}
		if(template.getAllele(Genes.BIOTOPE)==subject.getAllele(Genes.BIOTOPE))
			point+=1000;
		if(template.getAllele(Genes.TEMPERATURE)==subject.getAllele(Genes.TEMPERATURE))
			point+=100;
		if(template.getAllele(Genes.HUMIDITY)==subject.getAllele(Genes.HUMIDITY))
			point+=100;
		for(Gene<?> id:GeneRegistry.getGeneTypesUnordered()) {
			if(id==Genes.PRODUCTS)
				continue;
			if(id==Genes.BIOTOPE)
				continue;
			if(id==Genes.TEMPERATURE)
				continue;
			if(id==Genes.HUMIDITY)
				continue;
			if(template.getAllele(id)==subject.getAllele(id))
				point++;
		}
		return point;
	}
	public static Set<Biotope> findBiotope(Level l,BlockPos pos,int radius){
		Set<Biotope> bts=new HashSet<>();
		boolean hasFlower=false;
		final int x0=pos.getX()-radius,x1=pos.getX()+radius;
		final int y0=pos.getY()-radius,y1=pos.getY()+radius;
		final int z0=pos.getZ()-radius,z1=pos.getZ()+radius;
		BlockPos.MutableBlockPos mutable=new MutableBlockPos();
		for(int x=x0;x<=x1;x++) {
			for(int z=z0;z<=z1;z++) {
				mutable.set(x,y0, z);
				ChunkPos cp=ChunkPos.containing(mutable);
				LevelChunk chunk=l.getChunk(cp.x(), cp.z());
				for(int y=y0;y<=y1;y++) {
	
					mutable.set(x,y, z);
					BlockState bs=chunk.getBlockState(mutable);
					if(bs.is(Tags.FLOWERS)) {
						hasFlower=true;
					}
					for(Biotope bt:Genes.Alleles.BIOTOPE) {
						if(bs.is(bt.getTag())) {
							bts.add(bt);
						}
					}
				}
			}
		}
		return (bts.isEmpty()&&!hasFlower)?null:bts;
	}
	public static record ProductWithCount(ProductItem product,int count) {
		public ItemStack createProductComb() {
			ItemStack is=Items.PRODUCT_COMB.toStack(count());
			if(product()!=null) {
				
				ItemStackTemplate stack=product().stack();
				is.set(Components.COMB_PRODUCT,stack);
				is.set(Components.TINT_STACK,stack);
			}
			return is;
		}
	}
	public static List<ProductWithCount> pickProduct(Biotope biotope,Collection<ProductItem> product,RandomSource rand,int count) {
		List<ProductItem> products=filterProduct(biotope,product);
		if(products.size()==0)
			return List.of(new ProductWithCount(null,count));
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
			return new ProductWithCount(null,count);
		if(products.size()==1) {
			return new ProductWithCount(products.get(0),count);
		}
		return new ProductWithCount(products.get(rand.nextInt(products.size())),count);
		
	}
	public static List<ProductItem> filterProduct(Biotope biotope,Collection<ProductItem> product) {
		List<ProductItem> products=new ArrayList<>(product.size());
		for(ProductItem pi:product) {
			if(pi.biotope()==biotope)
				products.add(pi);
		}
		return products;
	}
	public static boolean isValidEnvironment(BeeHiveParameterSet params,AllelesHolder phenoType) {
		BeeEnvironmentValidateEvent ev=new BeeEnvironmentValidateEvent(params,phenoType);
		return !NeoForge.EVENT_BUS.post(ev).isCanceled();
	}
}
