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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.beehive.slot.ResourceStackHiveSlot;
import com.khjxiaogu.beecrasy.beehive.slot.StacksHiveSlot;
import com.khjxiaogu.beecrasy.components.BeeHiveArgumentation;
import com.khjxiaogu.beecrasy.components.BeehiveArgumenter;
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.GenomeWorkHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * 蜂巢基础组件。
 * 蜂巢方块的顶层逻辑组件，管理：
 * <ul>
 *   <li>物品槽位（蜂后/雄蜂/巢脾/额外槽位）</li>
 *   <li>工作模式（手动/自动/红石）</li>
 *   <li>工作冷却计时</li>
 *   <li>参数构建与参数增强项（Argumentation）提取</li>
 *   <li>与 {@link BeeHiveHandler} 的协调</li>
 * </ul>
 * 实现了 {@link ValueIOSerializable} 用于持久化。
 */
public class BeeHiveBaseComponent extends AbstractBeeComponent{
	
	/**
	 * 蜂巢基础数据记录。
	 * 用于磁盘存档和网络同步的不可变数据快照，包含所有槽位、蜂巢处理器状态、参数增强项、冷却时间和工作模式。
	 */
	public static class BeeHiveBaseData{
		
		public static final Codec<BeeHiveBaseData> CODEC=RecordCodecBuilder.create(t->t
			.group(StacksHiveSlot.LIST_CODEC.fieldOf("queen").forGetter(BeeHiveBaseData::queenSlot),
				StacksHiveSlot.LIST_CODEC.fieldOf("comb").forGetter(BeeHiveBaseData::combSlot),
				StacksHiveSlot.LIST_CODEC.fieldOf("drone").forGetter(BeeHiveBaseData::droneSlot),
				BeeHiveHandler.DataRecord.CODEC.fieldOf("hive").forGetter(BeeHiveBaseData::hiveInfo),
				BeeHiveArgumentation.CODEC.optionalFieldOf("arguments").forGetter(BeeHiveBaseData::arguments),
				Codec.INT.optionalFieldOf("begining",0).forGetter(BeeHiveBaseData::beginingTicks),
				WorkBehaviour.CODEC.fieldOf("work").forGetter(BeeHiveBaseData::work)
				)
			.apply(t, BeeHiveBaseData::new));
		public static final StreamCodec<RegistryFriendlyByteBuf,BeeHiveBaseData> STREAM_CODEC=StreamCodec
				.composite(StacksHiveSlot.LIST_STREAM_CODEC,BeeHiveBaseData::queenSlot,
					StacksHiveSlot.LIST_STREAM_CODEC,BeeHiveBaseData::combSlot,
					StacksHiveSlot.LIST_STREAM_CODEC,BeeHiveBaseData::droneSlot,
					BeeHiveHandler.DataRecord.STREAM_CODEC,BeeHiveBaseData::hiveInfo,
					ByteBufCodecs.optional(BeeHiveArgumentation.STREAM_CODEC),BeeHiveBaseData::arguments,
					ByteBufCodecs.VAR_INT,BeeHiveBaseData::beginingTicks,
					WorkBehaviour.STREAM_CODEC,BeeHiveBaseData::work,
					BeeHiveBaseData::new
					);
		protected final List<StacksHiveSlot> queenSlot;
		/** 巢脾/产物槽位列表。 */
		protected final List<StacksHiveSlot> combSlot;
		/** 雄蜂槽位列表。 */
		protected final List<StacksHiveSlot> droneSlot;
		/** 蜂巢处理器数据快照。 */
		protected final BeeHiveHandler.DataRecord hiveInfo;
		/** 参数增强项（可选，如信息素等物品提供的临时参数加成）。 */
		protected final Optional<BeeHiveArgumentation> arguments;
		/** 冷却计时器（工作开始前的等待 tick 数）。 */
		protected final int beginingTicks;
		/** 工作模式。 */
		protected final WorkBehaviour work;
		/**
		 * 从现有的 HiveSlot 列表创建数据记录（深拷贝槽位内容）。
		 * @param queenSlot    蜂后槽位列表
		 * @param combSlot     巢脾槽位列表
		 * @param droneSlot    雄蜂槽位列表
		 * @param hiveInfo     蜂巢处理器数据快照
		 * @param arguments    参数增强项
		 * @param beginingTicks 冷却计时
		 * @param work         工作模式
		 */
		public BeeHiveBaseData(List<? extends HiveSlot> queenSlot, List<? extends HiveSlot> combSlot,
			List<? extends HiveSlot> droneSlot,
			BeeHiveHandler.DataRecord hiveInfo, Optional<BeeHiveArgumentation> arguments,int beginingTicks, WorkBehaviour work) {
			super();
			
			this.queenSlot = StacksHiveSlot.createSlots(queenSlot);
			this.combSlot = StacksHiveSlot.createSlots(combSlot);
			this.droneSlot = StacksHiveSlot.createSlots(droneSlot);
			this.hiveInfo = hiveInfo;
			this.arguments = arguments;
			this.beginingTicks = beginingTicks;
			this.work=work;
		}
		/**
		 * 通过指定槽位数创建数据记录（所有槽位初始为空）。
		 * @param queenSlot    蜂后槽位数量
		 * @param combSlot     巢脾槽位数量
		 * @param droneSlot    雄蜂槽位数量
		 * @param hiveInfo     蜂巢处理器数据快照
		 * @param arguments    参数增强项
		 * @param work         工作模式
		 */
		public BeeHiveBaseData(int queenSlot,int combSlot,
				int droneSlot,
				BeeHiveHandler.DataRecord hiveInfo, Optional<BeeHiveArgumentation> arguments, WorkBehaviour work) {
				super();
				
				this.queenSlot = StacksHiveSlot.createSlots(queenSlot);
				this.combSlot = StacksHiveSlot.createSlots(combSlot);
				this.droneSlot = StacksHiveSlot.createSlots(droneSlot);
				this.hiveInfo = hiveInfo;
				this.arguments = arguments;
				this.beginingTicks = 0;
				this.work=work;
			}
		/**
		 * 从现有的 BeeHiveBaseComponent 实例创建数据记录快照。
		 * @param comp 源组件
		 */
		public BeeHiveBaseData(BeeHiveBaseComponent comp) {
			this(comp.queenSlot,comp.combSlot,comp.droneSlot,comp.hiveInfo.save(),Optional.ofNullable(comp.arguments),comp.beginingTicks,comp.work);
		}
		@Override
		public int hashCode() {
			return Objects.hash(arguments, combSlot, droneSlot, hiveInfo, queenSlot, work);
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			BeeHiveBaseData other = (BeeHiveBaseData) obj;
			return Objects.equals(arguments, other.arguments) && Objects.equals(combSlot, other.combSlot) && Objects.equals(droneSlot, other.droneSlot) && Objects.equals(hiveInfo, other.hiveInfo)
				&& Objects.equals(queenSlot, other.queenSlot) && work == other.work;
		}
		public List<StacksHiveSlot> queenSlot() {
			return queenSlot;
		}
		public List<StacksHiveSlot> combSlot() {
			return combSlot;
		}
		public List<StacksHiveSlot> droneSlot() {
			return droneSlot;
		}
		public BeeHiveHandler.DataRecord hiveInfo() {
			return hiveInfo;
		}
		public Optional<BeeHiveArgumentation> arguments() {
			return arguments;
		}
		public int beginingTicks() {
			return beginingTicks;
		}
		public WorkBehaviour work() {
			return work;
		}
		
	}

	/**
	 * 从数据记录创建蜂巢基础组件。
	 * @param data 包含初始状态的数据记录
	 */
	public BeeHiveBaseComponent(BeeHiveBaseData data) {
		this(data.queenSlot.size(),data.droneSlot.size(),data.combSlot.size(),0);
		load(data);
	}
	/**
	 * 创建指定容量的蜂巢基础组件。
	 * 初始化所有内部槽位和资源句柄，设置各区域（蜂后/雄蜂/巢脾/额外）的验证规则。
	 * @param queen 蜂后槽位数量
	 * @param drone 雄蜂槽位数量
	 * @param comb  巢脾槽位数量
	 * @param extra 额外槽位数量
	 */
	public BeeHiveBaseComponent(int queen,int drone,int comb,int extra) {
		super(queen,drone,comb,extra);
	}
	@Override
	public BeeHiveHandler createHiveInfo(int queen,int drone,int comb,int extra) {
		queenSlot=new ArrayList<>(queen);
		for(int i=0;i<queen;i++) {
			queenSlot.add(new ResourceStackHiveSlot(getInternInv(),i));
		}
		droneSlot=new ArrayList<>(drone);
		for(int i=0;i<drone;i++) {
			droneSlot.add(new ResourceStackHiveSlot(getInternInv(),i+queen));
		}
		combSlot=new ArrayList<>(comb);
		for(int i=0;i<comb;i++) {
			combSlot.add(new ResourceStackHiveSlot(getInternInv(),i+queen+drone));
		}
		extraSlot=new ArrayList<>(extra);
		for(int i=0;i<extra;i++) {
			extraSlot.add(new ResourceStackHiveSlot(getInternInv(),i+queen+drone+comb));
		}
		return new BeeHiveHandler(queenSlot,droneSlot,combSlot);
	}
	@Override
	public boolean isValidForExtra(int index, ItemResource resource) {
		return false;
	}
	/**
	 * 从指定槽位的物品中提取参数增强项。
	 * 如果物品带有 {@link BeehiveArgumenter} 组件，根据其 consumeOnUse 属性决定是否消耗物品，
	 * 并返回其中的参数修饰器。
	 * @param serverLevel 服务端世界
	 * @param slot        槽位索引
	 * @param root        事务上下文
	 * @return 参数增强项，如果物品不适用或提取失败则返回 null
	 */
	protected BeeHiveArgumentation extractArgumentation(ServerLevel serverLevel,int slot,TransactionContext root) {
		return BeehiveArgumenter.extractArgumentation(serverLevel, internInv, slot, root);
	}
	@Override
	protected boolean canBeginWork() {

		if(work==WorkBehaviour.MAUNAL&&!shouldWork) {
			err=ErrCode.MANUAL_HALT;
			return false;
		}
		int queenCount=0;
		for(HiveSlot slot:queenSlot) {
			if(!slot.isEmpty()) {
				if(slot.is(Items.QUEEN_BEE)) {
					queenCount++;
				}else {
					err=ErrCode.MALFORMED_SLOT;
					return false;
				}
			}
		}
		if(queenCount>1) {
			err=ErrCode.EXTRA_QUEEN;
			return false;
		}else if(queenCount==0) {
			err=ErrCode.MISSING_QUEEN;
			return false;
		}
		int droneCount=0;
		for(HiveSlot slot:droneSlot) {
			if(!slot.isEmpty()) {
				if(slot.is(Items.DRONE)) {
					droneCount++;
				}else {
					err=ErrCode.MALFORMED_SLOT;
					return false;
				}
			}
		}
		for(HiveSlot slot:combSlot) {
			if(!slot.isEmpty()) {
				if(slot.is(Items.DRONE)) {
					droneCount++;
				}else {
					err=ErrCode.MALFORMED_SLOT;
					return false;
				}
			}
		}
		if(droneCount<=0) {
			err=ErrCode.MISSING_DRONE;
			return false;
		}
		err=ErrCode.OK;
		return true;
	}
	@Override
	protected boolean beginGrowth(ServerLevel serverLevel, BlockPos worldPosition) {
		Genome[] queen=null;
		BeeHiveParameterSet params=null;
		List<Genome> drones=new ArrayList<>(10);
		try(Transaction trans=Transaction.openRoot()){
			int qn=queenSlot.size();
			for(int i=0;i<qn;i++) {
				if(getInternInv().getAmountAsInt(i)>0) {
					ItemResource stack=getInternInv().getResource(i);
					if(stack.is(Items.QUEEN_BEE.get())) {
						GenomeComponent comp=stack.get(Components.GENOME);
						if(comp!=null) {
							if(getInternInv().extract(i, stack, 1, trans)==1) {
								queen=comp.toArray();
								break;
							}
						}
					}
				}
			}
			if(queen==null)
				return false;
			int maxSlot=droneSlot.size()+combSlot.size();
			for(int i=0;i<maxSlot;i++) {
				int slot=qn+i;
				if(getInternInv().getAmountAsInt(slot)>0) {
					ItemResource stack=getInternInv().getResource(slot);
					if(stack.is(Items.DRONE.get())) {
						GenomeComponent comp=stack.get(Components.GENOME);
						if(comp!=null) {
							if(getInternInv().extract(slot, stack, 1, trans)==1)
								drones.add(comp.getGenome(0));
						}
					}
				}
			}
			if(drones.size()<=0)
				return false;
			arguments=buildArgumentation(serverLevel,worldPosition,trans).build();

			params=buildParams(serverLevel, worldPosition).build();
			if(!GenomeWorkHelper.isValidEnvironment(params, queen[0])) {
				err=ErrCode.INVALID_ENVIRONMENT;
				return false;
			}
			trans.commit();
		}

		hiveInfo.prepareWork(params, queen, drones);
		return true;
	}
	/**
	 * 从数据记录加载状态（覆盖当前内存状态）。
	 * @param data 源数据记录
	 */
	public void load(BeeHiveBaseData data) {
		HiveSlot.copy(data.queenSlot, queenSlot);
		HiveSlot.copy(data.droneSlot, droneSlot);
		HiveSlot.copy(data.combSlot, combSlot);
		hiveInfo.read(data.hiveInfo);
		arguments=data.arguments.orElse(null);
		work=data.work;
	}
	/**
	 * 将当前状态保存为数据记录。
	 * @return 包含当前状态快照的数据记录
	 */
	public BeeHiveBaseData save() {
		return new BeeHiveBaseData(this);
	}

}
