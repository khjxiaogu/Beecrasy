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

package com.khjxiaogu.beecrasy.client.apistle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import java.util.TreeMap;

import com.google.gson.JsonElement;
import com.khjxiaogu.beecrasy.events.ApistlePageRegistryEvent;
import com.mojang.serialization.JsonOps;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.neoforged.neoforge.common.NeoForge;

public class PageRegistry{
	public static final PageRegistry INSTANCE=new PageRegistry();
	Map<String,List<UnbakedPage>> pages;
	public void onResourceManagerReload(ResourceManager resourceManager, HolderLookup.Provider registryAccess) {
		Map<String,Map<Identifier,UnbakedPage>> pages=new TreeMap<>();
		
		for(Entry<Identifier, Page> ent:readPages(Minecraft.getInstance().getLanguageManager().getSelected(),resourceManager,registryAccess).entrySet()) {
			pages.computeIfAbsent(ent.getKey().getNamespace(), _->new HashMap<>()).put(ent.getKey(),ent.getValue());
			
		}
		for(Entry<Identifier, Page> ent:readPages("common",resourceManager,registryAccess).entrySet()) {
			pages.computeIfAbsent(ent.getKey().getNamespace(), _->new HashMap<>()).put(ent.getKey(),ent.getValue());
		}
		NeoForge.EVENT_BUS.post(new ApistlePageRegistryEvent(pages));
		Map<String,List<UnbakedPage>> result=new HashMap<>();
		for(Entry<String, Map<Identifier, UnbakedPage>> ent:pages.entrySet()) {
			List<UnbakedPage> pagelist=new ArrayList<>(ent.getValue().values());
			pagelist.sort(Comparator.comparingInt(UnbakedPage::order));
			result.put(ent.getKey(), pagelist);
		}
		this.pages=Collections.unmodifiableMap(result);
	}
	public Map<Identifier,Page> readPages(String lang, ResourceManager resourceManager, HolderLookup.Provider registryAccess){
		Map<Identifier,Page> langpages=new HashMap<>();
		FileToIdConverter convLocal=new FileToIdConverter("apistle/"+lang,".json");

		FileToIdConverter convMdLocal=new FileToIdConverter("apistle/"+lang,".md");
		RegistryOps<JsonElement> registry=RegistryOps.create(JsonOps.INSTANCE, registryAccess);
		SimpleJsonResourceReloadListener.scanDirectory(resourceManager, convLocal, registry, Page.CODEC, langpages);
		convMdLocal.listMatchingResources(resourceManager).forEach((id,resc)->{
			Page page=langpages.get(id);
			 try {
				if(page!=null)
					page=MarkdownParser.parse(registryAccess, page,IOUtils.toString(resc.open(), StandardCharsets.UTF_8));
				else
					page=MarkdownParser.parse(registryAccess, "",IOUtils.toString(resc.open(), StandardCharsets.UTF_8));
			} catch (IOException e) {
				e.printStackTrace();
			}
			langpages.put(id, page);
		});
		return langpages;
	}
	public List<UnbakedPage> getPages(String val) {
		return pages.getOrDefault(val,List.of());
	}

}
