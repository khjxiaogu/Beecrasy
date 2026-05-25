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

import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.Genome.Builder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;

public class NaturalBeeGenomeGenerateEvent extends Event{
	public final ServerLevel level;
	public final BlockPos pos;
	public final Holder<Biome> biome;
	public final BlockState requester;
	public final Genome.Builder genome;
	public NaturalBeeGenomeGenerateEvent(ServerLevel level, BlockPos pos, Holder<Biome> biome, BlockState requester,
			Builder genome) {
		super();
		this.level = level;
		this.pos = pos;
		this.biome = biome;
		this.requester = requester;
		this.genome = genome;
	}
}
