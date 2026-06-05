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

package com.khjxiaogu.beecrasy.client.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;


public enum BeeMovement{
	RANDOM,
	FIGURE_8_X,
	FIGURE_8_Z,
	CIRCLE_X,
	CIRCLE_Z;
	public static final Codec<BeeMovement> CODEC=Codec.STRING.comapFlatMap(
		t->{
			try {
				return DataResult.success(BeeMovement.valueOf(t.toUpperCase()));
			}catch(IllegalArgumentException err) {
				return DataResult.error(()->"No movement '"+t+"'");
			}
		}, t->t.name().toLowerCase()
		);
	public static final StreamCodec<ByteBuf,BeeMovement> STREAM_CODEC=ByteBufCodecs.VAR_INT.map(o->BeeMovement.values()[o],BeeMovement::ordinal);
	private BeeMovement() {
	}
}
