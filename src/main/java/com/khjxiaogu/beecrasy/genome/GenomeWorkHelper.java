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
import java.util.Optional;
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
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.NeoForge;

/**
 * 基因组工作辅助工具类，包含相似度计算、生境检测、产品选择、环境验证等核心游戏逻辑。
 */
public class GenomeWorkHelper {
	/**
	 * 计算两个二倍体基因组的整体相似度评分（64位，前32位为母方评分，后32位为父方评分）。
	 *
	 * @param <T>     等位基因持有者类型
	 * @param template 模板基因组数组
	 * @param subject  待比较基因组数组
	 * @return 64位综合相似度评分
	 */
	public static <T extends AllelesHolder> long checkSimilarityPoint(T[] template,T[] subject) {
		return (GenomeWorkHelper.checkSimilarityPoint(template[0],subject[0])<<32)+GenomeWorkHelper.checkSimilarityPoint(template[1],subject[1]);
	}
	/**
	 * 计算两个基因组的相似度评分。
	 * <p>
	 * 评分规则：产品完全匹配得100000分，共同元素每个得10000分；
	 * 生境匹配得1000分；温度/湿度匹配各得100分；其余基因匹配各得1分。
	 *
	 * @param template 模板基因组
	 * @param subject  待比较基因组
	 * @return 相似度评分
	 */
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
	/**
	 * 在指定位置周围指定半径内扫描生境。
	 *
	 * @param l      世界实例
	 * @param pos    中心位置
	 * @param radius 扫描半径
	 * @return 检测到的生境集合；如果没有任何鲜花或生境方块则返回 {@code null}
	 */
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
	/**
	 * 产品及数量记录。
	 *
	 * @param product 产品物品记录
	 * @param count   数量
	 */
	public static record ProductWithCount(ProductItem product,int count) {
		/**
		 * 创建产品蜜脾物品栈。
		 *
		 * @return 产品蜜脾物品栈
		 */
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
	/**
	 * 按生境过滤并随机分配产品数量。
	 *
	 * @param biotope 当前生境
	 * @param product 所有产品列表
	 * @param rand    随机数生成器
	 * @param count   要分配的总产品数量
	 * @return 分配后的产品列表
	 */
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
	/**
	 * 按生境过滤并随机选取单个产品（所有数量分配给一个产品）。
	 *
	 * @param biotope 当前生境
	 * @param product 所有产品列表
	 * @param rand    随机数生成器
	 * @param count   数量
	 * @return 随机选取的产品及数量
	 */
	public static ProductWithCount pickSingleProduct(Biotope biotope,Collection<ProductItem> product,RandomSource rand,int count) {
		List<ProductItem> products=filterProduct(biotope,product);
		if(products.size()==0)
			return new ProductWithCount(null,count);
		if(products.size()==1) {
			return new ProductWithCount(products.get(0),count);
		}
		return new ProductWithCount(products.get(rand.nextInt(products.size())),count);
		
	}
	/**
	 * 按生境过滤产品列表，仅返回匹配当前生境的产品。
	 *
	 * @param biotope 当前生境
	 * @param product 所有产品列表
	 * @return 过滤后的产品列表（仅包含匹配生境的产品）
	 */
	public static List<ProductItem> filterProduct(Biotope biotope,Collection<ProductItem> product) {
		List<ProductItem> products=new ArrayList<>(product.size());
		for(ProductItem pi:product) {
			if(pi.biotope()==biotope)
				products.add(pi);
		}
		return products;
	}
	/**
	 * 发送环境验证事件，判断蜂箱参数的当前环境是否有效。
	 *
	 * @param params    蜂箱参数集合
	 * @param phenoType 表现型基因组
	 * @return 如果环境有效则返回 {@code true}
	 */
	public static boolean isValidEnvironment(BeeHiveParameterSet params,AllelesHolder phenoType) {
		BeeEnvironmentValidateEvent ev=new BeeEnvironmentValidateEvent(params,phenoType);
		return !NeoForge.EVENT_BUS.post(ev).isCanceled();
	}
	public static boolean transformFlowers(Level l,BlockPos pos,int radius,float count){
		boolean updated=false;
		final int x0=pos.getX()-radius;
		final int y0=pos.getY()-radius,y1=pos.getY()+radius;
		final int z0=pos.getZ()-radius;
		BlockPos.MutableBlockPos mutable=new MutableBlockPos();
		RandomSource rs=l.getRandom();
		int maxcnt=BeecrasyMath.getRandomRate(count, rs);
		for(int i=0;i<maxcnt;i++) {
			Optional<Holder<Block>> opt=l.registryAccess().get(Tags.FLOWERS_FROM_APICULTURE).flatMap(t->t.getRandomElement(rs));
			if(opt.isEmpty())
				continue;
			mutable=mutable.set(rs.nextInt(radius*2+1)+x0,y0, rs.nextInt(radius*2+1)+z0);
			
			ChunkPos cp=ChunkPos.containing(mutable);
			LevelChunk chunk=l.getChunk(cp.x(), cp.z());
			for(int y=y0;y<=y1;y++) {
				mutable=mutable.setY(y);
				BlockState bs=chunk.getBlockState(mutable);
				if(bs.is(Tags.TO_BE_FLOWER)&&bs.canBeReplaced()) {
					mutable=mutable.setY(y-1);
					BlockState bsbelow=chunk.getBlockState(mutable);
					BlockState toPlace=opt.get().value().defaultBlockState();
					if(bsbelow.canBeReplaced()) {
						BlockPos placing=mutable.immutable();
						l.setBlock(placing, toPlace, 18);
						opt.get().value().setPlacedBy(l, placing, toPlace, null, new ItemStack(opt.get().value().asItem()));
						updated=true;
					}else{
						mutable=mutable.setY(y);
						System.out.println(mutable);
						BlockPos placing=mutable.immutable();
						BlockState bsabove=chunk.getBlockState(placing.above());
						if(bsabove.canBeReplaced()) {
							l.setBlock(placing, toPlace, 18);
							opt.get().value().setPlacedBy(l, placing, toPlace, null, new ItemStack(opt.get().value().asItem()));
							updated=true;
						}
						
					}
				}
			}
			
		}
		return updated;
	}
}
