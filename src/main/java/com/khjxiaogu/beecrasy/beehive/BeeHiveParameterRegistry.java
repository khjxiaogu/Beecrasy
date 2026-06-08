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

package com.khjxiaogu.beecrasy.beehive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.khjxiaogu.beecrasy.utils.Utils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

/**
 * 蜂巢参数类型注册表。
 * 全局管理所有 {@link BeehiveParameterType} 的注册、序列化（Codec / StreamCodec）和 ID 查找。
 * 维护了从标识符到参数类型的映射，以及用于网络传输的整数 ID 索引。
 */
public class BeeHiveParameterRegistry {
	/**
	 * 蜂巢参数类型记录。
	 * 泛型化的参数类型描述，包含编解码器、合并策略、默认值和描述生成器。
	 *
	 * @param <T>              参数值的类型（如 Float）
	 * @param id               参数唯一标识符
	 * @param codec            该参数值的编解码器
	 * @param merge            用于合并多个参数值的二元操作符（如加法或乘法）
	 * @param defaultValueSupplier 默认值提供器
	 * @param desc             用于将参数值格式化为文本组件的消费者（接收参数值和文本消费者）
	 */
	public static record BeehiveParameterType<T>(Identifier id, Codec<T> codec,BinaryOperator<T> merge,Supplier<T> defaultValueSupplier,BiConsumer<T,Consumer<Component>> desc){
		/**
		 * 获取该参数类型的默认值。
		 * @return 默认值
		 */
		public T getDefault() {
			return defaultValueSupplier.get();
		}
		/**
		 * 将一个参数值合并到给定的参数映射中。
		 * 使用当前类型的合并策略与映射中的已有值合并。
		 * @param params 目标参数映射
		 * @param value  要合并的值
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void mergeTo(Map<BeehiveParameterType<?>,Object> params,Object value) {
			params.merge((BeehiveParameterType)this, value, (BinaryOperator)merge());
		}
		/**
		 * 将给定值与默认值合并，返回合并结果。
		 * @param value 要合并的源值
		 * @return 合并后的值
		 */
		public T mergeToDefault(T value) {
			return merge.apply(value, getDefault());
		}
	}
	/** 标识符 → 参数类型的映射 */
	private static Map<Identifier,BeehiveParameterType<?>> idMap=new HashMap<>();
	/** 按排序顺序存储的参数类型标识符列表，用于生成整数 ID */
	private static List<Identifier> typeIdMap=new ArrayList<>();
	/** 参数类型 → 整数 ID 的反向映射 */
	private static Reference2IntOpenHashMap<BeehiveParameterType<?>> typeId=new Reference2IntOpenHashMap<>();
	/** 索引是否已排序的标志 */
	private static volatile boolean sorted=false;
	/** 索引构建的锁对象 */
	private static Object lock=new Object();
	/** 参数类型的字符串编解码器（标识符 ↔ 参数类型）。 */
	public static final Codec<BeehiveParameterType<?>> CODEC=Identifier.CODEC
			.comapFlatMap(BeeHiveParameterRegistry::getType, BeehiveParameterType::id);
	/** 参数映射的复合编解码器（按类型分发到各自的编解码器）。 */
	public static final Codec<Map<BeehiveParameterType<?>, Object>> COMPOSITE_CODEC=Codec.dispatchedMap(CODEC, BeehiveParameterType::codec);
	/** 参数类型的网络流式编解码器（基于整数 ID）。 */
	public static final StreamCodec<ByteBuf,BeehiveParameterType<?>> STREAM_CODEC=ByteBufCodecs.
			idMapper(BeeHiveParameterRegistry::getByInt, BeeHiveParameterRegistry::getIntId);
	/** 参数映射的网络流式编解码器。 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final StreamCodec<ByteBuf,Map<BeehiveParameterType<?>, Object>> COMPOSITE_STREAM_CODEC=Utils.streamDispatchedMap(BeeHiveParameterRegistry.STREAM_CODEC,Util.memoize(t->ByteBufCodecs.fromCodec((Codec)t.codec())));
	
	/**
	 * 注册一个数值型（Float）参数类型，使用加法作为合并策略。
	 * @param id   参数标识符
	 * @param def  默认值
	 * @param desc 描述生成器
	 * @return 注册完成的参数类型
	 */
	public static BeehiveParameterType<Float> registerNumeric(Identifier id,float def,BiConsumer<Float,Consumer<Component>> desc) {
		return register(id,Codec.FLOAT,(a,b)->a+b,()->def,desc);
	}
	/**
	 * 注册一个倍数型（Float）参数类型，使用乘法作为合并策略。
	 * @param id   参数标识符
	 * @param def  默认值
	 * @param desc 描述生成器
	 * @return 注册完成的参数类型
	 */
	public static BeehiveParameterType<Float> registerMultiplier(Identifier id,float def,BiConsumer<Float,Consumer<Component>> desc) {
		return register(id,Codec.FLOAT,(a,b)->a*b,()->def,desc);
	}
	/**
	 * 注册一个自定义参数类型。
	 * 如果标识符已存在，会覆盖之前的注册；同时将其加入整数 ID 索引（下次构建索引时生效）。
	 * @param <T>    参数值的类型
	 * @param id     参数唯一标识符
	 * @param codec  参数值的编解码器
	 * @param merge  合并策略（二元操作符）
	 * @param defaultValueSupplier 默认值提供器
	 * @param desc   描述生成器
	 * @return 注册完成的参数类型
	 */
	public synchronized static <T> BeehiveParameterType<T> register(Identifier id, Codec<T> codec,BinaryOperator<T> merge,Supplier<T> defaultValueSupplier,BiConsumer<T,Consumer<Component>> desc) {
		BeehiveParameterType<T> gt=new BeehiveParameterType<>(id,codec,merge,defaultValueSupplier,desc);
		if(!idMap.containsKey(id)) {
			synchronized(lock) {
				typeIdMap.add(id);
				sorted=false;
			}
		}
		idMap.put(id, gt);
		return gt;
	}

	/**
	 * 构建或刷新整数 ID 索引。
	 * 将 typeIdMap 按标识符的命名空间排序，并为每个参数类型分配一个稳定的整数 ID。
	 * 使用双重检查锁定确保线程安全。
	 */
	private static void makeIndex() {
		if(!sorted) {
			synchronized(lock) {
				if(!sorted) {
					typeIdMap.sort(Identifier::compareNamespaced);
					typeId.clear();
					for(int i=0;i<typeIdMap.size();i++) {
						typeId.put(idMap.get(typeIdMap.get(i)), i);
					}
					sorted=true;
				}
			}
		}
	}
	/**
	 * 根据整数 ID 获取参数类型。
	 * @param num 整数 ID（由排序后的索引决定）
	 * @return 对应的参数类型
	 */
	public static BeehiveParameterType<?> getByInt(int num){
		makeIndex();
		BeehiveParameterType<?> type=idMap.get(typeIdMap.get(num));
		return type;
	}
	/**
	 * 获取参数类型对应的整数 ID。
	 * @param type 参数类型
	 * @return 整数 ID，如果未找到则返回 -1
	 */
	public static int getIntId(BeehiveParameterType<?> type){
		makeIndex();
		return typeId.getOrDefault(type,-1);
	}
	/**
	 * 根据标识符获取参数类型。
	 * @param id 参数标识符
	 * @return 对应的参数类型，如果未注册则返回 null
	 */
	public static BeehiveParameterType<?> get(Identifier id){
		BeehiveParameterType<?> type=idMap.get(id);
		return type;
	}
	/**
	 * 根据标识符获取参数类型（带错误处理的编解码器辅助方法）。
	 * @param id 参数标识符
	 * @return 如果找到则返回 {@link DataResult#success}，否则返回包含错误信息的 {@link DataResult#error}
	 */
	private static DataResult<BeehiveParameterType<?>> getType(Identifier id){
		BeehiveParameterType<?> type=idMap.get(id);
		if(type==null)
			return DataResult.error(()->"BeehiveParam type '"+id+"' not present!");
		return DataResult.success(type);
	}
}
