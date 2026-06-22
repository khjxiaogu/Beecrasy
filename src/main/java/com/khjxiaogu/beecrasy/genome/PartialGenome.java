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

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import com.khjxiaogu.beecrasy.genome.Genes.Alleles;
import com.khjxiaogu.beecrasy.genome.gene.Biotope;
import com.khjxiaogu.beecrasy.genome.gene.Humidity;
import com.khjxiaogu.beecrasy.genome.gene.NumericAllele;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;
import com.khjxiaogu.beecrasy.genome.gene.Temperature;
import com.mojang.serialization.Codec;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;

/**
 * 部分基因组记录，仅包含显式设置的基因值（非全部基因）。
 * <p>
 * 适用于配方或配置文件表达部分基因信息，可覆盖应用到 {@link Genome.Builder}。
 *
 * @param alleles 等位基因映射表（仅包含显式设置的基因）
 */
public record PartialGenome(Map<Gene<?>, ?> alleles) implements AllelesHolder{
	/** 部分基因组的 {@link Codec} 编解码器（基于分发表格式）。 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Codec<PartialGenome> CODEC=Codec.dispatchedMap(GeneRegistry.CODEC,a->a.codec())
		.xmap(PartialGenome::new,t->(Map)t.alleles());
	/** 部分基因组的 {@link StreamCodec} 流编解码器。 */
	public static final StreamCodec<RegistryFriendlyByteBuf,PartialGenome> STREAM_CODEC=new StreamCodec<>() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void encode(RegistryFriendlyByteBuf output, PartialGenome value) {
			output.writeVarInt(value.alleles.size());
			for(Entry<Gene<?>, ?> ent:value.alleles.entrySet()) {
				GeneRegistry.STREAM_CODEC.encode(output, ent.getKey());
				((StreamCodec)ent.getKey().streamCodec()).encode(output, ent.getValue());
			}
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public PartialGenome decode(RegistryFriendlyByteBuf input) {
			int size=input.readVarInt();
			Map<Gene, Object> alleles=new IdentityHashMap<>(size);
			if(size>0)
			for(int i=0;i<size;i++) {
				Gene key=GeneRegistry.STREAM_CODEC.decode(input);
				Object value=key.streamCodec().decode(input);
				alleles.put(key, value);
			}
			return new PartialGenome((Map)alleles);
		}
	};
	/**
	 * 部分基因组的流式构建器，提供温度、湿度、生境、产品、产量、寿命等语义化方法。
	 */
	public static class Builder{
		/** 等位基因映射表。 */
		Map<Gene<?>, Object> alleles=new HashMap<>();
		/**
		 * 构造空的构建器。
		 */
		public Builder() {
		}
		/**
		 * 从现有映射表构造构建器。
		 *
		 * @param alleles 现有等位基因映射表
		 */
		public Builder(Map<Gene<?>, ?> alleles) {
			super();
			this.alleles.putAll(alleles);
		}
		/**
		 * 添加基因值。
		 *
		 * @param <T>   等位基因值类型
		 * @param key   基因类型
		 * @param value 等位基因值
		 * @return 当前构建器（用于链式调用）
		 */
		public <T> Builder add(Gene<T> key,T value) {
			this.alleles.put(key, value);
			return this;
		}
		/**
		 * 设置温度等位基因。
		 *
		 * @param value 温度等位基因
		 * @return 当前构建器（用于链式调用）
		 */
		public Builder temperature(Temperature value) {
			return add(Genes.TEMPERATURE, value);
		}
		/**
		 * 设置湿度等位基因。
		 *
		 * @param value 湿度等位基因
		 * @return 当前构建器（用于链式调用）
		 */
		public Builder humidity(Humidity value) {
			return add(Genes.HUMIDITY, value);
		}
		/**
		 * 设置繁殖力等位基因。
		 *
		 * @param value 繁殖力数值等位基因
		 * @return 当前构建器（用于链式调用）
		 */
		public Builder fertility(NumericAllele value) {
			return add(Genes.FERTILITY, value);
		}
		/**
		 * 设置生境等位基因。
		 *
		 * @param value 生境等位基因
		 * @return 当前构建器（用于链式调用）
		 */
		public Builder biotope(Biotope value) {
			return add(Genes.BIOTOPE, value);
		}
		/**
		 * 添加一项产品（使用物品持有者）。
		 *
		 * @param value 物品持有者
		 * @return 当前构建器（用于链式调用）
		 */
		public Builder product(Holder<Item> value) {
			return product(new ItemStackTemplate(value.value()));
		}
		/**
		 * 添加一项产品（使用物品类）。
		 *
		 * @param value 物品类
		 * @return 当前构建器（用于链式调用）
		 */
		public Builder product(ItemLike value) {
			return product(new ItemStackTemplate(value.asItem()));
		}
		/**
		 * 添加一项产品（使用物品模板，生境默认为当前设置的生境或野生）。
		 *
		 * @param value 物品模板
		 * @return 当前构建器（用于链式调用）
		 */
		public Builder product(ItemStackTemplate value) {
			return product((Biotope)alleles.getOrDefault(Genes.BIOTOPE, Alleles.WILD),value);
		}
		/**
		 * 添加一项产品（指定生境和物品模板）。
		 *
		 * @param bio   生境
		 * @param value 物品模板
		 * @return 当前构建器（用于链式调用）
		 */
		public Builder product(Biotope bio,ItemStackTemplate value) {
			return product(new ProductItem(bio,Optional.empty(),value));
		}
		/**
		 * 添加一项产品（使用完整产品物品记录）。
		 *
		 * @param value 产品物品记录
		 * @return 当前构建器（用于链式调用）
		 */
		@SuppressWarnings("unchecked")
		public Builder product(ProductItem value) {
			List<ProductItem> lp=(List<ProductItem>) this.alleles.get(Genes.PRODUCTS);
			if(lp==null) {
				return product(List.of(value));
			}
			return product(Stream.concat(lp.stream(), Stream.of(value)).toList());
		}
		/**
		 * 设置产品列表。
		 *
		 * @param value 产品物品记录列表
		 * @return 当前构建器（用于链式调用）
		 */
		public Builder product(List<ProductItem> value) {
			return add(Genes.PRODUCTS, value);
		}
		/**
		 * 设置产量等位基因。
		 *
		 * @param value 产量数值等位基因
		 * @return 当前构建器（用于链式调用）
		 */
		public Builder yld(NumericAllele value) {
			return add(Genes.YIELD, value);
		}
		/**
		 * 设置寿命等位基因。
		 *
		 * @param value 寿命数值等位基因
		 * @return 当前构建器（用于链式调用）
		 */
		public Builder lifespan(NumericAllele value) {
			return add(Genes.LIFESPAN, value);
		}
		/**
		 * 构建不可变的 {@link PartialGenome} 实例。
		 *
		 * @return 部分基因组实例
		 */
		public PartialGenome build() {
			return new PartialGenome(Map.copyOf(alleles));
		}
		@SuppressWarnings("unchecked")
		public <T> T get(Gene<T> type) {
			return (T) alleles.get(type);
		}
		
	}
	/**
	 * 获取指定基因类型的等位基因值（可能为 {@code null}）。
	 *
	 * @param <T>  等位基因值类型
	 * @param type 基因类型
	 * @return 等位基因值，未设置时返回 {@code null}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAllele(Gene<T> type) {
		return (T) alleles.get(type);
	}
	/**
	 * 将部分基因组的等位基因值覆盖应用到 {@link Genome.Builder}。
	 *
	 * @param builder 基因组建构器
	 * @return 覆盖后的基因组建构器
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Genome.Builder apply(Genome.Builder builder) {
		for(Entry<Gene<?>, ?> ent:alleles.entrySet()) {
			builder.add((Gene)ent.getKey(), ent.getValue());
		}
		return builder;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Genome createGenome() {
		Genome.Builder builder=new Genome.Builder();
		for(Entry<Gene<?>, ?> ent:alleles.entrySet()) {
			builder.add((Gene)ent.getKey(), ent.getValue());
		}
		return builder.build();
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public PartialGenome.Builder apply(PartialGenome.Builder builder) {
		for(Entry<Gene<?>, ?> ent:alleles.entrySet()) {
			builder.add((Gene)ent.getKey(), ent.getValue());
		}
		return builder;
	}
	public PartialGenome.Builder createBuilder() {
		return new PartialGenome.Builder(alleles);
	}
}
