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
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.khjxiaogu.beecrasy.BeecrasyConfig;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Capability;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterSet.BeehiveSlotProvider;
import com.khjxiaogu.beecrasy.beehive.slot.BeeCityCoreCombSlot;
import com.khjxiaogu.beecrasy.beehive.slot.ResourceStackHiveSlot;
import com.khjxiaogu.beecrasy.blocks.bee.beecity.BeeCitySpreadHelper;
import com.khjxiaogu.beecrasy.blocks.bee.beecity.HiveSlotProvider;
import com.khjxiaogu.beecrasy.blocks.bee.beecity.HiveSlotProvider.HiveSlotType;
import com.khjxiaogu.beecrasy.components.BeeHiveArgumentation;
import com.khjxiaogu.beecrasy.components.BeeHiveArgumentation.Builder;
import com.khjxiaogu.beecrasy.components.BeehiveArgumenter;
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.genome.Genes.Alleles;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.GenomeWorkHelper;
import com.khjxiaogu.beecrasy.genome.gene.Biotope;
import com.khjxiaogu.beecrasy.utils.ItemValidateHelper;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * 巨构蜂巢组件。
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
public class BeeCityComponent extends AbstractBeeComponent{

	protected LinkedHashMap<BlockPos,BitSet> pos=new LinkedHashMap<>();
	protected Set<Biotope> biotopes;
	protected int[] byBiotopes=new int[Alleles.BIOTOPE.size()+1];
	protected Set<Biotope> currentBiotopes;
	private static final Codec<List<Pair<BlockPos,BitSet>>> POS_CODEC=Codec.list(Codec.mapPair(BlockPos.CODEC.fieldOf("pos"), ExtraCodecs.BIT_SET.fieldOf("biotope")).codec());
	/**
	 * 创建指定容量的蜂巢基础组件。
	 * 初始化所有内部槽位和资源句柄，设置各区域（蜂后/雄蜂/巢脾/额外）的验证规则。
	 * @param queen 蜂后槽位数量
	 * @param drone 雄蜂槽位数量
	 * @param comb  巢脾槽位数量
	 * @param extra 额外槽位数量
	 */
	public BeeCityComponent(int queen,int drone,int comb,int extra) {
		super(queen,drone,comb,extra);
		queenSlot=new ArrayList<>(queen);
		for(int i=0;i<queen;i++) {
			queenSlot.add(new ResourceStackHiveSlot(getInternInv(),i));
		}
		droneSlot=new ArrayList<>(drone);
		if(drone>0)
			for(int i=0;i<drone;i++) {
				droneSlot.add(new ResourceStackHiveSlot(getInternInv(),i+queen));
			}
		combSlot=new ArrayList<>(comb);
		if(comb>0)
			for(int i=0;i<comb;i++) {
				combSlot.add(new BeeCityCoreCombSlot(getInternInv(),i+queen+drone));
			}
		extraSlot=new ArrayList<>(extra);
		for(int i=0;i<extra;i++) {
			extraSlot.add(new ResourceStackHiveSlot(getInternInv(),i+queen+drone+comb));
		}

	}
	public void appendHiveCity(BlockPos pos) {
		if(!this.pos.containsKey(pos))
			this.pos.put(pos,this.createBitSet());
		this.setChanged();
	}

	@Override
	public BeehiveSlotProvider createHiveInfo(ServerLevel serverLevel, BlockPos worldPosition) {
		Iterable<? extends HiveSlot> comb=()-> new BeeCityIterator(droneSlot.iterator(), serverLevel, worldPosition,pos.keySet().iterator(), HiveSlotType.COMB);
		return BeehiveSlotProvider.createBasic(
				comb, 
				comb, 
				()-> new BeeCityIterator(queenSlot.iterator(), serverLevel, worldPosition,pos.keySet().iterator(), HiveSlotType.QUEEN)).validOnly();
	}
	/**
	 * 验证额外槽位是否允许放入指定物品。
	 * 子类可重写此方法以添加自定义槽位规则。
	 * @param index    额外槽位索引
	 * @param resource 待验证的物品资源
	 * @return 如果允许放入则返回 true
	 */
	@Override
	public boolean isValidForExtra(int index, ItemResource resource) {
		return ItemValidateHelper.isArgument(resource.toStack());
	}
	public Builder buildArgumentation(ServerLevel level, BlockPos worldPosition, TransactionContext root) {
		
		Builder builder= super.buildArgumentation(level, worldPosition, root);
		BeeHiveArgumentation arg1=BeehiveArgumenter.extractArgumentation(level, internInv, 5, root);
		if(arg1!=null)
			builder.addParams(arg1);
		builder.addParam(BeeHiveParameters.YIELD,pos.size()*0.1f);
		builder.addParam(BeeHiveParameters.SPEED,pos.size()*0.1f);
		builder.addParam(BeeHiveParameters.FERTILITY,pos.size()*0.1f);
		return builder;
	}

	protected boolean canBeginWork(ServerLevel level, BlockPos worldPosition) {
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
		for(HiveSlot slot:(Iterable<HiveSlot>)(()-> new BeeCityIterator(Iterators.concat(droneSlot.iterator(), combSlot.iterator()),level,worldPosition,pos.keySet().iterator(),HiveSlotType.COMB))) {
			if(!slot.isEmpty()) {
				if(slot.is(Items.DRONE)) {
					droneCount++;
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
	protected void tickExtraWorking(BeeHiveParameterSet params, int time) {
		super.tickExtraWorking(params, time);
		for(int i=0;i<time;i++) {
			Set<BlockPos> cps=new HashSet<>(pos.keySet());
			for(int j=0;j<2;j++) {
				int index=params.level().getRandom().nextInt(pos.size()+1);
				BlockPos checkPos;
				if(index==0)
					checkPos=params.position();
				else
					checkPos=Iterators.get(pos.keySet().iterator(), index-1);
				GenomeWorkHelper.transformFlowers(params.level(), checkPos, Math.round(BeecrasyConfig.SERVER.FLOWER_RADIUS.getAsInt()*params.getParamValue(BeeHiveParameters.RADIUS)), (float)BeecrasyConfig.SERVER.FLOWER_RATE.getAsDouble());
				Pair<BlockPos, Direction> result=BeeCitySpreadHelper.findFirstValid(params.level(), params.position(), checkPos, cps);
				if(result!=null) {
					if(params.level().getCapability(Capability.BEE_CITY_BLOCK,result.getFirst()) instanceof HiveSlotProvider provider&&provider.isBindable(params.position())) {
						provider.bind(params.position());
						this.appendHiveCity(result.getFirst());
						return;
					}
				}
				
			}
		}
		
	}
	protected boolean beginGrowth(ServerLevel serverLevel, BlockPos worldPosition) {
		Genome[] queen=null;
		BeeHiveParameterSet params=null;
		List<Genome> drones=new ArrayList<>(10);
		List<HiveSlot> empties=new ArrayList<>();
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
			arguments=buildArgumentation(serverLevel,worldPosition,trans).build();
			params=buildParams(serverLevel, worldPosition).build();
			if(!GenomeWorkHelper.isValidEnvironment(params, queen[0])) {
				err=ErrCode.INVALID_ENVIRONMENT;
				return false;
			}
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
			for(HiveSlot slot:(Iterable<HiveSlot>)(()-> new BeeCityIterator(Collections.emptyIterator(),serverLevel,worldPosition,pos.keySet().iterator(),HiveSlotType.COMB))) {
				if(slot.is(Items.DRONE)) {
					ItemStack stack=slot.getItem();
					GenomeComponent comp=stack.get(Components.GENOME);
					if(comp!=null) {
						drones.add(comp.getGenome(0));
						empties.add(slot);
					}
				}
			}
			if(drones.size()<=0)
				return false;
			trans.commit();
		}
		for(HiveSlot hs:empties)
			hs.setItem(ItemStack.EMPTY);
		hiveInfo.prepareWork(params, queen, drones);
		return true;
	}
	private BitSet createBitSet() {
		return new BitSet(Alleles.BIOTOPE.size()+1);
	}
	/**
	 * 从 NBT 读取数据（支持客户端/服务端区分）。
	 * 服务端读取完整数据（库存、蜂巢处理器、参数增强项），客户端只读取界面相关数据。
	 * @param nbt      输入源
	 * @param isClient 是否为客户端读取
	 */
	@Override
	public void readCustomNBT(ValueInput nbt, boolean isClient) {
		super.readCustomNBT(nbt, isClient);
		if(!isClient) {
			pos.clear();
			List<Biotope> bt=nbt.read("biotopes", Codec.list(Alleles.BIOTOPE.CODEC.orElse(null))).orElse(List.of());
			IntList il=new IntArrayList();
			for(Biotope bts:bt) {
				if(bts!=null) {
					il.add(Alleles.BIOTOPE.getIntId(bts));
				}else {
					il.add(-1);
				}
			}
			Arrays.fill(byBiotopes, 0);
			
			List<Pair<BlockPos, BitSet>> map=nbt.read("connected", POS_CODEC).orElse(List.of());
			if(!Iterables.elementsEqual(bt, Alleles.BIOTOPE)) {
				for(Pair<BlockPos, BitSet> ent:map) {
					BitSet bs=ent.getSecond();
					BitSet nbs=this.createBitSet();
					for(int i=0;i<il.size();i++) {
						int idx=il.getInt(i);
						if(idx>=0) {
							nbs.set(idx, bs.get(i));
						}
					}
					nbs.set(nbs.size()-1,bs.get(bs.size()-1));
					this.computeBits(nbs,1);
					pos.put(ent.getFirst(), nbs);
				}
			}else {
				for(Pair<BlockPos, BitSet> ent:map) {
					pos.put(ent.getFirst(),ent.getSecond());
					this.computeBits(ent.getSecond(), 1);
				}
			}
			biotopes=nbt.read("currentBiotope", Codec.list(Alleles.BIOTOPE.CODEC)).map(t->new HashSet<>(t)).orElse(null);
			computeBiotope();
		}
	}
	public void computeBits(BitSet bs,int sign) {
		for(int i=0;i<byBiotopes.length;i++) {
			if(bs.get(i))
				byBiotopes[i]+=sign;
		}
	}
	/**
	 * 将数据写入 NBT（支持客户端/服务端区分）。
	 * 服务端写入完整数据，客户端只写入界面相关数据。
	 * @param nbt      输出目标
	 * @param isClient 是否为客户端写入
	 */
	@Override
	public void writeCustomNBT(ValueOutput nbt, boolean isClient) {
		super.writeCustomNBT(nbt, isClient);
		if(!isClient) {
			
			List<Biotope> bt=new ArrayList<>(Alleles.BIOTOPE.size());
			for(Biotope bts:Alleles.BIOTOPE) {
				bt.add(bts);
			}
			nbt.store("biotopes", Codec.list(Alleles.BIOTOPE.CODEC), bt);
			nbt.store("connected", POS_CODEC, pos.sequencedEntrySet().stream().map(o->Pair.of(o.getKey(), o.getValue())).toList());
			if(biotopes!=null)
				nbt.store("currentBiotope", Codec.list(Alleles.BIOTOPE.CODEC),new ArrayList<>(biotopes));
		}
	}
	@Override
	public BeeHiveParameterSet.Builder buildParams(ServerLevel serverLevel,
			BlockPos worldPosition) {
		BeeHiveParameterSet.Builder builder = super.buildParams(serverLevel, worldPosition);
		if(currentBiotopes!=null) {
			builder.addBiotopes(currentBiotopes);
			builder.overrideHasFlower();
		}
		return builder;
	}
	public void computeBiotope() {
		Set<Biotope> bt=new HashSet<>();
		for(int i=0;i<byBiotopes.length-1;i++) {
			if(byBiotopes[i]>0){
				bt.add(Alleles.BIOTOPE.getByInt(i));
			}
		}
		if(biotopes!=null)
			bt.addAll(biotopes);
		if(byBiotopes[byBiotopes.length-1]<=0&&bt.isEmpty()&&biotopes==null) {
			bt=null;
		}
		currentBiotopes=bt;
		this.setChanged();
	}
	@Override
	protected void tickBeforeWorking(BeeHiveParameterSet params) {
		super.tickBeforeWorking(params);
		float tickChance=pos.size()/100f;
		if(tickChance>=params.level().getRandom().nextInt(1000)/1000f) {
			int index=params.level().getRandom().nextInt(pos.size()+1);
			BlockPos checkPos;
			if(index==0) {
				biotopes=updateBiotopes(params);
				computeBiotope();
				return ;
			}else
				checkPos=Iterators.get(pos.keySet().iterator(), index-1);
			Set<Biotope> bt=GenomeWorkHelper.findBiotope(params.level(), checkPos, (int)(BeecrasyConfig.SERVER.RADIUS.getAsInt()*params.getParamValue(BeeHiveParameters.RADIUS)));
			BitSet bs=pos.get(checkPos);
			if(bs!=null)
				this.computeBits(bs, -1);
			bs=this.createBitSet();
			if(bt!=null) {
				for(Biotope bts:bt)
					bs.set(Alleles.BIOTOPE.getIntId(bts),true);
				bs.set(bs.length()-1,true);
			}
			this.computeBits(bs, 1);
			pos.put(checkPos, bs);
			computeBiotope();
		}
	}
}
