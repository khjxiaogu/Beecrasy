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

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PacketHandler {

	public static void sendToServer(CustomPacketPayload message) {
		ClientPacketDistributor.sendToServer(message);
	}
	public static void sendToPlayer(ServerPlayer player,CustomPacketPayload message) {
		PacketDistributor.sendToPlayer(player, message);
	}
	public static void registerPackets(RegisterPayloadHandlersEvent ev) {
		
		PayloadRegistrar registry=ev.registrar(Beecrasy.MODID);
		registry.playToServer(ContainerOperationMessage.TYPE, ContainerOperationMessage.CODEC,ContainerOperationMessage::handle);
		registry.playToClient(ContainerTankMessage.TYPE, ContainerTankMessage.CODEC, ContainerTankMessage::handle);
		registry.playToServer(MailEditMessage.TYPE, MailEditMessage.CODEC,MailEditMessage::handle);
		registry.playToClient(ClientMailReceivedMessage.TYPE, ClientMailReceivedMessage.CODEC,ClientMailReceivedMessage::handle);
		registry.playToClient(BeediPlayStatusMessage.TYPE, BeediPlayStatusMessage.CODEC,BeediPlayStatusMessage::handle);
		registry.versioned("1");
	}
}