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

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.beehive.slot.ResourceStackHiveSlot;
import com.khjxiaogu.beecrasy.beehive.slot.StacksHiveSlot;
import com.khjxiaogu.beecrasy.components.BeeHiveArgumentation;
import com.khjxiaogu.beecrasy.components.BeehiveArgumenter;
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.GenomeWorkHelper;
import com.khjxiaogu.beecrasy.utils.ItemValidateHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class BeeHiveBaseComponent implements ValueIOSerializable{
	
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
		protected final List<StacksHiveSlot> combSlot;
		protected final List<StacksHiveSlot> droneSlot;
		protected final BeeHiveHandler.DataRecord hiveInfo;
		protected final Optional<BeeHiveArgumentation> arguments;
		protected final int beginingTicks;
		protected final WorkBehaviour work;
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

	protected ItemStacksResourceHandler internInv;
	protected DelegatingResourceHandler<ItemResource> externInv;
	protected DelegatingResourceHandler<ItemResource> productInv;
	protected List<ResourceStackHiveSlot> queenSlot;
	protected List<ResourceStackHiveSlot> combSlot;
	protected List<ResourceStackHiveSlot> droneSlot;
	protected List<ResourceStackHiveSlot> extraSlot;
	public final BeeHiveHandler hiveInfo;
	protected BeeHiveArgumentation arguments;
	protected boolean shouldWork;
	protected boolean changed;
	protected int beginingTicks=0;
	public WorkBehaviour work=WorkBehaviour.MAUNAL;
	public ErrCode err=ErrCode.OK;
	public static final int COOLDOWN_TIME=100;
	public BeeHiveBaseComponent(BeeHiveBaseData data) {
		this(data.queenSlot.size(),data.droneSlot.size(),data.combSlot.size(),0);
		load(data);
	}
	public BeeHiveBaseComponent(int queen,int drone,int comb,int extra) {
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
		externInv=new DelegatingResourceHandler<>(internInv) {

			@Override
			public boolean isValid(int index, ItemResource resource) {
				if(index<queen)
					return ItemValidateHelper.isQueen(resource.toStack());
				if(index<queen+drone)
					return ItemValidateHelper.isDrone(resource.toStack());
				if(index<queen+drone+comb)
					return ItemValidateHelper.isComb(resource.toStack());
				return isValidForExtra(index-(queen+drone+comb),resource);
			}
			
		};
		productInv=new RangedResourceHandler<>(internInv,queen+drone,queen+drone+comb) {

			@Override
			public ItemResource getResource(int index) {
				ItemResource ir= super.getResource(index);
				if(!ItemValidateHelper.isComb(ir.toStack()))
					return ir;
				return ItemResource.EMPTY;
			}

			@Override
			public long getAmountAsLong(int index) {
				if(getResource(index).isEmpty())
					return 0;
				return super.getAmountAsLong(index);
			}

			@Override
			public boolean isValid(int index, ItemResource resource) {
				return false;
			}

			@Override
			public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
				return 0;
			}

			@Override
			public int insert(ItemResource resource, int amount, TransactionContext transaction) {
				return 0;
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
		extraSlot=new ArrayList<>(extra);
		for(int i=0;i<extra;i++) {
			extraSlot.add(new ResourceStackHiveSlot(getInternInv(),i+queen+drone+comb));
		}
		hiveInfo= new BeeHiveHandler(queenSlot,droneSlot,combSlot);
	}
	protected boolean isValidForExtra(int index, ItemResource resource) {
		return false;
	}
	@Override
	public void serialize(ValueOutput output) {
		writeCustomNBT(output,false);
	}
	@Override
	public void deserialize(ValueInput input) {
		readCustomNBT(input, false);
	}
	public void serializeClient(ValueOutput output) {
		writeCustomNBT(output,true);
	}
	public void deserializeClient(ValueInput input) {
		readCustomNBT(input, true);
	}
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
	public void load(BeeHiveBaseData data) {
		HiveSlot.copy(data.queenSlot, queenSlot);
		HiveSlot.copy(data.droneSlot, droneSlot);
		HiveSlot.copy(data.combSlot, combSlot);
		hiveInfo.read(data.hiveInfo);
		arguments=data.arguments.orElse(null);
		work=data.work;
	}
	public BeeHiveBaseData save() {
		return new BeeHiveBaseData(this);
	}
	public BeeHiveParameterSet.Builder buildParams(ServerLevel serverLevel, BlockPos worldPosition){
		BeeHiveParameterSet.Builder builder= new BeeHiveParameterSet.Builder(serverLevel,worldPosition);
		if(arguments!=null)
			builder.setParams(arguments.params());
		return builder;
	}
	protected BeeHiveArgumentation.Builder buildArgumentation(ServerLevel serverLevel, BlockPos worldPosition,TransactionContext root) {
		return new BeeHiveArgumentation.Builder();
	}
	protected BeeHiveArgumentation extractArgumentation(ServerLevel serverLevel,int slot,TransactionContext root) {
		ItemResource ir=internInv.getResource(slot);
		@Nullable BeehiveArgumenter argu=ir.get(Components.ARGUMENTATION);
		if(argu!=null) {
			if(argu.consumeOnUse()) {
				try(Transaction trans=Transaction.open(root)){
					int extracted=internInv.extract(slot, ir, 1, trans);
					if(extracted==1) {
						if(ir.has(DataComponents.DAMAGE)&&ir.has(DataComponents.MAX_DAMAGE)) {
							if(ir.has(DataComponents.UNBREAKABLE)) {
								return argu.modifiers();
							}
							ItemStack stack=ir.toStack();
							stack.hurtAndBreak(1, serverLevel, null,_->{});
							if(!stack.isEmpty()) {
								if(internInv.insert(slot, ItemResource.of(stack), extracted, trans)==extracted) {
									trans.commit();
									return argu.modifiers();
								}
							}
						}else {
							trans.commit();
							return argu.modifiers();
						}
					}
				}
				return null;
			}else {
				return argu.modifiers();
			}
		}
		return null;
	}
	protected boolean canBeginWork() {

		if(work==WorkBehaviour.MAUNAL&&!shouldWork) {
			err=ErrCode.MANUAL_HALT;
			return false;
		}
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
		err=ErrCode.OK;
		return true;
	}
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

		hiveInfo.beginWork(params, queen, drones);
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
	public void tick(ServerLevel serverLevel,BlockPos worldPosition,int speed,boolean hasRedstone) {
		switch(work) {
		case AUTO:shouldWork=true;break;
		case REDSTONE:shouldWork=hasRedstone;break;
		default:
			break;
		}
		if(beginingTicks>0) {
			beginingTicks+=speed;
			if(beginingTicks>=COOLDOWN_TIME) {
				beginingTicks=0;
				if(canBeginWork()) {
					shouldWork=false;
					if(beginGrowth(serverLevel, worldPosition)) {
						this.setChanged();
					}
				}
			}
		}else if(hiveInfo.isWorking()) {
			err=ErrCode.OK;
			BeeHiveParameterSet params=buildParams(serverLevel, worldPosition).build();
			hiveInfo.tick(params,speed);
			this.setChanged();
			if(hiveInfo.isBadEnvironment())
				err=ErrCode.INVALID_ENVIRONMENT;
			else if(hiveInfo.isNoFlower()) 
				err=ErrCode.NO_FLOWER;
			else if(hiveInfo.isBlocked())
				err=ErrCode.EMPTY_QUEEN;
			else if(hiveInfo.isNoBiotope())
				err=ErrCode.NO_BIOTOPE;
		}else if(shouldWork) {
			if(canBeginWork()) {
				beginingTicks=1;
			}
		}else if(err==ErrCode.OK) {
			err=ErrCode.MANUAL_HALT;
		}
	}
	public void setChanged() {
		this.changed=true;
	}
	
	public ItemStacksResourceHandler getInternInv() {
		return internInv;
	}
	public DelegatingResourceHandler<ItemResource> getExternInv() {
		return externInv;
	}
	public DelegatingResourceHandler<ItemResource> getProductInv() {
		return productInv;
	}
	public boolean isChanged() {
		return changed;
	}
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

}
