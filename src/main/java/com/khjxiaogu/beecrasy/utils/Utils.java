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

package com.khjxiaogu.beecrasy.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Attachments;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DataResult.Error;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess.ImmutableRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.RegistryOps.HolderLookupAdapter;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/**
 * 通用工具类，包含配方输出获取、同步随机数、任务拆分、文本组件构建、序列化编解码器等辅助方法。
 */
public final class Utils {
	/**
	 * 从配方和物品列表获取预期产出物品模板。
	 * <p>
	 * 针对原版的有序/无序配方直接返回预设的 {ItemStackTemplate}，
	 * 对其他类型配方则创建临时 {@link CraftingInput} 进行合成。
	 *
	 * @param stacks 输入物品列表
	 * @param recipe 合成配方
	 * @return 产出物品模板，无法获取时返回 {@code null}
	 */
	public static ItemStackTemplate getRecipeOutput(List<ItemStack> stacks,CraftingRecipe recipe) {
		return switch(recipe) {
		case ShapedRecipe sr->sr.result;
		case ShapelessRecipe sr->sr.result;
		default->{
			CraftingInput ipt=CraftingInput.of(3, 3, stacks);
			ItemStack is=recipe.assemble(ipt);
			if(is!=null&&!is.isEmpty()) {
				yield new ItemStackTemplate(is.getItem(),is.getCount(),is.getComponentsPatch());
			}
			yield null;
		}
		};
	}
	/**
	 * 获取与玩家数据同步的随机数生成器。
	 * <p>
	 * 从玩家的附件数据中读取当前种子，创建随机源后更新种子并写回附件。
	 *
	 * @param p 玩家
	 * @return 与玩家同步的随机数生成器
	 */
	public static RandomSource getSyncedRandom(Player p) {
		@Nullable Long comp=p.getData(Attachments.RANDOM_SEED);
		RandomSource rnd=RandomSource.create(comp);
		p.setData(Attachments.RANDOM_SEED.get(), rnd.nextLong());
		return rnd;
	}
	/**
	 * 将任务列表均匀拆分为若干个子集合。
	 * <p>
	 * 拆分规则：
	 * <ul>
	 *     <li>每个子集合的元素个数应尽可能接近 {@code effort}（目标大小），且不小于 {@code effort}（若总任务数不足 {@code effort}，
	 *     则返回包含整个列表的单个子集合）。</li>
	 *     <li>子集合之间元素数量尽可能相等（任意两个子集合大小之差不超过1）。</li>
	 *     <li>拆分的子集合数量不会超过系统可用的处理器核心数（{@link Runtime#availableProcessors()}），
	 *     即 {@code k = min(round(n / effort), 可用处理器数)}。</li>
	 * </ul>
	 *
	 * @param tasks  原始任务列表（不能为 null）
	 * @param effort 期望的每个子集合的目标大小（正数，例如 400.0f）。算法会尽量使每个子集合大小接近该值，
	 *               但实际大小可能略高或略低（受总数和处理器数限制）。
	 * @param <T>    任务类型
	 * @return 拆分后的子集合列表。若原始列表为 null 或空，返回空列表；
	 *         若列表长度小于 {@code effort}，返回包含整个列表的单个子集合。
	 */
    public static <T> List<List<T>> splitTasks(List<T> tasks,float effort) {
        if (tasks == null || tasks.isEmpty()) {
            return new ArrayList<>();
        }

        int n = tasks.size();
        if (n < effort) {
            List<List<T>> result = new ArrayList<>();
            result.add(new ArrayList<>(tasks));
            return result;
        }
        int k = Math.round(n / effort);
        k=Math.min(k, Runtime.getRuntime().availableProcessors());
        int base = n / k;
        int remainder = n % k; 

        List<List<T>> result = new ArrayList<>(k);
        int start = 0;
        for (int i = 0; i < k; i++) {
            int size = base + (i < remainder ? 1 : 0);
            int end = start + size;
            List<T> subList = new ArrayList<>(tasks.subList(start, end));
            result.add(subList);
            start = end;
        }
        return result;
    }
	/**
	 * 创建带参数的可翻译文本组件。
	 *
	 * @param format  翻译键
	 * @param objects 格式化参数
	 * @return 可变文本组件
	 */
	public static MutableComponent translate(String format, Object... objects) {
		return translateWithFallback(format, null, objects);
	}

	/**
	 * 创建无可变参数的可翻译文本组件。
	 *
	 * @param format 翻译键
	 * @return 可变文本组件
	 */
	public static MutableComponent translate(String format) {
		return translate(format, new Object[0]);
	}

	/**
	 * 创建带参数和回退文本的可翻译文本组件。
	 *
	 * @param format   翻译键
	 * @param fallback 回退文本（翻译键不存在时显示）
	 * @param objects  格式化参数
	 * @return 可变文本组件
	 */
	public static MutableComponent translateWithFallback(String format, String fallback, Object... objects) {
		return MutableComponent.create(new TranslatableContents(format, fallback, objects));
	}

	/**
	 * 创建无参数但有回退文本的可翻译文本组件。
	 *
	 * @param format   翻译键
	 * @param fallback 回退文本
	 * @return 可变文本组件
	 */
	public static MutableComponent translateWithFallback(String format, String fallback) {
		return translate(format, fallback, new Object[0]);
	}

	/**
	 * 创建纯文本内容组件。
	 *
	 * @param content 文本内容
	 * @return 可变文本组件
	 */
	public static MutableComponent string(String content) {
		return MutableComponent.create(PlainTextContents.create(content));
	}
	/**
	 * 创建根据键动态分发值编解码器的Map流编解码器。
	 * <p>
	 * 对Map中的每个条目，使用键对应的专用编解码器对值进行序列化/反序列化。
	 *
	 * @param keyCodec 键的流编解码器
	 * @param toCodec  根据键获取对应的值编解码器的函数
	 * @param <K>      键类型
	 * @param <V>      值类型
	 * @param <B>      ByteBuf子类型
	 * @return 流编解码器
	 */
	public static <K,V,B extends ByteBuf> StreamCodec<B,Map<K,V>> streamDispatchedMap(StreamCodec<? super B,K> keyCodec,Function<K,StreamCodec<? super B,V>> toCodec) {
		return new StreamCodec<>() {
			@Override
			public void encode(B output, Map<K,V> value) {
				ByteBufCodecs.VAR_INT.encode(output, value.size());
				for(Entry<K, V> ent:value.entrySet()) {
					keyCodec.encode(output, ent.getKey());
					toCodec.apply(ent.getKey()).encode(output, ent.getValue());
				}
			}
			@Override
			public Map<K,V> decode(B input) {
				int size=ByteBufCodecs.VAR_INT.decode(input);
				Map<K, V> values=new HashMap<>(size);
				if(size>0)
				for(int i=0;i<size;i++) {
					K key=keyCodec.decode(input);
					V value=toCodec.apply(key).decode(input);
					values.put(key, value);
				}
				return values;
			}
		};
	}
	/**
	 * 将List流编解码器包装为数组流编解码器。
	 *
	 * @param codec   List的流编解码器
	 * @param asArray 数组构造工厂
	 * @param <T>     元素类型
	 * @param <B>     ByteBuf子类型
	 * @return 数组流编解码器
	 */
	public static <T,B extends ByteBuf> StreamCodec<B,T[]> asArray(StreamCodec<? super B,List<T>> codec,IntFunction<T[]> asArray) {
		return codec.<T[]>map(o->o.toArray(asArray),Arrays::asList).cast();
	}
	
	/**
	 * 基于 {@link ValueIOSerializable} 创建 {@link Codec}。
	 * <p>
	 * 利用 ValueIO 框架将可序列化对象转换为 NBT 格式的 Codec。
	 *
	 * @param <T>     实现了 {@link ValueIOSerializable} 的类型
	 * @param factory 空实例的供应者，用于反序列化时创建新对象
	 * @return 对应的 Codec
	 */
	public static <T extends ValueIOSerializable> Codec<T> createCodec(Supplier<T> factory){
		return Codec.of(new Encoder<>() {
			@Override
			public <S> DataResult<S> encode(T input, DynamicOps<S> ops, S prefix) {
				ProblemReporter.Collector reporter = new ProblemReporter.Collector();
				TagValueOutput tvo = TagValueOutput.createWithoutContext(reporter);
				input.serialize(tvo);
				if(!reporter.isEmpty())
					return DataResult.error(reporter::getReport, tvo.buildResult()).flatMap(e->ExtraCodecs.NBT.encode(e, ops, prefix));
				else
					return ExtraCodecs.NBT.encode(tvo.buildResult(), ops, prefix);
				
			}
			
		},new Decoder<>() {

			@Override
			public <S> DataResult<Pair<T, S>> decode(DynamicOps<S> ops, S input) {
				DataResult<Pair<CompoundTag, S>> dr=ExtraCodecs.NBT.decode(ops, input).flatMap(o->{
				if(o.getFirst() instanceof CompoundTag ct)
					return DataResult.success(Pair.of(ct, o.getSecond()));
				else
					return DataResult.error(()->"Not a tag compound.");
				});
				if(dr.isError()) {
					Error<?> err=dr.error().get();
					return DataResult.error(err.messageSupplier(), err.lifecycle());
				}
				ProblemReporter.Collector reporter = new ProblemReporter.Collector();
				ValueInput tvo=TagValueInput.create(reporter, provider(ops),dr.getOrThrow().getFirst());
				T value=factory.get();
				value.deserialize(tvo);
				if(!reporter.isEmpty())
					return DataResult.error(reporter::getReport, Pair.of(value, input));
				return DataResult.success(Pair.of(value, input));
			}
			
		}, "valueIO");
		
	}
	/**
	 * 从 {@link DynamicOps} 中提取 {@link HolderLookup.Provider}。
	 * <p>
	 * 如果 ops 是 {@link RegistryOps} 且包含 {@link HolderLookupAdapter}，
	 * 则从中提取 lookupProvider；否则返回一个空的 RegistryAccess。
	 *
	 * @param ops 动态操作上下文
	 * @return HolderLookup.Provider
	 */
	public static HolderLookup.Provider provider(DynamicOps<?> ops) {
		if(ops instanceof RegistryOps<?> ros) {
			if(ros.lookupProvider instanceof HolderLookupAdapter lookup) {
				return lookup.lookupProvider;
			}
			return new ImmutableRegistryAccess(Map.of()){
				@Override
				public <V> RegistryOps<V> createSerializationContext(DynamicOps<V> parent) {
					return ros.withParent(parent);
				}
			};
		}
		return new ImmutableRegistryAccess(Map.of()){
			@Override
			public <V> RegistryOps<V> createSerializationContext(DynamicOps<V> parent) {
				return RegistryOps.create(parent, EMPTY);
			}
		};
	}
}
