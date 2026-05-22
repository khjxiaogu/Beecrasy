/** 
* Copyright (c) 2026 khjxiaogu
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
package com.khjxiaogu.beecrasy.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public record PossibleOutput(ItemStackTemplate stack,float chance) {
	public static final Codec<PossibleOutput> CODEC=RecordCodecBuilder.create(t->t
		.group(ItemStackTemplate.CODEC.fieldOf("stack").forGetter(PossibleOutput::stack),
			Codec.FLOAT.fieldOf("chance").forGetter(PossibleOutput::chance))
		.apply(t, PossibleOutput::new)
		);
	public static final StreamCodec<RegistryFriendlyByteBuf,PossibleOutput> STREAM_CODEC=StreamCodec.composite(
		ItemStackTemplate.STREAM_CODEC,PossibleOutput::stack,
		ByteBufCodecs.FLOAT,PossibleOutput::chance,
		PossibleOutput::new
		);
	public ItemStack createOutput(RandomSource rnd) {
		int count=Mth.floor(chance);
		if(rnd.nextFloat()<Mth.frac(chance))
			count++;
		if(count>0) {
			count*=stack.count();
			ItemStack ret=stack.create();
			ret.setCount(count);
			return ret;
		}
		return ItemStack.EMPTY;
	}
}
