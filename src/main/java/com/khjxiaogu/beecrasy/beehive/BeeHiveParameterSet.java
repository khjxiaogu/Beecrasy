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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.khjxiaogu.beecrasy.beehive.BeeHiveHandler.HiveSlotIterable;
import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterRegistry.BeehiveParameterType;
import com.khjxiaogu.beecrasy.genome.gene.Biotope;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/**
 * 蜂巢参数集记录。
 * 聚合了蜂巢工作所需的所有环境与参数信息，包括所在世界、位置、生物群系、维度、
 * 方块状态、禁用的突变列表、参数映射和活跃的生境集合。
 *
 * @param level           服务端世界实例
 * @param position        蜂巢所在的方块坐标
 * @param biome           当前生物群系
 * @param type            当前维度类型
 * @param state           蜂巢所在位置的方块状态
 * @param disabledMutation 禁用的突变 ID 集合
 * @param params          工作参数值映射（参数类型 → 值）
 * @param activeBiotopes  当前活跃的生境集合
 */
public record BeeHiveParameterSet(ServerLevel level,BlockPos position,BeehiveSlotProvider slots,Holder<Biome> biome,Holder<DimensionType> type,@Nullable BlockState state,Set<Identifier> disabledMutation,Map<BeehiveParameterType<?>,Object> params,Set<Biotope> activeBiotopes,boolean hasFlower) {
	public static interface BeehiveSlotProvider{
		/** 巢脾槽位列表（存放幼虫和产物）。 */
		Iterable<? extends HiveSlot> combSlot();
		/** 雄蜂槽位列表。 */
		Iterable<? extends HiveSlot> droneSlot();
		/** 蜂后槽位列表（存放蜂后/王台）。 */
		Iterable<? extends HiveSlot> queenSlot();
		public static record BasicBeehiveSlotProvider(Iterable<? extends HiveSlot> combSlot,
				Iterable<? extends HiveSlot> droneSlot,
				Iterable<? extends HiveSlot> queenSlot) implements BeehiveSlotProvider{
			
		}
		public static BeehiveSlotProvider createBasic(Iterable<? extends HiveSlot> combSlot,
				Iterable<? extends HiveSlot> droneSlot,
				Iterable<? extends HiveSlot> queenSlot) {
			return new BasicBeehiveSlotProvider(combSlot,droneSlot,queenSlot);
		}
		public static final BeehiveSlotProvider EMPTY=createBasic(Collections.emptyList(),Collections.emptyList(),Collections.emptyList());
		default BeehiveSlotProvider validOnly() {
			Iterable<? extends HiveSlot> combSlot=combSlot();
			final HiveSlotIterable<? extends HiveSlot> validCombSlot=combSlot instanceof HiveSlotIterable?(HiveSlotIterable<? extends HiveSlot>) combSlot:new HiveSlotIterable<>(combSlot);
			Iterable<? extends HiveSlot> droneSlot=droneSlot();
			final HiveSlotIterable<? extends HiveSlot> validDroneSlot=droneSlot instanceof HiveSlotIterable?(HiveSlotIterable<? extends HiveSlot>) droneSlot:new HiveSlotIterable<>(droneSlot);
			Iterable<? extends HiveSlot> queenSlot=queenSlot();
			final HiveSlotIterable<? extends HiveSlot> validQueenSlot=queenSlot instanceof HiveSlotIterable?(HiveSlotIterable<? extends HiveSlot>) queenSlot:new HiveSlotIterable<>(queenSlot);
			return new BeehiveSlotProvider(){
				@Override
				public Iterable<? extends HiveSlot> combSlot() {
					return validCombSlot;
				}
				@Override
				public Iterable<? extends HiveSlot> droneSlot() {
					return validDroneSlot;
				}
				@Override
				public Iterable<? extends HiveSlot> queenSlot() {
					return validQueenSlot;
				}
			};
		}
	}
	
	/**
	 * 蜂巢工作参数（可持久化版本）。
	 * 实现了 {@link ValueIOSerializable}，可以将参数映射序列化到 NBT 中，
	 * 用于在蜂巢方块实体中保存/加载工作参数。
	 */
	public static class BeehiveWorkingParams implements ValueIOSerializable{
		private Map<BeehiveParameterType<?>,Object> params=new HashMap<>();
		
		/**
		 * 将传入的参数映射按各自的合并策略合并到当前参数集中。
		 * @param params 要合并的参数映射
		 */
		public void addParams(Map<BeehiveParameterType<?>,Object> params) {
			for(Entry<BeehiveParameterType<?>, Object> ent:params.entrySet())
				ent.getKey().mergeTo(params,ent.getValue());
		}

		@Override
		public void serialize(ValueOutput output) {
			output.store("params", BeeHiveParameterRegistry.COMPOSITE_CODEC, params);
			
		}

		@Override
		public void deserialize(ValueInput input) {
			Optional<Map<BeehiveParameterType<?>, Object>> op=input.read("params", BeeHiveParameterRegistry.COMPOSITE_CODEC);
			if(op.isPresent()) {
				params=op.get();
			}
		}
	}
	/**
	 * 蜂巢参数集构建器。
	 * 采用建造者模式逐步设置环境参数、突变禁用、参数映射和生境信息，最后调用 {@link #build()} 生成不可变的 {@link BeeHiveParameterSet}。
	 */
	public static class Builder{
		ServerLevel level;
		BlockPos position;
		Set<Identifier> disabledMutation=new HashSet<>();
		Map<BeehiveParameterType<?>,Object> params=new HashMap<>();
		Set<Biotope> activeBiotopes=new HashSet<>();
		Holder<Biome> biome;
		Holder<DimensionType> type;
		BlockState state;
		BeehiveSlotProvider slots;
		boolean hasFlower;
		/**
		 * 创建一个构建器，自动从世界中获取当前位置的生物群系、维度和方块状态。
		 * @param level    服务端世界实例
		 * @param position 蜂巢方块坐标
		 */
		public Builder(ServerLevel level, BlockPos position, BeehiveSlotProvider slots) {
			super();
			this.level = level;
			this.position = position;
			biome=level.getBiomeManager().getNoiseBiomeAtPosition(position);
			type=level.dimensionTypeRegistration();
			state=level.getBlockState(position);
			this.slots=slots;
		}
		/**
		 * 禁用指定的突变。
		 * @param id 要禁用的突变 ID
		 * @return 当前构建器实例
		 */
		public Builder disableMutation(Identifier id) {
			disabledMutation.add(id);
			return this;
		};
		/**
		 * 按各类型的合并策略将传入的参数合并到当前参数映射中。
		 * @param params 要合并的参数映射
		 * @return 当前构建器实例
		 */
		public Builder addParams(Map<BeehiveParameterType<?>,Object> params) {
			for(Entry<BeehiveParameterType<?>, Object> ent:params.entrySet())
				ent.getKey().mergeTo(this.params,ent.getValue());
			return this;
		}
		/**
		 * 直接覆盖设置参数映射（替换原有值）。
		 * @param params 要设置的参数映射
		 * @return 当前构建器实例
		 */
		public Builder setParams(Map<BeehiveParameterType<?>,Object> params) {
			this.params.putAll(params);
			return this;
		}
		/**
		 * 批量添加生境集合。
		 * @param biotopes 要添加的生境集合
		 * @return 当前构建器实例
		 */
		public Builder addBiotopes(Collection<Biotope> biotopes) {
			activeBiotopes.addAll(biotopes);
			return this;
		}
		public Builder overrideHasFlower() {
			hasFlower=true;
			return this;
		}
		/**
		 * 添加单个生境。
		 * @param biotope 要添加的生境
		 * @return 当前构建器实例
		 */
		public Builder addBiotopes(Biotope biotope) {
			activeBiotopes.add(biotope);
			return this;
		}
		/**
		 * 覆盖生物群系。
		 * @param biome 要设置的生物群系
		 * @return 当前构建器实例
		 */
		public Builder overrideBiome(Holder<Biome> biome) {
			this.biome=biome;
			return this;
		}
		/**
		 * 覆盖维度类型。
		 * @param type 要设置的维度类型
		 * @return 当前构建器实例
		 */
		public Builder overrideDimensionType(Holder<DimensionType> type) {
			this.type=type;
			return this;
		}
		/**
		 * 覆盖方块状态。
		 * @param state 要设置的方块状态
		 * @return 当前构建器实例
		 */
		public Builder overrideBlockState(BlockState state) {
			this.state=state;
			return this;
		}
		
		/**
		 * 获取当前参数映射中指定参数类型的值。
		 * @param <T>  参数值类型
		 * @param type 参数类型
		 * @return 参数值，如果映射中不存在则返回 null
		 */
		@SuppressWarnings("unchecked")
		public <T> T getParamValue(BeehiveParameterType<T> type) {
			return (T) params.get(type);
		}
		/**
		 * 构建不可变的 {@link BeeHiveParameterSet} 实例。
		 * 在构建时会执行每个参数与默认值的合并，并创建不可变的副本。
		 * @return 构建完成的参数集
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public BeeHiveParameterSet build() {
			for(Entry<BeehiveParameterType<?>, Object> ent:params.entrySet()) {
				ent.setValue(((BeehiveParameterType)ent.getKey()).mergeToDefault(ent.getValue()));
			}
			return new BeeHiveParameterSet(level,position,slots,biome,type,state,Set.copyOf(disabledMutation),Map.copyOf(params),activeBiotopes,hasFlower);
		}
	}
	/**
	 * 获取指定参数类型的值，如果映射中不存在则返回该类型的默认值。
	 * @param <T>  参数值类型
	 * @param type 参数类型
	 * @return 参数值或默认值
	 */
	@SuppressWarnings("unchecked")
	public <T> T getParamValue(BeehiveParameterType<T> type) {
		T val= (T) params.get(type);
		if(val==null)
			val=type.getDefault();
		return val;
	}
	public void addBiotopes(Collection<Biotope> bios) {
		activeBiotopes.addAll(bios);
	}
	/**
	 * 检查当前参数集中是否包含指定的生境（通过参数集中的活跃生境或传入的当前生境集合判断）。
	 * @param current 当前蜜蜂自身的生境集合
	 * @param biotope 要检查的生境
	 * @return 如果参数集的活跃生境或当前生境集合包含该生境则返回 true
	 */
	public boolean hasBiotope(Biotope biotope) {
		return activeBiotopes.contains(biotope);
	}
}
