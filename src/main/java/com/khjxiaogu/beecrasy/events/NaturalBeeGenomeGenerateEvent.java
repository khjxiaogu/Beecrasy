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

package com.khjxiaogu.beecrasy.events;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterSet;
import com.khjxiaogu.beecrasy.data.recipe.GenomePresets;
import com.khjxiaogu.beecrasy.genome.PartialGenome;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedList;
import net.neoforged.bus.api.Event;

public class NaturalBeeGenomeGenerateEvent extends Event{
	public final BeeHiveParameterSet params;
	public final PartialGenome.Builder genome;
	private final Set<Identifier> sets=new HashSet<>();
	public NaturalBeeGenomeGenerateEvent(ServerLevel level, BlockPos pos, PartialGenome.Builder genome) {
		super();
		this.params=new BeeHiveParameterSet.Builder(level,pos).build();
		this.genome = genome;
	}
	
	public NaturalBeeGenomeGenerateEvent(BeeHiveParameterSet params, PartialGenome.Builder genome) {
		super();
		this.params = params;
		this.genome = genome;
	}
	public boolean hasPool(Identifier id) {
		return sets.contains(id);
	}
	@SuppressWarnings("resource")
	public void applyPools(Identifier id) {
		if(sets.add(id))
			applyPools(GenomePresets.getPools(level(), id));
	}
	@SuppressWarnings("resource")
	private void applyPools(List<WeightedList<PartialGenome>> list) {
		for(WeightedList<PartialGenome> li:list) {
			li.getRandom(level().getRandom()).ifPresent(t->t.apply(genome));
		}
	}
	public ServerLevel level() {
		return params.level();
	}
}
