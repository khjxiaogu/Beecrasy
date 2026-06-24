package com.khjxiaogu.beecrasy.data;

import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.zip.DeflaterOutputStream;

import com.khjxiaogu.beecrasy.beedi.MidiSheet;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;

public abstract class MidiBinaryBakery extends FileBinaryBakery<MidiSheet> {
	public MidiBinaryBakery(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
		super(output, Target.RESOURCE_PACK, "beedi", MidiSheet.STREAM_CODEC, lookupProvider, modId);
	}

	@Override
	protected String getExtension() {
		return "bmid";
	}

	@Override
	protected OutputStream wrapStream(OutputStream in) {
		return new DeflaterOutputStream(in);
	}
	@Override
	public String getName() {
		return modid+" midi preprocessor";
	}
}
