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

import java.util.List;

import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class BeehiveHandler {
	Genome[] queenGenome;
	List<Genome> droneGenomes;
	ResourceHandler<ItemResource> COMB_SLOTS;
	ResourceHandler<ItemResource> DRONE_SLOTS;
	
	
	public DiploidGenome makeLarva(BeeHiveParameters params,RandomSource rs) {
		DiploidGenome ret=RecombinationHelper.makeDiploid(queenGenome, getRandomDrone(rs), rs::nextBoolean);
		MutationRegistry.handleMutation(params, ret, rs);
		
		return ret;
	}
	public Genome.Builder makeDrone(BeeHiveParameters params,RandomSource rs) {
		Genome.Builder ret=RecombinationHelper.makeHaploid(queenGenome, rs::nextBoolean);
		return ret;
	}
	public Genome getRandomDrone(RandomSource rs) {
		return droneGenomes.get(rs.nextInt(droneGenomes.size()));
	}
}
