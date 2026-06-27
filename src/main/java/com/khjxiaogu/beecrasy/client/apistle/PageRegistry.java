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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
		Map<String,List<UnbakedPage>> pages=new TreeMap<>();
		Map<Identifier,Page> langpages=new HashMap<>();
		FileToIdConverter convLocal=new FileToIdConverter("apistle/"+Minecraft.getInstance().getLanguageManager().getSelected(),".json");
		RegistryOps<JsonElement> registry=RegistryOps.create(JsonOps.INSTANCE, registryAccess);
		SimpleJsonResourceReloadListener.scanDirectory(resourceManager, convLocal, registry, Page.CODEC, langpages);
		Map<Identifier,Page> commonpages=new HashMap<>();
		FileToIdConverter conv=new FileToIdConverter("apistle/common",".json");
		SimpleJsonResourceReloadListener.scanDirectory(resourceManager, conv, registry, Page.CODEC, commonpages);
		commonpages.putAll(langpages);
		for(Entry<Identifier, Page> ent:commonpages.entrySet()) {
			pages.computeIfAbsent(ent.getKey().getNamespace(), _->new ArrayList<>()).add(ent.getValue());
		}
		NeoForge.EVENT_BUS.post(new ApistlePageRegistryEvent(pages));

		pages.replaceAll((_,v)->{
			v.sort(Comparator.comparingInt(UnbakedPage::order));
			return Collections.unmodifiableList(v);
		});
		this.pages=Collections.unmodifiableMap(pages);
	}
	public List<UnbakedPage> getPages(String val) {
		return pages.getOrDefault(val,List.of());
	}

}
