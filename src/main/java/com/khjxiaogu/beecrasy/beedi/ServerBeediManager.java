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

package com.khjxiaogu.beecrasy.beedi;

import java.util.Optional;

import javax.annotation.Nullable;

import com.khjxiaogu.beecrasy.network.BeediPlayStatusMessage;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;

public class ServerBeediManager {
	public static void playSong(ServerLevel l,BlockPos pos,Identifier song,@Nullable Identifier sound,int offset,float speed) {
		PacketDistributor.sendToPlayersNear(l, null, pos.getX(), pos.getY(), pos.getZ(), 64, new BeediPlayStatusMessage(Optional.of(song),Optional.ofNullable(sound),pos,offset,speed));
		
	}
	public static void stopSong(ServerLevel l,BlockPos pos) {
		PacketDistributor.sendToPlayersInDimension(l, new BeediPlayStatusMessage(pos));
		
	}
}
