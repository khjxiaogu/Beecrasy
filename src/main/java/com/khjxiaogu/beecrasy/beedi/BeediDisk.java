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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record BeediDisk(long ticks,Identifier name,Optional<Identifier> sound,int comparatorOutput,int offset,float speed,TextColor color) {

	public static final Codec<BeediDisk> CODEC=RecordCodecBuilder.create(t->t.group(
			Codec.LONG.fieldOf("ticks").forGetter(BeediDisk::ticks),
			Identifier.CODEC.fieldOf("name").forGetter(BeediDisk::name),
			Identifier.CODEC.optionalFieldOf("sound").forGetter(BeediDisk::sound),
			Codec.INT.optionalFieldOf("comparator",0).forGetter(BeediDisk::comparatorOutput),
			Codec.INT.optionalFieldOf("offset",0).forGetter(BeediDisk::offset),
			Codec.FLOAT.optionalFieldOf("speed",1f).forGetter(BeediDisk::speed),
			TextColor.CODEC.optionalFieldOf("color",TextColor.fromRgb(0)).forGetter(BeediDisk::color)
			).apply(t, BeediDisk::new));
	public static final StreamCodec<ByteBuf,BeediDisk> STREAM_CODEC=StreamCodec.composite(
			ByteBufCodecs.VAR_LONG,BeediDisk::ticks,
			Identifier.STREAM_CODEC,BeediDisk::name,
			ByteBufCodecs.optional(Identifier.STREAM_CODEC),BeediDisk::sound,
			ByteBufCodecs.VAR_INT,BeediDisk::comparatorOutput,
			ByteBufCodecs.VAR_INT,BeediDisk::offset,
			ByteBufCodecs.FLOAT,BeediDisk::speed,
			ByteBufCodecs.INT.map(TextColor::fromRgb, TextColor::getValue),t->t.color(),
			BeediDisk::new
			);
	public BeediDisk(long ticks,Identifier name,int comparatorOutput,TextColor color) {
		this(ticks,name,Optional.empty(),comparatorOutput,0,1,color);
	}
}
