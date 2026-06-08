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

package com.khjxiaogu.beecrasy.genome;

import java.util.ArrayList;
import java.util.List;

import com.khjxiaogu.beecrasy.BeecrasyConfig;
import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterSet;

import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

/**
 * 突变注册表，管理所有突变类型的注册与统一触发逻辑。
 */
public class MutationRegistry {
	/**
	 * 突变记录，包含标识符和突变实例。
	 *
	 * @param id       突变标识符
	 * @param mutation 突变实例
	 */
	private static record MutationRecord(Identifier id,Mutation mutation) {
		
	}
	/** 所有已注册的突变列表。 */
	private static List<MutationRecord> mutations=new ArrayList<>();
	private MutationRegistry() {
	}
	/**
	 * 注册一个突变类型。
	 * <p>
	 * 如果已存在相同ID的突变，则先移除旧记录。
	 *
	 * @param id   突变标识符
	 * @param type 突变实例（可为 {@code null} 以清除）
	 * @return 注册的突变实例
	 */
	public static synchronized Mutation register(Identifier id,Mutation type) {
		mutations.removeIf(t->t.id.equals(id));
		if(type!=null)
			mutations.add(new MutationRecord(id,type));
		return type;
	}
	/**
	 * 遍历适用突变并按概率触发。
	 * <p>
	 * 筛选出当前适用的突变，综合考虑总概率和配置中的突变倍率后，按加权随机选择执行。
	 *
	 * @param params           蜂箱参数集合
	 * @param genome           子代的二倍体基因组
	 * @param chanceMultiplier 概率倍率
	 * @param random           随机序列
	 */
	public static void handleMutation(BeeHiveParameterSet params,DiploidGenome genome,double chanceMultiplier,RandomSource random) {
		if(chanceMultiplier<=0)
			return;
		List<MutationRecord> applicable=new ArrayList<>(mutations.size());
		for(MutationRecord mr:mutations) {
			if(params.disabledMutation().contains(mr.id))
				continue;
			if(mr.mutation.isApplicable(params, genome)) {
				applicable.add(mr);
			}
		}
		if(applicable.isEmpty())
			return;
		double totalChance=BeecrasyConfig.SERVER.MUTATION_CHANCE.getAsDouble();
		double applicableChance=0;
		double[] chances=new double[applicable.size()];
		int i=0;
		for(MutationRecord mr:applicable) {
			double chance=mr.mutation.getChance(params, genome);
			applicableChance+=chance;
			chances[i++]=chance;
		}
		double activeChance=Math.min(Math.min(applicableChance, totalChance)*chanceMultiplier,1);
		double rate=random.nextDouble();
		for(i=0;i<chances.length;i++) {
			double chance=(chances[i]/applicableChance)*activeChance;
			if(rate<chance) {
				applicable.get(i).mutation.mutate(params, genome, random);
				break;
			}else {
				rate-=chance;
			}
		}
		
	}
	/**
	 * 遍历适用突变并按概率触发（使用单构建器自动转换为二倍体）。
	 *
	 * @param params           蜂箱参数集合
	 * @param genome           基因组建构器
	 * @param chanceMultiplier 概率倍率
	 * @param random           随机序列
	 */
	public static void handleMutation(BeeHiveParameterSet params,Genome.Builder genome,double chanceMultiplier,RandomSource random) {
		handleMutation(params,new DiploidGenome(genome,genome.copy()),chanceMultiplier,random);
	}
}
