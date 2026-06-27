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
import com.khjxiaogu.beecrasy.menu.MailMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MailSetIconMessage(int containerId,ItemStack stack) implements CustomPacketPayload{

	public static final Type<MailSetIconMessage> TYPE=new Type<>(Beecrasy.rl("mail_icon"));
	public static final StreamCodec<RegistryFriendlyByteBuf, MailSetIconMessage> CODEC=StreamCodec.composite(
		ByteBufCodecs.VAR_INT,MailSetIconMessage::containerId,
		ItemStack.STREAM_CODEC,MailSetIconMessage::stack,
		MailSetIconMessage::new);
	void handle(IPayloadContext context) {
		context.enqueueWork(()->{
			AbstractContainerMenu openedMenu=context.player().containerMenu;
			if (openedMenu.containerId==containerId&&openedMenu instanceof MailMenu opm) {
				opm.setIcon(stack);
			}
		});
	}
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
