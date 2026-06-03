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

package com.khjxiaogu.beecrasy.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BeehiveArgumenter(BeeHiveArgumentation modifiers,boolean consumeOnUse) {
	public static final Codec<BeehiveArgumenter> CODEC=RecordCodecBuilder.create(t->
	t.group(BeeHiveArgumentation.CODEC.fieldOf("modifiers").forGetter(BeehiveArgumenter::modifiers),
	Codec.BOOL.fieldOf("consumeOnUse").forGetter(BeehiveArgumenter::consumeOnUse)
	).apply(t, BeehiveArgumenter::new));
	public static final StreamCodec<RegistryFriendlyByteBuf,BeehiveArgumenter> STREAM_CODEC=StreamCodec.composite(
		BeeHiveArgumentation.STREAM_CODEC,BeehiveArgumenter::modifiers,
		ByteBufCodecs.BOOL,BeehiveArgumenter::consumeOnUse,
		BeehiveArgumenter::new);
}
