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

package com.khjxiaogu.beecrasy;


import java.util.concurrent.CompletableFuture;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Fluids;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Tags;
import com.khjxiaogu.beecrasy.data.BeecrasyTagGenerator;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.material.Fluid;

@SuppressWarnings("unused")
public class BeecrasyFluidTagGenerator extends BeecrasyTagGenerator<Fluid> {

	public BeecrasyFluidTagGenerator(DataGenerator dataGenerator, String modId,CompletableFuture<HolderLookup.Provider> provider) {
		super(dataGenerator, Registries.FLUID,modId,provider);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void addTags(Provider pProvider) {
		tag(Tags.HONEY).add(rk(Fluids.HONEY_FLOWING)).add(rk(Fluids.HONEY_STILL));
	}
}
