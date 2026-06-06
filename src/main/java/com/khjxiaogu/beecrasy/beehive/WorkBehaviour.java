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

package com.khjxiaogu.beecrasy.beehive;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum WorkBehaviour{
	MAUNAL,//人工
	AUTO,//自动
	REDSTONE;//红石
	private final String key="gui.beehive.control."+this.name().toLowerCase();
	private final Component text=Component.translatable(key);
	public static final Codec<WorkBehaviour> CODEC=Codec.INT.xmap(i->WorkBehaviour.values()[i], WorkBehaviour::ordinal);
	public static final StreamCodec<ByteBuf,WorkBehaviour> STREAM_CODEC=ByteBufCodecs.INT.map(i->WorkBehaviour.values()[i], WorkBehaviour::ordinal);
	public Component getComponents() {
		return text;
	}
	public String getTranslationKey() {
		return key;
	}
}