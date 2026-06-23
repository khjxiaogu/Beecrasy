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

import java.util.Optional;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.beedi.BeediManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BeediPlayStatusMessage(Optional<Identifier> music,BlockPos pos)  implements CustomPacketPayload{
	public static final Type<BeediPlayStatusMessage> TYPE=new Type<>(Beecrasy.rl("midi_status"));
	public static final StreamCodec<RegistryFriendlyByteBuf, BeediPlayStatusMessage> CODEC=StreamCodec.composite(
		ByteBufCodecs.optional(Identifier.STREAM_CODEC),BeediPlayStatusMessage::music,
		BlockPos.STREAM_CODEC, BeediPlayStatusMessage::pos,
		BeediPlayStatusMessage::new);
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
	void handle(IPayloadContext context) {
		context.enqueueWork(()->{
			if(context.player().level() instanceof ClientLevel level) {
				if(music.isPresent())
					BeediManager.INSTANCE.playSong(level, music.get(), pos);
				else
					BeediManager.INSTANCE.stopSongAndNotifyNearby(level,pos);
			}
		});
	}
}
