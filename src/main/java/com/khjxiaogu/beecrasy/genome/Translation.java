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

import net.minecraft.network.chat.Component;

public record Translation(String key,Component component,String shortKey,Component shortComponent){
	public static final Translation MISSING=new Translation("missing",Component.literal("ERROR"),"missing",Component.literal("ERROR"));
	public Translation(String key) {
		this(key,Component.translatable(key),key+".short",Component.translatable(key+".short"));
	}
}