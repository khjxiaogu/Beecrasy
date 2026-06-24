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
		} catch (IOException | InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}



}
