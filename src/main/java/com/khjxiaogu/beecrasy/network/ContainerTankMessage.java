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


import java.util.ArrayList;
import java.util.List;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.menu.BeecrasyContainerMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public record ContainerTankMessage(int containerId,List<MessagePair> list) implements CustomPacketPayload{
	public static record MessagePair(int tank,FluidResource fluid,int amount) {
		public static final StreamCodec<RegistryFriendlyByteBuf, MessagePair> CODEC=StreamCodec.composite(
			ByteBufCodecs.VAR_INT, MessagePair::tank,
			FluidResource.STREAM_CODEC,MessagePair::fluid,
			ByteBufCodecs.VAR_INT,MessagePair::amount,
			MessagePair::new);
	}
	public static final Type<ContainerTankMessage> TYPE=new Type<>(Beecrasy.rl("tank"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ContainerTankMessage> CODEC=StreamCodec.composite(
		ByteBufCodecs.VAR_INT,ContainerTankMessage::containerId,
		MessagePair.CODEC.apply(ByteBufCodecs.list()), ContainerTankMessage::list,
		ContainerTankMessage::new);
	void handle(IPayloadContext context) {
		context.enqueueWork(()->{
			AbstractContainerMenu openedMenu=context.player().containerMenu;
			if (openedMenu.containerId==containerId&&openedMenu instanceof BeecrasyContainerMenu menu) {
				menu.processPacket(this);
			}
		});
	}
	public ContainerTankMessage(int containerId) {
		this(containerId,new ArrayList<>());
	}
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
