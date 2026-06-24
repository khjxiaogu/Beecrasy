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
import java.util.Set;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.beehive.slot.BeeCityCoreCombSlot;
import com.khjxiaogu.beecrasy.beehive.slot.ResourceStackHiveSlot;
import com.khjxiaogu.beecrasy.blocks.HiveSlotProvider.HiveSlotType;
import com.khjxiaogu.beecrasy.blocks.bee.beecity.BeeCityCoreBlockEntity;
import com.khjxiaogu.beecrasy.components.BeeHiveArgumentation;
import com.khjxiaogu.beecrasy.components.BeeHiveArgumentation.Builder;
import com.khjxiaogu.beecrasy.components.BeehiveArgumenter;
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.GenomeWorkHelper;
import com.khjxiaogu.beecrasy.utils.ItemValidateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
public class BeeCityQueenComponent extends AbstractBeeComponent{
	public ServerLevel level;
	public BlockPos corePos;
	/**
	 * 创建指定容量的蜂巢基础组件。
	 * 初始化所有内部槽位和资源句柄，设置各区域（蜂后/雄蜂/巢脾/额外）的验证规则。
	 * @param queen 蜂后槽位数量
	 * @param drone 雄蜂槽位数量
	 * @param comb  巢脾槽位数量
	 * @param extra 额外槽位数量
	 */
	public BeeCityQueenComponent(int queen,int drone,int comb,int extra) {
		super(queen,drone,comb,extra);


	}
	public Set<BlockPos> getConnected(ServerLevel level){
		BeeCityComponent comp=getConnectedComponent(level);
		if(comp!=null) {
			return comp.pos;
		}
		return Set.of();
	}
	public BeeCityComponent getConnectedComponent(ServerLevel level){
		if(corePos!=null&&level.isLoaded(corePos)) {
			if(level.getBlockEntity(corePos) instanceof BeeCityCoreBlockEntity be) {
				return be.component;
			}
		}
		return null;
	}
	@Override
	public BeeHiveHandler createHiveInfo(int queen,int drone,int comb,int extra) {
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
		return new BeeHiveHandler(
			()-> new BeeCityIterator(queenSlot.iterator(),level,getConnected(level).iterator(),HiveSlotType.QUEEN),
			()-> new BeeCityIterator(droneSlot.iterator(),level,getConnected(level).iterator(),HiveSlotType.COMB),
			()-> new BeeCityIterator(combSlot.iterator(),level,getConnected(level).iterator(),HiveSlotType.COMB));
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
		BeeHiveArgumentation arg1=BeehiveArgumenter.extractArgumentation(level, internInv, 1, root);
		if(arg1!=null)
			builder.addParams(arg1);
		builder.addParam(BeeHiveParameters.YIELD,getConnected(level).size()*0.1f);
		builder.addParam(BeeHiveParameters.SPEED,getConnected(level).size()*0.1f);
		builder.addParam(BeeHiveParameters.FERTILITY,getConnected(level).size()*0.1f);
		return builder;
	}
	protected boolean canBeginWork(ServerLevel level) {
		BeeCityComponent coreComp=getConnectedComponent(level);
		if(coreComp==null||!coreComp.hiveInfo.isWorking()) {
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
		err=ErrCode.OK;
		return true;
	}
	protected boolean beginGrowth(ServerLevel serverLevel, BlockPos worldPosition) {
		Genome[] queen=null;
		BeeHiveParameterSet params=null;
		BeeCityComponent coreComp=getConnectedComponent(level);
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
			if(coreComp.hiveInfo.droneGenomes.size()<=0)
				return false;
			arguments=buildArgumentation(serverLevel,worldPosition,trans).build();
	
			params=buildParams(serverLevel, worldPosition).build();
			if(!GenomeWorkHelper.isValidEnvironment(params, queen[0])) {
				err=ErrCode.INVALID_ENVIRONMENT;
				return false;
			}
			trans.commit();
		}
		hiveInfo.prepareWork(params, queen, coreComp.hiveInfo.droneGenomes);
		return true;
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
		corePos=nbt.read("corePos", BlockPos.CODEC).orElse(null);
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
		nbt.storeNullable("corePos", BlockPos.CODEC, corePos);
		
	}

}
