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

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.khjxiaogu.beecrasy.utils.Utils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/**
 * 基因组类，实现 {@link AllelesHolder} 接口，存储所有基因类型的等位基因值。
 * <p>
 * 支持 Codec 序列化与 Builder 构建模式。
 */
public class Genome implements AllelesHolder {
	/** 基因组的 {@link Codec} 编解码器（基于分发表格式）。 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Codec<Genome> CODEC=RecordCodecBuilder.create(t->t
			.group(Codec.dispatchedMap(GeneRegistry.CODEC,a->a.codec())
				.fieldOf("alleles")
				.forGetter(o->(Map)o.alleles))
			.apply(t,Genome::new));
	/** 基因组列表的 {@link Codec} 编解码器。 */
	public static final Codec<List<Genome>> LIST_CODEC=CODEC.listOf();
	/** 基因组的 {@link StreamCodec} 流编解码器。 */
	public static final StreamCodec<RegistryFriendlyByteBuf,Genome> STREAM_CODEC=
			Utils.streamDispatchedMap(GeneRegistry.STREAM_CODEC, Gene::streamCodec).map(Genome::new, Genome::alleles);
	/** 空的默认基因组（所有基因使用默认值）。 */
	public static final Genome DEFAULT=new Genome(Map.of());
	/** 等位基因映射表。 */
	private Map<Gene<?>, ?> alleles;
	/**
	 * 构造基因组。
	 *
	 * @param alleles 等位基因映射表
	 */
	Genome(Map<Gene<?>, ?> alleles) {
		super();
		this.alleles = alleles;
	}
	/**
	 * 获取内部的等位基因映射表（包内可见）。
	 *
	 * @return 等位基因映射表
	 */
	@SuppressWarnings("rawtypes")
	Map alleles(){
		return alleles;
	}
	/**
	 * 获取指定基因类型的等位基因值，缺失时返回该基因的默认值。
	 *
	 * @param <T>  等位基因值类型
	 * @param type 基因类型
	 * @return 等位基因值
	 */
	public <T> T getAllele(Gene<T> type) {
		@SuppressWarnings("unchecked")
		T alle= (T) alleles.get(type);
		if(alle==null)
			return type.getDefault();
		return alle;
	}
	@Override
	public int hashCode() {
		return Objects.hash(alleles);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Genome other = (Genome) obj;
		return Objects.equals(alleles, other.alleles);
	}
	/**
	 * 从此基因组建模创建构建器（复制现有值）。
	 *
	 * @return 基因组建构器
	 */
	public Builder createBuilder() {
		return new Builder(this);
	}
	/**
	 * 创建一个空的基因组建构器。
	 *
	 * @return 基因组建构器
	 */
	public static Builder builder() {
		return new Builder();
	}
	/**
	 * 基因组建构器，支持链式调用添加基因并构建不可变 {@link Genome}。
	 */
	public static class Builder implements AllelesHolder {
		/** 等位基因映射表（基于引用恒等）。 */
		private Map<Gene<?>, Object> alleles=new IdentityHashMap<>();
		/**
		 * 构造空的基因组建构器。
		 */
		public Builder() {
		}
		/**
		 * 从现有基因组复制值构建构造器。
		 *
		 * @param genome 现有基因组
		 */
		public Builder(Genome genome) {
			for(Entry<Gene<?>, ?> ent:genome.alleles.entrySet()) {
				alleles.put(ent.getKey(), ent.getValue());
			}
		}
		/**
		 * 从现有构建器复制值构建新的构造器。
		 *
		 * @param genome 现有构建器
		 */
		public Builder(Builder genome) {
			for(Entry<Gene<?>, ?> ent:genome.alleles.entrySet()) {
				alleles.put(ent.getKey(), ent.getValue());
			}
		}
		/**
		 * 返回当前构造器的副本。
		 *
		 * @return 基因组建构器副本
		 */
		public Builder copy() {
			return new Builder(this);
		}
		/**
		 * 设置指定基因类型的等位基因值。
		 *
		 * @param <T>  等位基因值类型
		 * @param type 基因类型
		 * @param gene 等位基因值
		 * @return 当前构建器（用于链式调用）
		 */
		public <T> Builder add(Gene<T> type,T gene) {
			alleles.put(type, gene);
			return this;
		}
		/**
		 * 获取指定基因类型的等位基因值（可能为 {@code null}）。
		 *
		 * @param <T>  等位基因值类型
		 * @param type 基因类型
		 * @return 等位基因值，未设置时返回 {@code null}
		 */
		@SuppressWarnings("unchecked")
		public <T> T get(Gene<T> type) {
			return (T) alleles.get(type);
		}
		/**
		 * 获取指定基因类型的等位基因值，缺失时返回该基因的默认值。
		 *
		 * @param <T>  等位基因值类型
		 * @param type 基因类型
		 * @return 等位基因值
		 */
		public <T> T getAllele(Gene<T> type) {
			@SuppressWarnings("unchecked")
			T alle= (T) alleles.get(type);
			if(alle==null)
				return type.getDefault();
			return alle;
		}
		/**
		 * 构建不可变的 {@link Genome} 实例。
		 * <p>
		 * 遍历所有已注册的基因类型，将当前设置的等位基因值（或默认值）填充到新映射表中。
		 *
		 * @return 不可变的基因组实例
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Genome build() {
			Map<Gene, Object> calleles=new IdentityHashMap<>();
			for(Identifier id:GeneRegistry.getGeneTypes()) {
				Gene gt=GeneRegistry.get(id);
				Object o=alleles.get(gt);
				if(o!=null) {
					calleles.put(gt, o);
				}else {
					calleles.put(gt, gt.getDefault());
				}
			}
			return new Genome((Map)calleles);
			
		}
		
	}
}
