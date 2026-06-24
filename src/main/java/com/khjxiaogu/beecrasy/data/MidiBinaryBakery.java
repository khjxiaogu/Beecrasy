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
