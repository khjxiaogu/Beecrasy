/*
 * Copyright (c) 2024 TeamMoeg
 *
 * This file is part of Caupona.
 *
 * Caupona is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Caupona is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Specially, we allow this software to be used alongside with closed source software Minecraft(R) and Forge or other modloader.
 * Any mods or plugins can also use apis provided by forge or com.teammoeg.caupona.api without using GPL or open source.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caupona. If not, see <https://www.gnu.org/licenses/>.
 */

package com.khjxiaogu.beecrasy.network;


import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.menu.OperatableMenu;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ContainerOperationMessage(int containerId,short opCode,int opData) implements CustomPacketPayload{

	public static final Type<ContainerOperationMessage> TYPE=new Type<>(Beecrasy.rl("container_operation"));
	public static final StreamCodec<ByteBuf, ContainerOperationMessage> CODEC=StreamCodec.composite(
		ByteBufCodecs.INT,ContainerOperationMessage::containerId,
		ByteBufCodecs.SHORT, ContainerOperationMessage::opCode,
		ByteBufCodecs.INT,ContainerOperationMessage::opData,
		ContainerOperationMessage::new);
	void handle(IPayloadContext context) {
		context.enqueueWork(()->{
			AbstractContainerMenu openedMenu=context.player().containerMenu;
			if (openedMenu.containerId==containerId&&openedMenu instanceof OperatableMenu opm) {
				opm.receiveOperation(opCode, opData);
			}
		});
	}
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
