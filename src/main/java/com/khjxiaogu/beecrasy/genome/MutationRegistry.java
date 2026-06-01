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

public class MutationRegistry {
	private static record MutationRecord(Identifier id,Mutation mutation) {
		
	}
	private static List<MutationRecord> mutations=new ArrayList<>();
	private MutationRegistry() {
	}
	public static synchronized Mutation register(Identifier id,Mutation type) {
		mutations.removeIf(t->t.id.equals(id));
		if(type!=null)
			mutations.add(new MutationRecord(id,type));
		return type;
	}
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
	public static void handleMutation(BeeHiveParameterSet params,Genome.Builder genome,double chanceMultiplier,RandomSource random) {
		handleMutation(params,new DiploidGenome(genome,genome.copy()),chanceMultiplier,random);
	}
}
