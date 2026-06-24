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

public record NoteInfo(long begin,int pitch,byte volume,long length){
	public record NoteOn(long begin,int pitch,byte volume){
		public NoteOn(int key, long begin, int vol) {
			this(begin,key,(byte)vol);
		}
		public NoteInfo off(long end) {
			return new NoteInfo(begin,pitch,volume,end-begin);
		}
	}
	public static final StreamCodec<ByteBuf,NoteInfo> STREAM_CODEC=StreamCodec.composite(
		ByteBufCodecs.VAR_LONG,NoteInfo::begin,
		ByteBufCodecs.VAR_INT,NoteInfo::pitch,
		ByteBufCodecs.BYTE,NoteInfo::volume,
		ByteBufCodecs.VAR_LONG,NoteInfo::length,
		NoteInfo::new
		);
	public static final Codec<NoteInfo> CODEC=RecordCodecBuilder.create(t->t.group(
		Codec.LONG.fieldOf("begin").forGetter(NoteInfo::begin),
		Codec.INT.fieldOf("pitch").forGetter(NoteInfo::pitch),
		Codec.BYTE.fieldOf("volume").forGetter(NoteInfo::volume),
		Codec.LONG.fieldOf("length").forGetter(NoteInfo::length)
		).apply(t,NoteInfo::new));
	public NoteInfo(int key, long begin, int vol,long length) {
		this(begin,key,(byte)vol,length);
	}

}