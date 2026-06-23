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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record BeediDisk(long ticks,Identifier name,int comparatorOutput) {

	public static final Codec<BeediDisk> CODEC=RecordCodecBuilder.create(t->t.group(
			Codec.LONG.fieldOf("ticks").forGetter(BeediDisk::ticks),
			Identifier.CODEC.fieldOf("name").forGetter(BeediDisk::name),
			Codec.INT.fieldOf("comparator").forGetter(BeediDisk::comparatorOutput)
			).apply(t, BeediDisk::new));
	public static final StreamCodec<ByteBuf,BeediDisk> STREAM_CODEC=StreamCodec.composite(
			ByteBufCodecs.VAR_LONG,BeediDisk::ticks,
			Identifier.STREAM_CODEC,BeediDisk::name,
			ByteBufCodecs.VAR_INT,BeediDisk::comparatorOutput,
			BeediDisk::new
			);
}
