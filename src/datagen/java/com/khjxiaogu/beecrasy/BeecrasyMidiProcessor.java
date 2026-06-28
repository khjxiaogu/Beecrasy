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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import javax.sound.midi.InvalidMidiDataException;

import com.khjxiaogu.beecrasy.beedi.MidiSheet;
import com.khjxiaogu.beecrasy.data.MidiBinaryBakery;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;

public class BeecrasyMidiProcessor extends MidiBinaryBakery {

	public BeecrasyMidiProcessor(PackOutput output, CompletableFuture<Provider> lookupProvider) {
		super(output, lookupProvider, Beecrasy.MODID);
	}

	@Override
	protected void gather() {
		File resourcePath=new File("../../src/datagen/resources");
		try {
			this.addTask(Beecrasy.rl("flight_of_the_bumble_bee"), new MidiSheet(new FileInputStream(new File(resourcePath,"flight_of_the_bumble_bee.mid"))));
			this.addTask(Beecrasy.rl("seikilos_epitaph"), new MidiSheet(new FileInputStream(new File(resourcePath,"seikilos_epitaph.mid"))));
			this.addTask(Beecrasy.rl("bee_mart"), new MidiSheet(new FileInputStream(new File(resourcePath,"bee_mart.mid"))));
			this.addTask(Beecrasy.rl("bee_donald"), new MidiSheet(new FileInputStream(new File(resourcePath,"bee_donald.mid"))));
			
		} catch (IOException | InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}



}
