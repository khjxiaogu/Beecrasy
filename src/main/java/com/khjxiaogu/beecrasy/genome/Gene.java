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

package com.khjxiaogu.beecrasy.genome;

import java.util.function.Consumer;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public interface Gene<T>{
	public T getDefault();
	public Identifier id();
	public Codec<T> codec();
	public StreamCodec<RegistryFriendlyByteBuf,T> streamCodec();
	public void getReadableText(T allele,Consumer<Component> text);

	public default void getReadableText(AllelesHolder genome,Consumer<Component> text) {
		getReadableText(genome.getAllele(this),text);
	}
	public void getShortReadableText(T allele,Consumer<Component> text);

	public default void getShortReadableText(AllelesHolder genome,Consumer<Component> text) {
		getShortReadableText(genome.getAllele(this),text);
	}
	public String getLanguageKey();
	public String getShortLanguageKey();;
}