package com.khjxiaogu.beecrasy.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TintColorComponent(int color) {
	public static final Codec<TintColorComponent> CODEC=RecordCodecBuilder.create(t->t.group(Codec.INT.fieldOf("color").forGetter(TintColorComponent::color)).apply(t, TintColorComponent::new));
	public static final StreamCodec<ByteBuf,TintColorComponent> NETWORK_CODEC=ByteBufCodecs.INT.map(TintColorComponent::new, TintColorComponent::color);
}
