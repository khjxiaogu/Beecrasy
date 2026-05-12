package com.khjxiaogu.beecrasy.genome;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public interface Gene<T>{
	public T getDefault();
	public Identifier id();
	public Codec<T> codec();
	public StreamCodec<RegistryFriendlyByteBuf,T> streamCodec();
}