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

import java.util.List;
import java.util.Set;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Beecrasy.MODID)
public class BeecrasyDataGenerator {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent.Server event) {
		System.out.println("Gather server data");
		DataGenerator gen = event.getGenerator();
		gen.addProvider(true,new BeecrasyBlockTagGenerator(gen, Beecrasy.MODID,event.getLookupProvider()));
		gen.addProvider(true,new BeecrasyFluidTagGenerator(gen, Beecrasy.MODID,event.getLookupProvider()));
		gen.addProvider(true,new BeecrasyItemTagGenerator(gen, Beecrasy.MODID,event.getLookupProvider()));
		
		
		gen.addProvider(true, new BeecrasyRecipeProvider.Runner(gen.getPackOutput(), event.getLookupProvider()));
		gen.addProvider(true, new LootTableProvider(gen.getPackOutput(),Set.of(),
            List.of(
                new LootTableProvider.SubProviderEntry(BeecrasyLootProvider::new, LootContextParamSets.BLOCK)
            ),event.getLookupProvider()
        ));
	}
	@SubscribeEvent
	public static void gatherData(GatherDataEvent.Client event) {
		System.out.println("Gather client data");
		DataGenerator gen = event.getGenerator();
		
		gen.addProvider(true,new BeecrasyModelProvider(gen.getPackOutput(), Beecrasy.MODID,event.getResourceManager(PackType.CLIENT_RESOURCES)));
		gen.addProvider(true, new BeecrasyLangGenerator(gen.getPackOutput(), Beecrasy.MODID,"en_us"));
		gen.addProvider(true, new BeecrasyParticleProvider(gen.getPackOutput()));
		gen.addProvider(true, new BeecrasyMidiProcessor(gen.getPackOutput(),event.getLookupProvider()));
		gen.addProvider(true,new BeecrasyApistleGenerator(gen.getPackOutput(),event.getLookupProvider()));
		
	}
}
