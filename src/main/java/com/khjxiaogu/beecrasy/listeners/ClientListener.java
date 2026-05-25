package com.khjxiaogu.beecrasy.listeners;

import java.util.function.Consumer;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.genome.Genes;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = Beecrasy.MODID, value=Dist.CLIENT)
public class ClientListener {
	@SubscribeEvent
	public static void addTooltip(ItemTooltipEvent event) {
		ItemStack stack=event.getItemStack();
		GenomeComponent genome=stack.get(Components.GENOME);
		
		if(genome!=null&&genome.isInspected()) {
			Consumer<Component> tooltipAdder=event.getToolTip()::add;
			Genes.TEMPERATURE.getReadableText(genome.getGenome(0), tooltipAdder);
			Genes.HUMIDITY.getReadableText(genome.getGenome(0), tooltipAdder);
			Genes.BIOTOPE.getReadableText(genome.getGenome(0), tooltipAdder);
		}
	}
}
