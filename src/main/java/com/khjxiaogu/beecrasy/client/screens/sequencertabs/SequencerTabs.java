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

package com.khjxiaogu.beecrasy.client.screens.sequencertabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.khjxiaogu.beecrasy.events.SequencerTabRegistryEvent;

import net.neoforged.neoforge.common.NeoForge;

public class SequencerTabs {
	private static List<SequencerTab> tabs;
	public static void init() {
		List<SequencerTab> tabs=new ArrayList<>();
		tabs.add(new BasicGeneticsTab());
		tabs.add(new ProductsTab());
		SequencerTabRegistryEvent ev=new SequencerTabRegistryEvent(tabs::add);
		NeoForge.EVENT_BUS.post(ev);
		SequencerTabs.tabs=Collections.unmodifiableList(tabs);
	}
	public static List<SequencerTab> getTabs(){
		return tabs;
	}
}
