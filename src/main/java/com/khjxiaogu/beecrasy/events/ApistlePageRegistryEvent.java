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

import java.util.HashMap;
import java.util.Map;

import com.khjxiaogu.beecrasy.client.apistle.UnbakedPage;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;

public class ApistlePageRegistryEvent extends Event {
	Map<String,Map<Identifier, UnbakedPage>> pages;
	
	public ApistlePageRegistryEvent(Map<String, Map<Identifier, UnbakedPage>> pages) {
		super();
		this.pages = pages;
	}

	public void register(Identifier id,UnbakedPage page) {
		pages.computeIfAbsent(id.getNamespace(), _->new HashMap<>()).put(id,page);
	}
	
}
