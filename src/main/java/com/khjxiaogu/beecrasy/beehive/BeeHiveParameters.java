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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterRegistry.BeehiveParameterType;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

public class BeeHiveParameters {
	public static class Formatters{
		NumberFormat number=new DecimalFormat("+0.0;-0.0");
		NumberFormat percent=new DecimalFormat("+0%;-0%");
		public String formatPercentage(float value) {
			return percent.format(value);
		}
		public String formatNumber(float value) {
			return number.format(value);
		}
	}
	public static final BeehiveParameterType<Float> SPEED=BeeHiveParameterRegistry.registerNumeric(Beecrasy.rl("speed"),1f, (value,adder)->adder.accept(formatPercentageLanguage(Beecrasy.rl("speed"),value)));
	public static final BeehiveParameterType<Float> MUTATE=BeeHiveParameterRegistry.registerNumeric(Beecrasy.rl("mutate"),1f, (value,adder)->adder.accept(formatPercentageLanguage(Beecrasy.rl("mutate"),value)));
	public static final BeehiveParameterType<Float> LIFESPAN=BeeHiveParameterRegistry.registerNumeric(Beecrasy.rl("lifespan"),1f, (value,adder)->adder.accept(formatPercentageLanguage(Beecrasy.rl("lifespan"),value)));
	public static final BeehiveParameterType<Float> YIELD=BeeHiveParameterRegistry.registerNumeric(Beecrasy.rl("yield"),1f, (value,adder)->adder.accept(formatPercentageLanguage(Beecrasy.rl("yield"),value)));
	public static final BeehiveParameterType<Float> TEMPERATURE=BeeHiveParameterRegistry.registerNumeric(Beecrasy.rl("temperature"),0f, (value,adder)->adder.accept(formatNumberLanguage(Beecrasy.rl("temperature"),value)));
	public static final BeehiveParameterType<Float> HUMIDITY=BeeHiveParameterRegistry.registerNumeric(Beecrasy.rl("humidity"),0f, (value,adder)->adder.accept(formatNumberLanguage(Beecrasy.rl("humidity"),value)));
	
	private BeeHiveParameters() {}
	
	public static final ThreadLocal<Formatters> formatter=ThreadLocal.withInitial(Formatters::new);

	public static MutableComponent formatPercentageLanguage(Identifier id,float value) {
		return Component.translatable(getLanguageKey(id),formatter.get().formatPercentage(value));
	}
	public static MutableComponent formatNumberLanguage(Identifier id,float value) {
		return Component.translatable(getLanguageKey(id),formatter.get().formatNumber(value));
	}
	public static String getLanguageKey(Identifier id) {
		return id.toLanguageKey("argument.beehive");
	}
}
