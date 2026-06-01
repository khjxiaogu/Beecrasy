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

import java.util.function.Consumer;

import com.khjxiaogu.beecrasy.client.screens.sequencertabs.SequencerTab;

import net.neoforged.bus.api.Event;

public class SequencerTabRegistryEvent extends Event {
	private Consumer<SequencerTab> tabAdder;

	public SequencerTabRegistryEvent(Consumer<SequencerTab> tabAdder) {
		super();
		this.tabAdder = tabAdder;
	}
	public void register(SequencerTab tab) {
		tabAdder.accept(tab);
	}
}
