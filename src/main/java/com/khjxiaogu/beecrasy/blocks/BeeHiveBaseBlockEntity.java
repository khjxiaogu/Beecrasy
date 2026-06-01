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

package com.khjxiaogu.beecrasy.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyConfig;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.beehive.BeeHiveHandler;
import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterSet;
import com.khjxiaogu.beecrasy.beehive.slot.ResourceStackHiveSlot;
import com.khjxiaogu.beecrasy.components.BeeHiveArgumentation;
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.genome.BiotopeHelper;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.gene.Biotope;
import com.khjxiaogu.beecrasy.utils.LazyTickWorker;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class BeeHiveBaseBlockEntity extends BeecrasyBlockEntity {
	public static enum WorkBehaviour{
		MAUNAL,//人工
		AUTO,//自动
		REDSTONE;//红石
		private final String key="gui.beehive.control."+this.name().toLowerCase();
		private final Component text=Component.translatable(key);
		public static final Codec<WorkBehaviour> CODEC=Codec.INT.xmap(i->WorkBehaviour.values()[i], WorkBehaviour::ordinal);
		public Component getComponents() {
			return text;
		}
		public String getTranslationKey() {
			return key;
		}
	}
	public static enum ErrCode{
		OK,//无错误
		MANUAL_HALT,//手动模式下，需要手动放入蜂后
		MISSING_QUEEN,//无蜂后
		EXTRA_QUEEN,//蜂后过多
		MISSING_DRONE,//无雄蜂
		MALFORMED_SLOT,//蜂巢格子有非雄蜂/蜂后
		NO_FLOWER,//无花
		NO_BIOTOPE,//部分蜜蜂生境不符（警告）
		EMPTY_QUEEN;//王台为空
		private final String key="gui.beehive.status."+this.name().toLowerCase();
		private final Component text=Component.translatable(key);
		public static final Codec<ErrCode> CODEC=Codec.INT.xmap(i->ErrCode.values()[i], ErrCode::ordinal);
		public Component getComponents() {
			return text;
		}
		public String getTranslationKey() {
			return key;
		}
	}
	protected ItemStacksResourceHandler internInv;
	protected List<ResourceStackHiveSlot> queenSlot;
	protected List<ResourceStackHiveSlot> combSlot;
	protected List<ResourceStackHiveSlot> droneSlot;
	protected boolean hasBiotope=false;
	protected Set<Biotope> biotopes;
	protected BeeHiveHandler hiveInfo;
	protected BeeHiveArgumentation arguments;
	protected boolean shouldWork;
	protected int beginingTicks=0;
	LazyTickWorker biotopeUpdater=new LazyTickWorker(200,()->{
		if(hasBiotope)
			updateBiotopes();
		
		return hasBiotope;
	});
	public static final int COOLDOWN_TIME=100;
	public void updateBiotopes() {
		biotopes=BiotopeHelper.findBiotope(level, worldPosition, BeecrasyConfig.SERVER.RADIUS.getAsInt());
		//System.out.println("find biotope "+biotopes);
		hasBiotope=true;
	}
	public WorkBehaviour work=WorkBehaviour.MAUNAL;
	ErrCode err=ErrCode.OK;
	public BeeHiveBaseBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState,int queen,int drone,int comb,int extra) {
		super(pType, pWorldPosition, pBlockState);
		internInv = new ItemStacksResourceHandler(queen+drone+comb+extra) {
			@Override
			protected void onContentsChanged(int slot, ItemStack stack) {
				if(slot<queen&&this.getAmountAsInt(slot)>0&&!hiveInfo.isWorking()) {
					shouldWork=true;
				}
				beginingTicks=0;
				setChanged();
				super.onContentsChanged(slot, stack);
			}
			
		};
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
		hiveInfo= new BeeHiveHandler(queenSlot,droneSlot,combSlot);
	}

	@Override
	public void readCustomNBT(ValueInput nbt, boolean isClient) {
		if(!isClient) {
			nbt.readChild("inv", getInternInv());
			nbt.readChild("hive", hiveInfo);
			shouldWork=nbt.getBooleanOr("nextWork", false);
			arguments=nbt.read("arguments", BeeHiveArgumentation.CODEC).orElse(null);
		}
		beginingTicks=nbt.getIntOr("cooldown", 0);
		work=nbt.read("work", WorkBehaviour.CODEC).orElse(WorkBehaviour.MAUNAL);
		err=nbt.read("err", ErrCode.CODEC).orElse(ErrCode.OK);
	}

	@Override
	public void writeCustomNBT(ValueOutput nbt, boolean isClient) {
		if(!isClient) {
			nbt.putChild("inv", getInternInv());
			nbt.putChild("hive", hiveInfo);
			nbt.putBoolean("nextWork", shouldWork);
			if(arguments!=null)
				nbt.store("arguments", BeeHiveArgumentation.CODEC, arguments);
		}
		nbt.putInt("cooldown", beginingTicks);
		nbt.store("work", WorkBehaviour.CODEC, work);
		nbt.store("err", ErrCode.CODEC, err);
	}
	public BeeHiveParameterSet.Builder buildParams(ServerLevel serverLevel){
		BeeHiveParameterSet.Builder builder= new BeeHiveParameterSet.Builder(serverLevel,worldPosition);
		if(arguments!=null)
			builder.setParams(arguments.params());
		return builder;
	}
	public BeeHiveArgumentation.Builder buildArgumentation() {
		return new BeeHiveArgumentation.Builder();
	}
	public boolean canBeginWork() {
		int queenCount=0;
		for(ResourceStackHiveSlot slot:queenSlot) {
			if(!slot.isEmpty()) {
				if(slot.getItem().is(Items.QUEEN_BEE)) {
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
		for(ResourceStackHiveSlot slot:droneSlot) {
			if(!slot.isEmpty()) {
				if(slot.getItem().is(Items.DRONE)) {
					droneCount++;
				}else {
					err=ErrCode.MALFORMED_SLOT;
					return false;
				}
			}
		}
		for(ResourceStackHiveSlot slot:combSlot) {
			if(!slot.isEmpty()) {
				if(slot.getItem().is(Items.DRONE)) {
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
		if(work==WorkBehaviour.MAUNAL&&!shouldWork) {
			err=ErrCode.MANUAL_HALT;
			return false;
		}
		err=ErrCode.OK;
		return true;
	}
	public boolean beginGrowth(ServerLevel serverLevel) {
		try(Transaction trans=Transaction.openRoot()){
			Genome[] queen=null;

			int qn=queenSlot.size();
			for(int i=0;i<qn;i++) {
				if(getInternInv().getAmountAsInt(i)>0) {
					ItemResource stack=getInternInv().getResource(i);
					if(stack.is(Items.QUEEN_BEE.get())) {
						@Nullable GenomeComponent comp=stack.get(Components.GENOME);
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
			List<Genome> drones=new ArrayList<>();
			int maxSlot=droneSlot.size()+combSlot.size();
			for(int i=0;i<maxSlot;i++) {
				int slot=qn+i;
				if(getInternInv().getAmountAsInt(slot)>0) {
					ItemResource stack=getInternInv().getResource(slot);
					if(stack.is(Items.DRONE.get())) {
						@Nullable GenomeComponent comp=stack.get(Components.GENOME);
						if(comp!=null) {
							if(getInternInv().extract(slot, stack, 1, trans)==1)
								drones.add(comp.getGenome(0));
						}
					}
				}
			}
			if(drones.size()<=0)
				return false;
			trans.commit();
			arguments=buildArgumentation().build();
			BeeHiveParameterSet params=buildParams(serverLevel).build();
			hiveInfo.beginWork(params, queen, drones);
			
		}
		return true;
	}
	public ContainerData containerData() {
		return new ContainerData() {

			@Override
			public int get(int dataId) {
				switch(dataId) {
				case 0:return err.ordinal();
				case 1:return work.ordinal();
				case 2:return beginingTicks>0?beginingTicks:hiveInfo.getProcess();
				case 3:return beginingTicks>0?COOLDOWN_TIME:hiveInfo.getProcessMax();
				}
				return 0;
			}

			@Override
			public void set(int dataId, int value) {
				switch(dataId) {
				case 0:err=ErrCode.values()[value];break;
				case 1:work=WorkBehaviour.values()[value];break;
				}
			}

			@Override
			public int getCount() {
				return 4;
			}
			
		};
	}
	@Override
	public void tick() {
		if(level instanceof ServerLevel serverLevel) {
			switch(work) {
			case AUTO:shouldWork=true;break;
			case REDSTONE:shouldWork=level.hasNeighborSignal(this.worldPosition);break;
			default:
				break;
			}
			
			if(beginingTicks>0) {
				beginingTicks++;
				if(beginingTicks>=COOLDOWN_TIME) {
					beginingTicks=0;
					if(canBeginWork()) {
						
						shouldWork=false;
						updateBiotopes();
						if(beginGrowth(serverLevel)) {
							this.setChanged();
						}
					}
				}
			}else if(hiveInfo.isWorking()) {
				err=ErrCode.OK;
				if(!hasBiotope)
					updateBiotopes();
				else
					biotopeUpdater.tick();
				if(biotopes==null) {
					err=ErrCode.NO_FLOWER;
				}else {
					BeeHiveParameterSet params=buildParams(serverLevel).addBiotopes(biotopes).build();
					hiveInfo.tick(params);
					if(hiveInfo.isBlocked())
						err=ErrCode.EMPTY_QUEEN;
					else if(hiveInfo.isNoBiotope())
						err=ErrCode.NO_BIOTOPE;
				}
			}else if(shouldWork) {
				if(canBeginWork()) {
					beginingTicks=1;
				}
			}else {
				err=ErrCode.MANUAL_HALT;
				hasBiotope=false;
			}
			
			
			boolean oldstate=this.getBlockState().getValue(BlockStateProperties.LIT);
			boolean newstate=hiveInfo.isWorking();
			if(oldstate!=newstate) {
				BlockState nextstate=getBlockState().setValue(BlockStateProperties.LIT, newstate);
				this.level.setBlockAndUpdate(worldPosition, nextstate);
			}
		}
	}

	public ItemStacksResourceHandler getInternInv() {
		return internInv;
	}
}
