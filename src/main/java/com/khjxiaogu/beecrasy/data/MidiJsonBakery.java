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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.khjxiaogu.beecrasy.beedi.MidiSheet;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.common.data.JsonCodecProvider;

public abstract class MidiJsonBakery extends JsonCodecProvider<MidiSheet> {
	public MidiJsonBakery(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
		super(output, Target.RESOURCE_PACK, "beedi", MidiSheet.CODEC, lookupProvider, modId);
	}
    public void unconditional(Identifier id, InputStream midi) {
    	try{
    		unconditional(id, new MidiSheet(midi));
    	} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
    public void unconditional(Identifier id, File midi) {
    	try(InputStream is=new FileInputStream(midi)){
    		unconditional(id, new MidiSheet(is));
    	} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
    public void conditionallyFile(Identifier id, Consumer<WithConditions.Builder<File>> configurator) {
        final WithConditions.Builder<File> builder = new WithConditions.Builder<>();
        configurator.accept(builder);
        WithConditions<File> wc=builder.build();
        try(InputStream is=new FileInputStream(wc.carrier())){
        	MidiSheet ms=new MidiSheet(is);
        	super.conditionally(id, c->c.addCondition(wc.conditions()).withCarrier(ms));
        } catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
    public void conditionallyStream(Identifier id, Consumer<WithConditions.Builder<InputStream>> configurator) {
        final WithConditions.Builder<InputStream> builder = new WithConditions.Builder<>();
        configurator.accept(builder);
        WithConditions<InputStream> wc=builder.build();
        try{
        	MidiSheet ms=new MidiSheet(wc.carrier());
        	super.conditionally(id, c->c.addCondition(wc.conditions()).withCarrier(ms));
        } catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
}
