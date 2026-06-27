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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.khjxiaogu.beecrasy.client.apistle.Page;
import com.khjxiaogu.beecrasy.client.apistle.PageBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.data.JsonCodecProvider;

public abstract class ApistleGenerator extends JsonCodecProvider<Page> {
	HolderLookup.Provider provider;
	public ApistleGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
		super(output, Target.RESOURCE_PACK, "apistle", Page.CODEC, lookupProvider, modId);
	}
	Map<Identifier,PageBuilder> builders=new HashMap<>();
    public PageBuilder add(Identifier name,String title) {
    	if(builders.containsKey(name))
    		throw new IllegalArgumentException(name+" Already Exists");
    	PageBuilder pb=new PageBuilder(provider,title);
    	builders.put(name,pb);
    	return pb;
    }
    public PageBuilder add(String name,String lang,String title) {
    	return add(Identifier.fromNamespaceAndPath(super.modid,lang+"/"+ name),title);
    }
    public PageBuilder addCommon(String name,String title) {
    	return add(name,"common",title);
    }
	@Override
	protected final void gather() {
		try {
			provider=this.lookupProvider.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		addPages();
		for(Entry<Identifier, PageBuilder> ent:builders.entrySet()) {
			this.unconditional(ent.getKey(), ent.getValue().build());
		}
	}
	protected abstract void addPages();
}
