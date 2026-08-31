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

package com.khjxiaogu.beecrasy.network;


import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.genome.Gene;
import com.khjxiaogu.beecrasy.genome.GeneRegistry;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.Genome.Builder;
import com.khjxiaogu.beecrasy.menu.GMMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GeneticEditMessage(int containerId,int geneSlot,Gene gene,Object data) implements CustomPacketPayload{

	public static final Type<GeneticEditMessage> TYPE=new Type<>(Beecrasy.rl("synthesizer_edit"));

	public static final StreamCodec<RegistryFriendlyByteBuf,GeneticEditMessage> CODEC=new StreamCodec<>() {
		@Override
		public void encode(RegistryFriendlyByteBuf output, GeneticEditMessage value) {
			ByteBufCodecs.VAR_INT.encode(output, value.containerId());
			ByteBufCodecs.VAR_INT.encode(output, value.geneSlot());
			GeneRegistry.STREAM_CODEC.encode(output, value.gene());
			value.gene().streamCodec().encode(output, value.data());
		}
		@Override
		public GeneticEditMessage decode(RegistryFriendlyByteBuf input) {
			int containerId=ByteBufCodecs.VAR_INT.decode(input);
			int geneSlot=ByteBufCodecs.VAR_INT.decode(input);
			Gene<?> gene=GeneRegistry.STREAM_CODEC.decode(input);
			Object data=gene.streamCodec().decode(input);
			return new GeneticEditMessage(containerId,geneSlot,gene,data);
		}
	};
	void handle(IPayloadContext context) {
		context.enqueueWork(()->{
			AbstractContainerMenu openedMenu=context.player().containerMenu;
			if (openedMenu.containerId==containerId()&&openedMenu instanceof GMMenu opm) {
				ItemStack stack=opm.getSlot(0).getItem();
				@Nullable GenomeComponent gene=stack.get(Components.GENOME);
				if(gene!=null) {
					Genome genome=gene.getGenome(geneSlot());
					Builder gb=genome.createBuilder();
					gb.add(gene(), data());
					gene=gene.setGenome(geneSlot(), gb.build());
					stack.set(Components.GENOME, gene);
					opm.getSlot(0).set(stack);
				}
			}
		});
	}
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
