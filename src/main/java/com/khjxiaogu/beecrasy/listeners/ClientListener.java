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
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.genome.Genes;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddAttributeTooltipsEvent;

@EventBusSubscriber(modid = Beecrasy.MODID, value=Dist.CLIENT)
public class ClientListener {
	@SubscribeEvent
	public static void addTooltip(AddAttributeTooltipsEvent event) {
		ItemStack stack=event.getStack();
		GenomeComponent genome=stack.get(Components.GENOME);
		
		if(genome!=null) {
			if(genome.isInspected()) {
				Genes.TEMPERATURE.getReadableText(genome.getGenome(0), event::addTooltipLines);
				Genes.HUMIDITY.getReadableText(genome.getGenome(0), event::addTooltipLines);
				Genes.BIOTOPE.getReadableText(genome.getGenome(0), event::addTooltipLines);
			}else {

				event.addTooltipLines(Component.translatable("tooltip.beecrasy.to_be_inspected"));
			}
		}
		if(stack.is(Items.PRODUCT_COMB)) {

			@Nullable ItemStackTemplate product=stack.get(Components.COMB_PRODUCT);
			if(product==null) {
				event.addTooltipLines(Component.translatable("tooltip.beecrasy.no_special_product"));
			}else {
				event.addTooltipLines(Component.translatable("tooltip.beecrasy.possible_product", product.get(DataComponents.ITEM_NAME)));
			}
		}
	}
}
