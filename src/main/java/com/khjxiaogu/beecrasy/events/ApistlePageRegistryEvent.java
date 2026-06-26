package com.khjxiaogu.beecrasy.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.khjxiaogu.beecrasy.client.apistle.UnbakedPage;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;

public class ApistlePageRegistryEvent extends Event {
	Map<String,List<UnbakedPage>> pages;
	
	public ApistlePageRegistryEvent(Map<String, List<UnbakedPage>> pages) {
		super();
		this.pages = pages;
	}

	public void register(Identifier id,UnbakedPage page) {
		pages.computeIfAbsent(id.getNamespace(), _->new ArrayList<>()).add(page);
	}
	
}
