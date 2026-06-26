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

package com.khjxiaogu.beecrasy.client.apistle.lines;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.util.Util;

public interface UnbakedLine {
	public static final Map<String,MapCodec<? extends UnbakedLine>> codecs=Util.make(()->{
		Map<String,MapCodec<? extends UnbakedLine>> map=new HashMap<>();
		map.put("hr", HLine.CODEC);
		map.put("image", Image.CODEC);
		map.put("item", ItemSpotLine.CODEC);
		map.put("space", SpaceLine.CODEC);
		map.put("text", Text.CODEC);
		return map;
	});
	public static final Codec<UnbakedLine> CODEC=Codec.STRING.fieldOf("type").dispatch(UnbakedLine::type, codecs::get);

	Line bake(int width);
	String type();
}
