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


import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.client.apistle.ApistleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenApistleMessage(String id,Component title) implements CustomPacketPayload{

	public static final Type<OpenApistleMessage> TYPE=new Type<>(Beecrasy.rl("open_apistle"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenApistleMessage> CODEC=StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8,OpenApistleMessage::id,
		ComponentSerialization.STREAM_CODEC,OpenApistleMessage::title,
		OpenApistleMessage::new);
	void handle(IPayloadContext context) {
		context.enqueueWork(()->{
			Minecraft.getInstance().setScreen(new ApistleScreen(id,title));
		});
	}
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
