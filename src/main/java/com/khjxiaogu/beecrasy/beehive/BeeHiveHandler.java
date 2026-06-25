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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.khjxiaogu.beecrasy.BeecrasyConfig;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterSet.BeehiveSlotProvider;
import com.khjxiaogu.beecrasy.components.LarvaProductivity;
import com.khjxiaogu.beecrasy.components.WorldCalendar;
import com.khjxiaogu.beecrasy.genome.DiploidGenome;
import com.khjxiaogu.beecrasy.genome.Genes;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.GenomeDataHelper;
import com.khjxiaogu.beecrasy.genome.GenomeWorkHelper;
import com.khjxiaogu.beecrasy.genome.MutationRegistry;
import com.khjxiaogu.beecrasy.genome.RecombinationHelper;
import com.khjxiaogu.beecrasy.item.LarvaItem;
import com.khjxiaogu.beecrasy.utils.BeecrasyMath;
import com.khjxiaogu.beecrasy.utils.SerializableRandomSource;
import com.khjxiaogu.beecrasy.utils.Utils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/**
 * 蜂巢生命周期处理器。
 * 核心类，管理蜂巢工作周期的完整流程，包括：
 * <ul>
 *   <li>蜂后产卵（生成幼虫和雄蜂）</li>
 *   <li>幼虫生长和蜂后羽化</li>
 *   <li>雄蜂填充</li>
 *   <li>杂交突变处理</li>
 *   <li>产物生成（蜂蜜等）</li>
 *   <li>环境检查和生境更新</li>
 * </ul>
 * 实现了 {@link ValueIOSerializable} 用于数据持久化，以及 {@link ContainerData} 用于容器同步。
 */
public class BeeHiveHandler implements ValueIOSerializable,ContainerData{
	public static class ValidSlotIterator<T extends HiveSlot> implements Iterator<T> {
		/** 底层迭代器 */
		private final Iterator<T> iterator;
		/** 缓存的下一个符合条件的元素 */
		private T nextItem;

		/**
		 * 使用底层迭代器构造可操作条目迭代器。
		 *
		 * @param iterator 底层迭代器
		 */
		public ValidSlotIterator(Iterator<T> iterator) {
			this.iterator = iterator;
			advance();
		}
		/**
		 * 预加载下一个符合条件的元素。
		 */
		private void advance() {
			while (iterator.hasNext()) {
				T item = iterator.next();
				if (item.isValid()) {
					nextItem = item;
					return;
				}
			}
			nextItem = null;
		}
		@Override
		public boolean hasNext() {
			return nextItem != null;
		}
		@Override
		public T next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			T result = nextItem;
			advance();
			return result;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	public static class HiveSlotIterable<T extends HiveSlot> implements Iterable<T>{
		private final Iterable<T> backed;
		public HiveSlotIterable(Iterable<T> backed) {
			super();
			this.backed = backed;
		}
		@Override
		public Iterator<T> iterator() {
			return new ValidSlotIterator<>(backed.iterator());
		}

	}
	/**
	 * 数据快照记录，用于网络同步和持久化。
	 * 包含了蜂巢工作状态的所有必要数据：随机数种子、蜂后基因组、雄蜂基因组列表、
	 * 幼虫计数、雄蜂计数、当前进度和最大进度。
	 */
	public static record DataRecord(Optional<SerializableRandomSource> random,Optional<Genome[]> queenGenome,
		Optional<List<Genome>> droneGenomes,int queenCount,int larvaCount,int droneCount,int process,int processMax) {
		/** 空数据记录（所有字段均为默认值/空 Optional）。 */
		public static final DataRecord EMPTY=new DataRecord(Optional.empty(),Optional.empty(),Optional.empty(),0,0,0,0,0);
		public static final Codec<DataRecord> CODEC=RecordCodecBuilder.create(t->t
			.group(Codec.LONG.xmap(SerializableRandomSource::new, SerializableRandomSource::getSeed).optionalFieldOf("seed").forGetter(o->o.random()),
				Genome.LIST_CODEC.xmap(o->o.toArray(Genome[]::new),Arrays::asList).optionalFieldOf("queen").forGetter(DataRecord::queenGenome),
				Genome.LIST_CODEC.optionalFieldOf("drones").forGetter(DataRecord::droneGenomes),
				Codec.INT.fieldOf("queen").forGetter(DataRecord::queenCount),
				Codec.INT.fieldOf("larva").forGetter(DataRecord::larvaCount),
				Codec.INT.fieldOf("drone").forGetter(DataRecord::droneCount),
				Codec.INT.fieldOf("process").forGetter(DataRecord::process),
				Codec.INT.fieldOf("processMax").forGetter(DataRecord::processMax)
				)
			.apply(t, DataRecord::new));
		public static final StreamCodec<RegistryFriendlyByteBuf,DataRecord> STREAM_CODEC=StreamCodec.composite(
				ByteBufCodecs.optional(ByteBufCodecs.LONG.map(SerializableRandomSource::new, SerializableRandomSource::getSeed)),o->o.random(),
				ByteBufCodecs.optional(Utils.asArray(Genome.STREAM_CODEC.apply(ByteBufCodecs.list()),Genome[]::new)),DataRecord::queenGenome,
				ByteBufCodecs.optional(Genome.STREAM_CODEC.apply(ByteBufCodecs.list())),DataRecord::droneGenomes,
				ByteBufCodecs.VAR_INT,DataRecord::queenCount,
				ByteBufCodecs.VAR_INT,DataRecord::larvaCount,
				ByteBufCodecs.VAR_INT,DataRecord::droneCount,
				ByteBufCodecs.INT,DataRecord::process,
				ByteBufCodecs.INT,DataRecord::processMax,
				DataRecord::new
				);
	}

	
	/** 可序列化的随机数源，用于保证工作周期中所有随机操作的一致性。 */
	SerializableRandomSource rs;
	/** 蜂后的二倍体基因组（[0]=母本, [1]=父本）。 */
	Genome[] queenGenome;
	/** 雄蜂的单倍体基因组列表。 */
	List<Genome> droneGenomes;

	/** 待生成的幼虫数量。 */
	int larvaCount;
	/** 待生成的雄蜂数量。 */
	int droneCount;
	/** 待生成的蜂后/蜂王浆数量。 */
	int queenCount;
	/** 当前工作进度。 */
	int process;
	/** 最大工作进度（即蜂后总寿命对应的 tick 数）。 */
	int processMax;
	/** 是否因槽位满而阻塞（瞬态，不同步）。 */
	transient boolean blocked=false;
	/** 是否缺少匹配的生境（瞬态，不同步）。 */
	transient boolean noBiotope=false;
	/** 附近是否没有花（瞬态，不同步）。 */
	transient boolean noFlower=false;
	/** 当前环境是否不适宜（瞬态，不同步）。 */
	transient boolean badEnvironment=false;
	/** 产物生成的间隔 tick 数（来自配置）。 */
	transient int interval;
	/** 寿命对间隔的比值系数（用于计算产量）。 */
	transient float lsAgainstInterval;
	/** 冷却计时器，用于重试阻塞操作。 */
	transient int cooldown;
	
	/**
	 * 创建一个蜂巢处理器，绑定到指定的槽位列表。
	 * 从游戏配置中读取工作间隔和寿命相关参数。
	 * @param queenSlot 蜂后槽位列表
	 * @param droneSlot 雄蜂槽位列表
	 * @param combSlot  巢脾/产物槽位列表
	 */
	public BeeHiveHandler() {
		super();
		/*this.queenSlot = new HiveSlotIterable<>(queenSlot);
		this.droneSlot = new HiveSlotIterable<>(droneSlot);
		this.combSlot = new HiveSlotIterable<>(combSlot);*/
		this.interval=BeecrasyConfig.SERVER.INTERVAL.getAsInt();
		this.lsAgainstInterval=BeecrasyConfig.SERVER.LIFESPAN.getAsInt()*.5f/BeecrasyConfig.SERVER.INTERVAL.getAsInt();
	}

	/**
	 * 重置蜂巢的所有工作状态。
	 * 清空幼虫/雄蜂计数、工作进度和随机源，移除蜂后基因组。
	 */
	public void reset() {
		larvaCount=0;
		droneCount=0;
		process=processMax=0;
		cooldown=0;
		rs=null;
		droneGenomes=null;
		setQueen(null);
	}
	/**
	 * 检查蜂巢当前是否正在工作中。
	 * @return 如果最大进度大于 0 则返回 true
	 */
	public boolean isWorking() {
		return processMax>0;
	}
	/**
	 * 更新所有蜂后槽位中幼虫的过期时间。
	 * @param secs 世界当前时间（秒）
	 */
	public void updateQueenLifespan(BeeHiveParameterSet params,long secs) {
		for(HiveSlot slot:params.slots().queenSlot()) {
			updateLifespan(slot, secs);
		}
	}
	private static void updateLifespan(HiveSlot slot,long secs) {
		ItemStack stack=slot.getItem();
		if(stack.is(Items.LARVA)&&stack.has(Components.LARVA_EXPIRES)) {
			stack.set(Components.LARVA_EXPIRES,secs);
			slot.setItem(stack);
		}
	}
	/**
	 * 更新所有巢脾槽位中幼虫的过期时间。
	 * @param secs 世界当前时间（秒）
	 */
	public void updateCombLifespan(BeeHiveParameterSet params,long secs) {
		for(HiveSlot slot:params.slots().combSlot()) {
			updateLifespan(slot, secs);
		}
	}
	public static void checkCombLifespan(HiveSlot slot,long secs,RandomSource rnd) {
		ItemStack stack=slot.getItem();
		if(stack.is(Items.LARVA)) {
			if(LarvaItem.isExpired(stack, secs)) {
				slot.setItem(LarvaItem.getProduct(stack, stack.count(), rnd));
			}
		}
	}
	@SuppressWarnings("resource")
	public void tickNotWorking(ServerLevel level, BlockPos worldPosition, BeehiveSlotProvider slots) {
		long secs=WorldCalendar.getCalendar(level).getSeconds();
		for(HiveSlot slot:slots.combSlot()) {
			checkCombLifespan(slot, secs, level.getRandom());
		}
		for(HiveSlot slot:slots.queenSlot()) {
			checkCombLifespan(slot, secs, level.getRandom());
		}
	}
	/**
	 * 主 tick 方法，驱动蜂巢的工作周期。
	 * 根据当前进度执行以下逻辑：
	 * <ul>
	 *   <li>如果 {@code process > 0}：持续进行当前工作，按速度减少进度，在整间隔点触发产卵/产物生成</li>
	 *   <li>如果 {@code process <= 0} 且 {@code processMax > 0}：工作结束，尝试产出最终产物</li>
	 * </ul>
	 * @param params 当前环境参数集
	 * @param speed  工作速度倍率
	 */
	@SuppressWarnings("resource")
	public int tickFertilityOnly(BeeHiveParameterSet params,int speed) {
		blocked=false;
		badEnvironment=false;
		noFlower=false;
		long secs=WorldCalendar.getCalendar(params.level()).getSeconds();
		if(process>0) {

			if(!GenomeWorkHelper.isValidEnvironment(params, queenGenome[0])) {
				badEnvironment=true;
			}
			if(!badEnvironment&&!noFlower) {
				int lprocess=process;
				process-=BeecrasyMath.getRandomRate(params.getParamValue(BeeHiveParameters.SPEED)*speed, rs);
				if(process<0)
					process=0;
				int ltick=lprocess/interval;
				int ctick=process/interval;
				if(ltick>ctick) {
					for(int i=ctick;i<ltick;i++) {
						if(!fillLarva(params,secs))
							fillDrone(params);
					}
					return ltick-ctick;
				}
			}
		}
		return 0;
	}
	/**
	 * 主 tick 方法，驱动蜂巢的工作周期。
	 * 根据当前进度执行以下逻辑：
	 * <ul>
	 *   <li>如果 {@code process > 0}：持续进行当前工作，按速度减少进度，在整间隔点触发产卵/产物生成</li>
	 *   <li>如果 {@code process <= 0} 且 {@code processMax > 0}：工作结束，尝试产出最终产物</li>
	 * </ul>
	 * @param params 当前环境参数集
	 * @param speed  工作速度倍率
	 */
	@SuppressWarnings("resource")
	public int tick(BeeHiveParameterSet params,int speed) {
		blocked=false;
		badEnvironment=false;
		noFlower=false;
		long secs=WorldCalendar.getCalendar(params.level()).getSeconds();
		if(process>0) {
			if(!GenomeWorkHelper.isValidEnvironment(params, queenGenome[0])) {
				badEnvironment=true;
			}
			if(!badEnvironment&&!noFlower) {
				
				int lprocess=process;
				process-=BeecrasyMath.getRandomRate(params.getParamValue(BeeHiveParameters.SPEED)*speed, rs);
				if(process<0)
					process=0;
				int ltick=lprocess/interval;
				int ctick=process/interval;
				if(ltick>ctick) {
					for(int i=ctick;i<ltick;i++) {
						if(fillLarva(params,secs)||fillDrone(params)) {
							updateCombLifespan(params,secs);
							updateQueenLifespan(params,secs);
						}else {
							increaseProduction(params);
						}
					}

					return ltick-ctick;
				}
			}
		}else {
			if(processMax>=0){
				if(cooldown<=0) {
					cooldown=0;
					if(finishProduct(params)) {
						processMax=process=0;
						reset();
					}else {
						blocked=true;
						cooldown=20;
					}
				}else {
					cooldown--;
				}
			}
			for(HiveSlot slot:params.slots().combSlot()) {
				ItemStack itemStack=slot.getItem();
				if(LarvaItem.isExpired(itemStack,secs)) {
					ItemStack ret=LarvaItem.getProduct(itemStack, itemStack.count(), params.level().getRandom());
					slot.setItem(ret);
				}
			}
		}
		return 0;
	}
	private void increaseProduction(BeeHiveParameterSet params,HiveSlot hi,long secs,float yieldMod) {
		if(!hi.isEmpty()) {
			ItemStack item=hi.getItem();
			if(!item.is(Items.LARVA))
				return;
			if(item.has(Components.LARVA_EXPIRES))
				item.set(Components.LARVA_EXPIRES,secs);
			Genome pheno=GenomeDataHelper.getPhenoType(item);
			LarvaProductivity lp=item.get(Components.LARVA_PRODUCT);
			if(lp==null)
				lp=LarvaProductivity.DEFAULT;

			float yield=pheno.getAllele(Genes.YIELD).getNumber()/lsAgainstInterval*yieldMod;
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
	/**
	 * 增加巢脾槽位中幼虫的产物积累量。
	 * 根据产量参数、生境匹配情况和幼虫的基因表型计算每次增加的产量。
	 * @param params   当前环境参数
	 * @param biotopes 当前环境中的生境集合（可能为 null）
	 */
	@SuppressWarnings("resource")
	private void increaseProduction(BeeHiveParameterSet params) {
		noBiotope=false;

		long secs=WorldCalendar.getCalendar(params.level()).getSeconds();
		

		updateQueenLifespan(params,secs);
		float yieldMod=params.getParamValue(BeeHiveParameters.YIELD);
		Set<HiveSlot> computed=new ReferenceOpenHashSet<>();
		for(HiveSlot hi:params.slots().combSlot()) {
			if(computed.add(hi))
				increaseProduction(params,hi,secs,yieldMod);
		}
		for(HiveSlot hi:params.slots().queenSlot()) {
			if(computed.add(hi))
				increaseProduction(params,hi,secs,yieldMod);
		}
		for(HiveSlot hi:params.slots().droneSlot()) {
			if(computed.add(hi))
				increaseProduction(params,hi,secs,yieldMod);
		}
	}
	/**
	 * 检查是否有生境不匹配的情况。
	 * @return 如果存在生境不匹配则返回 true
	 */
	public boolean isNoBiotope() {
		return noBiotope;
	}
	/**
	 * 检查蜂巢是否因槽位满而阻塞。
	 * @return 如果阻塞则返回 true
	 */
	public boolean isBlocked() {
		return blocked;
	}
	/**
	 * 设置蜂后基因组。
	 * 如果传入的数组为空或只有一个基因组，会进行填充以保证始终为二倍体。
	 * @param queenGenome 蜂后基因组数组（应为 [母本, 父本]）
	 */
	public void setQueen(Genome[] queenGenome) {
		if(queenGenome!=null) {
			if(queenGenome.length==0) {
				queenGenome=new Genome[] {Genome.DEFAULT,Genome.DEFAULT};
			}else if(queenGenome.length==1) {
				queenGenome=new Genome[] {queenGenome[0],queenGenome[0]};
			}
		}
		this.queenGenome=queenGenome;
	}
	/**
	 * 开始一个新的工作周期。
	 * 初始化随机源、设置蜂后和雄蜂基因组、计算幼虫/雄蜂数量和总工作进度。
	 * @param params       当前环境参数
	 * @param queenGenome  蜂后基因组（二倍体）
	 * @param droneGenomes 雄蜂基因组列表
	 */
	@SuppressWarnings({ "deprecation", "resource" })
	public void prepareWork(BeeHiveParameterSet params,Genome[] queenGenome,List<Genome> droneGenomes) {
		rs=SerializableRandomSource.create(Mth.getSeed(params.position())^params.level().getRandom().nextLong());
		setQueen(queenGenome);
		this.droneGenomes=droneGenomes;
	
		queenCount=Math.max(BeecrasyMath.getRandomRate((float) Math.log10(queenGenome[0].getAllele(Genes.FERTILITY).getNumber()
			*params.getParamValue(BeeHiveParameters.FERTILITY)
			*queenGenome[0].getAllele(Genes.YIELD).getNumber()
			*params.getParamValue(BeeHiveParameters.YIELD)
			*Math.log(droneGenomes.size())
			*2), rs), 1);
		larvaCount=BeecrasyMath.getRandomRate(queenGenome[0].getAllele(Genes.FERTILITY).getNumber()*params.getParamValue(BeeHiveParameters.FERTILITY), rs);
		droneCount=BeecrasyMath.getRandomRate(queenGenome[0].getAllele(Genes.FERTILITY).getNumber()*params.getParamValue(BeeHiveParameters.FERTILITY), rs);
		processMax=process=(int) (GenomeDataHelper.getLifespanTicks(queenGenome[0])*params.getParamValue(BeeHiveParameters.LIFESPAN));
		
	}
	/**
	 * 尝试将一只新的幼虫填充到巢脾槽位中。
	 * 如果存在空槽位，则创建一个幼虫物品栈并赋予经过突变处理的二倍体基因组。
	 * @param params 当前环境参数
	 * @return 如果成功填充则返回 true
	 */
	private boolean fillLarva(BeeHiveParameterSet params,long secs) {
		if(larvaCount<=0)
			return false;
		HiveSlot fillable=null;
		for(HiveSlot hi:params.slots().queenSlot()) {
			if(hi.isEmpty()) {
				fillable=hi;
				break;
			}
		}
		if(fillable==null)
			for(HiveSlot hi:params.slots().combSlot()) {
				if(hi.isEmpty()) {
					fillable=hi;
					break;
				}
			}
		if(fillable!=null) {
			ItemStack larvaStack=Items.LARVA.toStack();
			GenomeDataHelper.setDiploidGenome(larvaStack, makeLarva(params));
			larvaStack.set(Components.LARVA_EXPIRES, secs);
			long curSimPoint=GenomeWorkHelper.checkSimilarityPoint(queenGenome, GenomeDataHelper.getAsDiploid(larvaStack));
			for(HiveSlot hi:params.slots().queenSlot()) {
				if(hi.isEmpty()) {
					hi.setItem(larvaStack);
					larvaStack=ItemStack.EMPTY;
				}else if(hi.is(Items.LARVA)){
					ItemStack cur=hi.getItem();
					long slotSimPoint=GenomeWorkHelper.checkSimilarityPoint(queenGenome, GenomeDataHelper.getAsDiploid(cur));
					if(curSimPoint>slotSimPoint) {
						curSimPoint=slotSimPoint;
						hi.setItem(larvaStack);
						larvaStack=cur;
					}
				}
				if(larvaStack.isEmpty())
					break;
			}
			if(!larvaStack.isEmpty())
				fillable.setItem(larvaStack);
			larvaCount--;
		}
		
		
		return true;
	}
	/**
	 * 完成产物生成，处理工作周期结束时的逻辑。
	 * 包括：幼虫羽化为蜂后、巢脾中的幼虫转换为产物、生成蜂王浆等。
	 * @param params 当前环境参数
	 * @return 如果产物处理完成则返回 true
	 */
	@SuppressWarnings("resource")
	private boolean finishProduct(BeeHiveParameterSet params) {
		boolean hasQueen=queenCount==0;
		long secs=WorldCalendar.getCalendar(params.level()).getSeconds();
		if(!hasQueen) {
			for(HiveSlot hi:params.slots().queenSlot()) {
				if(hi.isEmpty())
					continue;
				ItemStack item=hi.getItem();
				if(item.is(Items.LARVA)) {
					hi.setItem(LarvaItem.convertToQueen(item));
					hasQueen=true;
					queenCount--;
				}
				if(queenCount<=0)
					break;
			}
		}
		if(!hasQueen) {
			boolean hasLarva=false;
			for(HiveSlot hi:params.slots().combSlot()) {
				if(!hi.isEmpty()) {
					ItemStack stack=hi.getItem();
					if(stack.is(Items.LARVA)) {
						if(stack.has(Components.LARVA_EXPIRES)) {
							stack.set(Components.LARVA_EXPIRES,secs);
							hi.setItem(stack);
						}
						hasLarva=true;
					}
				}
			}
			if(hasLarva) {
				return false;
			}
		}
		if(queenCount>0) {
			for(HiveSlot hi:params.slots().queenSlot()) {
				if(!hi.isEmpty())
					continue;
				hi.setItem(Items.ROYAL_JELLY.toStack());
				queenCount--;
				if(queenCount<=0)
					break;
			}
		}
		queenCount=0;
		for(HiveSlot hi:params.slots().combSlot()) {
			if(!hi.isEmpty()) {
				ItemStack item=hi.getItem();
				if(!item.is(Items.LARVA))
					continue;
				ItemStack ret=LarvaItem.getProduct(item, item.count(), params.level().getRandom());
				hi.setItem(ret);
			}
		}
		for(HiveSlot hi:params.slots().queenSlot()) {
			if(!hi.isEmpty()) {
				ItemStack item=hi.getItem();
				if(!item.is(Items.LARVA))
					continue;
				ItemStack ret=LarvaItem.getProduct(item, item.count(), params.level().getRandom());
				hi.setItem(ret);
			}
		}
		return true;
	}
	/**
	 * 尝试将一只新的雄蜂填充到雄蜂槽位中。
	 * 如果存在空槽位，则创建一个雄蜂物品栈并赋予由蜂后基因组重组的单倍体基因组。
	 * @param params 当前环境参数
	 * @return 如果成功填充则返回 true
	 */
	private boolean fillDrone(BeeHiveParameterSet params) {
		if(droneCount<=0)
			return false;
		for(HiveSlot hi:params.slots().droneSlot()) {
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
	/**
	 * 生成一个经过突变处理的二倍体幼虫基因组。
	 * 通过蜂后基因组与随机雄蜂基因组的重组，再应用可能的突变。
	 * @param params 当前环境参数（用于突变判定）
	 * @return 二倍体幼虫基因组
	 */
	private DiploidGenome makeLarva(BeeHiveParameterSet params) {
		DiploidGenome ret=RecombinationHelper.makeDiploid(queenGenome, getRandomDrone(rs), rs::nextBoolean);
		MutationRegistry.handleMutation(params, ret, params.getParamValue(BeeHiveParameters.MUTATE), rs);
		
		return ret;
	}
	/**
	 * 根据蜂后基因组生成一个单倍体雄蜂基因组（无突变）。
	 * @param params 当前环境参数
	 * @return 雄蜂基因组构建器
	 */
	private Genome.Builder makeDrone(BeeHiveParameterSet params) {
		Genome.Builder ret=RecombinationHelper.makeHaploid(queenGenome, rs::nextBoolean);
		return ret;
	}
	/**
	 * 从雄蜂基因组列表中随机获取一个。
	 * @param rs 随机数源
	 * @return 随机挑选的雄蜂基因组
	 */
	private Genome getRandomDrone(RandomSource rs) {
		return droneGenomes.get(rs.nextInt(droneGenomes.size()));
	}
	/**
	 * 从 ValueInput 中反序列化恢复蜂巢工作状态。
	 * @param nbt 输入源
	 */
	@Override
	public void deserialize(ValueInput nbt) {
		rs=nbt.getLong("seed").map(SerializableRandomSource::new).orElse(null);
		setQueen(nbt.read("queen", Genome.LIST_CODEC).map(t->t.toArray(Genome[]::new)).orElse(null));
		droneGenomes=nbt.read("drones", Genome.LIST_CODEC).orElse(null);
		queenCount=nbt.getIntOr("queens", 0);
		larvaCount=nbt.getIntOr("larva", 0);
		droneCount=nbt.getIntOr("drone", 0);
		process=nbt.getIntOr("process", 0);
		processMax=nbt.getIntOr("processMax", 0);
	}
	/**
	 * 将蜂巢工作状态序列化到 ValueOutput 中。
	 * @param nbt 输出目标
	 */
	@Override
	public void serialize(ValueOutput nbt) {
		if(rs!=null)
			nbt.putLong("seed", rs.getSeed());
		if(queenGenome!=null)
			nbt.store("queen", Genome.LIST_CODEC, Arrays.asList(queenGenome));
		if(droneGenomes!=null)
			nbt.store("drones", Genome.LIST_CODEC,droneGenomes);
		nbt.putInt("queens", queenCount);
		nbt.putInt("larvaCount", larvaCount);
		nbt.putInt("droneCount", droneCount);
		nbt.putInt("process", process);
		nbt.putInt("processMax", processMax);
	}
	/**
	 * 从数据记录中恢复蜂巢工作状态（用于网络同步）。
	 * @param data 数据记录
	 */
	public void read(DataRecord data) {
		rs=data.random().orElse(null);
		setQueen(data.queenGenome().orElse(null));
		droneGenomes=data.droneGenomes().orElse(null);
		larvaCount=data.larvaCount();
		droneCount=data.droneCount();
		queenCount=data.queenCount();
		process=data.process();
		processMax=data.processMax();
	}

	/**
	 * 将当前工作状态保存为数据记录（用于网络同步）。
	 * @return 包含当前状态快照的数据记录
	 */
	public DataRecord save() {
		return new DataRecord(Optional.ofNullable(rs),
			Optional.ofNullable(queenGenome),
			Optional.ofNullable(droneGenomes),
			queenCount,
			larvaCount,
			droneCount,
			process,
			processMax);
	}
	/**
	 * 获取指定索引的容器数据。
	 * @param index 数据索引（0=当前进度，1=最大进度）
	 * @return 数据值
	 */
	@Override
	public int get(int index) {
		Objects.checkIndex(index, 2);
		switch(index) {
		case 0:return process;
		case 1:return processMax;
		}
		return -1;
	}
	/**
	 * 设置指定索引的容器数据。
	 * @param index 数据索引（0=当前进度，1=最大进度）
	 * @param value 数据值
	 */
	@Override
	public void set(int index, int value) {
		Objects.checkIndex(index, 2);
		switch(index) {
		case 0:process=value;return;
		case 1:processMax=value;return;
		}
	}
	/**
	 * 获取容器数据条目总数。
	 * @return 2（仅包含当前进度和最大进度）
	 */
	@Override
	public int getCount() {
		return 2;
	}
	/**
	 * 获取当前工作进度。
	 * @return 当前进度值
	 */
	public int getProcess() {
		return process;
	}
	/**
	 * 获取最大工作进度。
	 * @return 最大进度值
	 */
	public int getProcessMax() {
		return processMax;
	}
	/**
	 * 检查附近是否没有花。
	 * @return 如果附近无花则返回 true
	 */
	public boolean isNoFlower() {
		return noFlower;
	}
	/**
	 * 检查环境是否不适宜。
	 * @return 如果环境不适宜则返回 true
	 */
	public boolean isBadEnvironment() {
		return badEnvironment;
	}
}
