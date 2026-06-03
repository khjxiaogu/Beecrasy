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

import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterSet;
import com.khjxiaogu.beecrasy.genome.AllelesHolder;
import com.khjxiaogu.beecrasy.genome.Gene;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class BeeEnvironmentValidateEvent extends Event implements ICancellableEvent,AllelesHolder{
	BeeHiveParameterSet params;
	AllelesHolder phenoType;
	public BeeEnvironmentValidateEvent(BeeHiveParameterSet params, AllelesHolder phenoType) {
		super();
		this.params = params;
		this.phenoType = phenoType;
	}
	public BeeHiveParameterSet getParams() {
		return params;
	}
	public AllelesHolder getPhenoType() {
		return phenoType;
	}
	@Override
	public <T> T getAllele(Gene<T> type) {
		return phenoType.getAllele(type);
	}
}
