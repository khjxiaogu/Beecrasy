package com.khjxiaogu.beecrasy.client.apistle.lines;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public interface UnbakedLine {
	public static final Map<String,MapCodec<UnbakedLine>> codecs=new HashMap<>();
	public static final Codec<UnbakedLine> CODEC=Codec.STRING.fieldOf("type").dispatch(UnbakedLine::type, codecs::get);

	Line bake(int width);
	String type();
}
