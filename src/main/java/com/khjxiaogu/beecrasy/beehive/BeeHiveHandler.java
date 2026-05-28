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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;

import com.khjxiaogu.beecrasy.BeecrasyConfig;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.components.LarvaProductivity;
import com.khjxiaogu.beecrasy.genome.DiploidGenome;
import com.khjxiaogu.beecrasy.genome.GeneSimilarityHelper;
import com.khjxiaogu.beecrasy.genome.Genes;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.GenomeDataHelper;
import com.khjxiaogu.beecrasy.genome.MutationRegistry;
import com.khjxiaogu.beecrasy.genome.ProductHelper;
import com.khjxiaogu.beecrasy.genome.ProductHelper.ProductWithCount;
import com.khjxiaogu.beecrasy.genome.RecombinationHelper;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;
import com.khjxiaogu.beecrasy.utils.BeecrasyMath;
import com.khjxiaogu.beecrasy.utils.SerializableRandomSource;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class BeeHiveHandler implements ValueIOSerializable,ContainerData{
	private static record HiveSlotPriority(HiveSlot slot,long priority){}
	public static record DataRecord(Optional<SerializableRandomSource> random,Optional<Genome[]> queenGenome,
		Optional<List<Genome>> droneGenomes,int larvaCount,int droneCount,int process,int processMax) {
		public static final Codec<DataRecord> CODEC=RecordCodecBuilder.create(t->t
			.group(Codec.LONG.xmap(SerializableRandomSource::new, SerializableRandomSource::getSeed).optionalFieldOf("seed").forGetter(o->o.random()),
				Genome.LIST_CODEC.xmap(o->o.toArray(Genome[]::new),Arrays::asList).optionalFieldOf("queen").forGetter(DataRecord::queenGenome),
				Genome.LIST_CODEC.optionalFieldOf("drones").forGetter(DataRecord::droneGenomes),
				Codec.INT.fieldOf("larva").forGetter(DataRecord::larvaCount),
				Codec.INT.fieldOf("drone").forGetter(DataRecord::droneCount),
				Codec.INT.fieldOf("process").forGetter(DataRecord::process),
				Codec.INT.fieldOf("processMax").forGetter(DataRecord::processMax)
				)
			.apply(t, DataRecord::new));
	}
	List<? extends HiveSlot> combSlot;
	List<? extends HiveSlot> droneSlot;
	List<? extends HiveSlot> queenSlot;
	
	SerializableRandomSource rs;
	Genome[] queenGenome;
	List<Genome> droneGenomes;
	int larvaCount;
	int droneCount;
	int process;
	int processMax;
	boolean blocked=false;
	boolean noBiotope=false;
	transient int interval;
	transient float lsAgainstInterval;
	public BeeHiveHandler(List<? extends HiveSlot> queenSlot, List<? extends HiveSlot> droneSlot, List<? extends HiveSlot> combSlot) {
		super();
		this.queenSlot = queenSlot;
		this.droneSlot = droneSlot;
		this.combSlot = combSlot;
		this.interval=BeecrasyConfig.SERVER.INTERVAL.getAsInt();
		this.lsAgainstInterval=BeecrasyConfig.SERVER.LIFESPAN.getAsInt()*.5f/BeecrasyConfig.SERVER.INTERVAL.getAsInt();
	}
	public void reset() {
		larvaCount=0;
		droneCount=0;
		process=processMax=0;
		rs=null;
		droneGenomes=null;
		setQueen(null);
	}
	public boolean isWorking() {
		return processMax>0;
	}
	public void tick(BeeHiveParameterSet params) {
		blocked=false;
		if(process>0) {
			process--;
			if(process%interval==0) {
				boolean isPre=process>=(processMax/2);
				if(isPre) {
					if(!fillLarva(params))
						fillDrone(params);
				}else {
					if(process%(interval*6)==0)
						elecQueen();
					increaseProduction(params);
				}
			}
		}else if(processMax>0){
			
			if(finishProduct(params)) {
				processMax=process=0;
				reset();
			}else {
				blocked=true;
			}
		}
	}
	private void increaseProduction(BeeHiveParameterSet params) {
		noBiotope=false;
		for(HiveSlot hi:combSlot) {
			if(!hi.isEmpty()) {
				ItemStack item=hi.getItem();
				if(!item.is(Items.LARVA))
					continue;
				
				Genome pheno=GenomeDataHelper.getPhenoType(item);
				LarvaProductivity lp=item.get(Components.LARVA_PRODUCT);
				if(lp==null)
					lp=LarvaProductivity.DEFAULT;

				float yield=pheno.getAllele(Genes.YIELD).getNumber()/lsAgainstInterval;
				if(params.hasBiotope(pheno.getAllele(Genes.BIOTOPE))) {
					lp=lp.increaseBiotoped(yield);
				}else {
					lp=lp.increaseWildcard(yield);
					noBiotope=true;
				}
				item.set(Components.LARVA_PRODUCT, lp);
				hi.setItem(item);
			}
		}
	}
	public boolean isNoBiotope() {
		return noBiotope;
	}
	public boolean isBlocked() {
		return blocked;
	}
	public void setQueen(Genome[] queenGenome) {
		if(queenGenome!=null) {
			if(queenGenome.length==0) {
				queenGenome=new Genome[] {Genome.DEFAULT,Genome.DEFAULT};
			}else if(queenGenome.length==1) {
				queenGenome=new Genome[] {queenGenome[0],queenGenome[1]};
			}
		}
		this.queenGenome=queenGenome;
	}
	@SuppressWarnings("deprecation")
	public void beginWork(BeeHiveParameterSet params,	Genome[] queenGenome,List<Genome> droneGenomes) {
		rs=SerializableRandomSource.create(Mth.getSeed(params.position())^params.level().getRandom().nextLong());
		setQueen(queenGenome);
		this.droneGenomes=droneGenomes;
		larvaCount=BeecrasyMath.getRandomRate(queenGenome[0].getAllele(Genes.FERTILITY).getNumber(), rs);
		droneCount=BeecrasyMath.getRandomRate(queenGenome[0].getAllele(Genes.FERTILITY).getNumber(), rs);
		processMax=process=GenomeDataHelper.getLifespanTicks(queenGenome[0]);
	}
	private boolean fillLarva(BeeHiveParameterSet params) {
		if(larvaCount<=0)
			return false;
		for(HiveSlot hi:combSlot) {
			if(hi.isEmpty()) {
				ItemStack larvaStack=Items.LARVA.toStack();
				GenomeDataHelper.setDiploidGenome(larvaStack, makeLarva(params));
				hi.setItem(larvaStack);
				larvaCount--;
				return true;
			}
		}
		return false;
	}
	private boolean finishProduct(BeeHiveParameterSet params) {
		boolean hasQueen=false;
		for(HiveSlot hi:queenSlot) {
			if(hi.isEmpty())
				continue;
			ItemStack item=hi.getItem();
			if(item.is(Items.LARVA)) {
				ItemStack queenStack=item.transmuteCopy(Items.QUEEN_BEE);
				queenStack.remove(Components.LARVA_PRODUCT);
				hi.setItem(queenStack);
				hasQueen=true;
			}
		}
		if(!hasQueen)
			return false;
		for(HiveSlot hi:combSlot) {
			if(!hi.isEmpty()) {
				ItemStack item=hi.getItem();
				if(!item.is(Items.LARVA))
					continue;
				hi.setItem(ItemStack.EMPTY);
				Genome pheno=GenomeDataHelper.getPhenoType(item);
				
				List<ProductItem> products=pheno.getAllele(Genes.PRODUCTS);
				LarvaProductivity lp=item.get(Components.LARVA_PRODUCT);
				if(lp!=null) {
					if(lp.biotopeProductive()<lp.wildcardProductive()) {
						hi.setItem(Items.PRODUCT_COMB.toStack(BeecrasyMath.getRandomRate(lp.wildcardProductive(), rs)));
					}else {
						ProductWithCount product=ProductHelper.pickSingleProduct(pheno.getAllele(Genes.BIOTOPE), products, rs, BeecrasyMath.getRandomRate(lp.biotopeProductive(), rs));
						hi.setItem(product.createProductComb());
					}
				}
			}
		}
		return true;
	}
	public void elecQueen() {
		boolean hasEmptyQueen=false;
		for(HiveSlot hi:queenSlot) {
			if(hi.isEmpty())
				hasEmptyQueen=true;
		}
		if(!hasEmptyQueen)
			return;
		PriorityQueue<HiveSlotPriority> queens=new PriorityQueue<>(Comparator.comparingLong(HiveSlotPriority::priority).reversed());
		for(int i=0;i<combSlot.size();i++) {
			HiveSlot hi=combSlot.get(i);
			if(!hi.isEmpty()) {
				queens.add(new HiveSlotPriority(hi,GeneSimilarityHelper.checkSimilarityPoint(queenGenome, GenomeDataHelper.getAsDiploid(hi.getItem()))));
			}
		}
		for(HiveSlot hi:queenSlot) {
			if(queens.isEmpty())
				break;
			if(hi.isEmpty()) {
				HiveSlotPriority queen=queens.poll();
				ItemStack queenStack=queen.slot().getItem().transmuteCopy(Items.QUEEN_BEE);
				hi.setItem(queenStack);
				queen.slot().setItem(ItemStack.EMPTY);
			}
		}
	}
	private boolean fillDrone(BeeHiveParameterSet params) {
		if(droneCount<=0)
			return false;
		for(HiveSlot hi:droneSlot) {
			if(hi.isEmpty()) {
				ItemStack droneStack=Items.DRONE.toStack();
				GenomeDataHelper.setHaploidGenome(droneStack, makeDrone(params).build());
				hi.setItem(droneStack);
				droneCount--;
				return true;
			}
		}
		return false;
	}
	private DiploidGenome makeLarva(BeeHiveParameterSet params) {
		DiploidGenome ret=RecombinationHelper.makeDiploid(queenGenome, getRandomDrone(rs), rs::nextBoolean);
		MutationRegistry.handleMutation(params, ret, rs);
		
		return ret;
	}
	private Genome.Builder makeDrone(BeeHiveParameterSet params) {
		Genome.Builder ret=RecombinationHelper.makeHaploid(queenGenome, rs::nextBoolean);
		return ret;
	}
	private Genome getRandomDrone(RandomSource rs) {
		return droneGenomes.get(rs.nextInt(droneGenomes.size()));
	}
	@Override
	public void deserialize(ValueInput nbt) {
		rs=nbt.getLong("seed").map(SerializableRandomSource::new).orElse(null);
		setQueen(nbt.read("queen", Genome.LIST_CODEC).map(t->t.toArray(Genome[]::new)).orElse(null));
		droneGenomes=nbt.read("drones", Genome.LIST_CODEC).orElse(null);
		larvaCount=nbt.getIntOr("larva", 0);
		droneCount=nbt.getIntOr("drone", 0);
		process=nbt.getIntOr("process", 0);
		processMax=nbt.getIntOr("processMax", 0);
	}
	@Override
	public void serialize(ValueOutput nbt) {
		if(rs!=null)
			nbt.putLong("seed", rs.getSeed());
		if(queenGenome!=null)
			nbt.store("queen", Genome.LIST_CODEC, Arrays.asList(queenGenome));
		if(droneGenomes!=null)
			nbt.store("drones", Genome.LIST_CODEC,droneGenomes);
		nbt.putInt("larvaCount", larvaCount);
		nbt.putInt("droneCount", droneCount);
		nbt.putInt("process", process);
		nbt.putInt("processMax", processMax);
	}
	public void read(DataRecord data) {
		rs=data.random().orElse(null);
		setQueen(data.queenGenome().orElse(null));
		droneGenomes=data.droneGenomes().orElse(null);
		larvaCount=data.larvaCount();
		droneCount=data.droneCount();
		process=data.process();
		processMax=data.processMax();
	}

	public DataRecord save() {
		return new DataRecord(Optional.ofNullable(rs),Optional.ofNullable(queenGenome),Optional.ofNullable(droneGenomes),larvaCount,droneCount,process,processMax);
	}
	@Override
	public int get(int index) {
		Objects.checkIndex(index, 2);
		switch(index) {
		case 0:return process;
		case 1:return processMax;
		}
		return -1;
	}
	@Override
	public void set(int index, int value) {
		Objects.checkIndex(index, 2);
		switch(index) {
		case 0:process=value;return;
		case 1:processMax=value;return;
		}
	}
	@Override
	public int getCount() {
		return 2;
	}
	public int getProcess() {
		return process;
	}
	public int getProcessMax() {
		return processMax;
	}
}
