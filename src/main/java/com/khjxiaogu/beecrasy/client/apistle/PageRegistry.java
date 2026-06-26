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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.JsonOps;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;

public class PageRegistry implements ResourceManagerReloadListener{
	public static final PageRegistry INSTANCE=new PageRegistry();
	List<Page> pages;
	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		pages=new ArrayList<>();
		Map<Identifier,Page> allpages=new HashMap<>();
		FileToIdConverter convLocal=new FileToIdConverter("apistle/"+Minecraft.getInstance().getLanguageManager().getSelected(),".json");
		SimpleJsonResourceReloadListener.scanDirectory(resourceManager, convLocal, JsonOps.INSTANCE, Page.CODEC, allpages);

		FileToIdConverter conv=new FileToIdConverter("apistle/common",".json");
		SimpleJsonResourceReloadListener.scanDirectory(resourceManager, conv, JsonOps.INSTANCE, Page.CODEC, allpages);
		pages.addAll(allpages.values());
		pages.sort(Comparator.comparingInt(Page::order));
	}
	public List<Page> getPages() {
		return pages;
	}

}
