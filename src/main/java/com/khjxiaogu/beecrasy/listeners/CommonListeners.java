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

package com.khjxiaogu.beecrasy.listeners;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Attachments;
import com.khjxiaogu.beecrasy.events.NaturalBeeGenomeGenerateEvent;
import com.khjxiaogu.beecrasy.genome.Genes;
import com.khjxiaogu.beecrasy.genome.Genes.Alleles;
import com.khjxiaogu.beecrasy.genome.gene.Humidity;
import com.khjxiaogu.beecrasy.genome.gene.Temperature;

import net.minecraft.data.worldgen.DimensionTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;

@EventBusSubscriber(modid = Beecrasy.MODID)
public class CommonListeners {
	@SubscribeEvent
	public static void onLogin(PlayerLoggedInEvent event) {
		Player p=event.getEntity();
		@Nullable Long comp=p.getData(Attachments.RANDOM_SEED);
		//Neoforge会初始化一个默认值，这里只做备用
		if(comp==null) {
			comp=RandomSupport.generateUniqueSeed();
			p.setData(Attachments.RANDOM_SEED, comp);
		}
	}
	@SubscribeEvent
	public static void onGenomeBuild(NaturalBeeGenomeGenerateEvent event) {
		if(event.level.dimensionTypeRegistration().is(BuiltinDimensionTypes.NETHER)) {
			event.genome.add(Genes.TEMPERATURE, Alleles.NETHER_TEMPERATURE);
		}else if(event.level.dimensionTypeRegistration().is(BuiltinDimensionTypes.NETHER)) {
			event.genome.add(Genes.TEMPERATURE, Alleles.ENDER_TEMPERATURE);
		}else{
			for(Humidity humid:Alleles.HUMIDITY) {
				if(humid.isNatural()&&humid.isValidFor(event.level, event.pos)) {
					event.genome.add(Genes.HUMIDITY, humid);
					break;
				}
			}
			Humidity humid=event.genome.get(Genes.HUMIDITY);
			for(Temperature temp:Alleles.TEMPERATURE) {
				if(temp.isNatural()&&temp.isValidFor(event.level, event.pos,humid)) {
					event.genome.add(Genes.TEMPERATURE, temp);
					break;
				}
			}
		}
	}
	
}
