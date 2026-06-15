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
import com.khjxiaogu.beecrasy.menu.MailMenu;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MailMessage(int containerId,Optional<String> receiver,Optional<String> line1,Optional<String> line2) implements CustomPacketPayload{

	public static final Type<MailMessage> TYPE=new Type<>(Beecrasy.rl("mail_text"));
	public static final StreamCodec<ByteBuf, MailMessage> CODEC=StreamCodec.composite(
		ByteBufCodecs.VAR_INT,MailMessage::containerId,
		ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), MailMessage::receiver,
		ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), MailMessage::line1,
		ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), MailMessage::line2,
		MailMessage::new);
	public static MailMessage ofReceiver(int containerId,String receiver) {
		return new MailMessage(containerId,Optional.of(receiver),Optional.empty(),Optional.empty());
	}
	public static MailMessage ofLine1(int containerId,String line1) {
		return new MailMessage(containerId,Optional.empty(),Optional.of(line1),Optional.empty());
	}
	public static MailMessage ofLine2(int containerId,String line2) {
		return new MailMessage(containerId,Optional.empty(),Optional.empty(),Optional.of(line2));
	}
	void handle(IPayloadContext context) {
		context.enqueueWork(()->{
			AbstractContainerMenu openedMenu=context.player().containerMenu;
			if (openedMenu.containerId==containerId&&openedMenu instanceof MailMenu opm) {
				opm.setLetterText(receiver, line1, line2);
			}
		});
	}
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
