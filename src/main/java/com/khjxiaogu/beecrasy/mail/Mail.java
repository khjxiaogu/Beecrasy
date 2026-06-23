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

package com.khjxiaogu.beecrasy.mail;

import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.common.UsernameCache;

public record Mail(UUID letterId,UUID sender,UUID receiver,Optional<ItemStackTemplate> icon,String line1,String line2,ItemContainerContents items){

	public static final Codec<Mail> CODEC=RecordCodecBuilder.create(t->t.group(
		UUIDUtil.CODEC.fieldOf("letterId").forGetter(Mail::letterId),
		UUIDUtil.CODEC.fieldOf("sender").forGetter(Mail::sender),
		UUIDUtil.CODEC.fieldOf("receiver").forGetter(Mail::receiver),
		ItemStackTemplate.CODEC.optionalFieldOf("icon").forGetter(Mail::icon),
		Codec.STRING.fieldOf("line1").forGetter(Mail::line1),
		Codec.STRING.fieldOf("line2").forGetter(Mail::line2),
		ItemContainerContents.CODEC.fieldOf("items").forGetter(Mail::items)
		).apply(t, Mail::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, Mail> STREAM_CODEC=StreamCodec.composite(
		UUIDUtil.STREAM_CODEC,Mail::letterId,
		UUIDUtil.STREAM_CODEC,Mail::sender,
		UUIDUtil.STREAM_CODEC,Mail::receiver,
		ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC),Mail::icon,
		ByteBufCodecs.STRING_UTF8,Mail::line1,
		ByteBufCodecs.STRING_UTF8,Mail::line2,
		ItemContainerContents.STREAM_CODEC,Mail::items,
			Mail::new);
	public MailComponent getMail() {
		
		Optional<String> idSender  =Optional.ofNullable(UsernameCache.getLastKnownUsername(sender));
		Optional<String> idReceiver=Optional.ofNullable(UsernameCache.getLastKnownUsername(receiver));
		return new MailComponent(idSender.orElse("<unknown>"),idReceiver.orElse("<unknown>"),icon,line1,line2,true);
	}
}
