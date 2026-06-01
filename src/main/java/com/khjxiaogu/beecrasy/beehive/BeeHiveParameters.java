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

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterRegistry.BeehiveParameterType;

import net.minecraft.network.chat.Component;

public class BeeHiveParameters {
	public static final BeehiveParameterType<Float> SPEED=BeeHiveParameterRegistry.registerNumeric(Beecrasy.rl("speed"),1f, (value,adder)->adder.accept(Component.translatable(Beecrasy.rl("speed").toLanguageKey("argument.beehive"),(value>0?"+":"")+(int)(value*100)+"%")));
	public static final BeehiveParameterType<Float> MUTATE=BeeHiveParameterRegistry.registerNumeric(Beecrasy.rl("mutate"),1f, (value,adder)->adder.accept(Component.translatable(Beecrasy.rl("mutate").toLanguageKey("argument.beehive"),(value>0?"+":"")+(int)(value*100)+"%")));
	
	private BeeHiveParameters() {}
}
