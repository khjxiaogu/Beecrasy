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

/**
 * 蜂巢工作参数类型常量定义。
 * 定义了所有与蜂巢工作相关的参数类型，包括速度、突变率、寿命、产量、温度和湿度。
 * 每个参数类型通过 {@link BeeHiveParameterRegistry} 注册，并关联其格式化显示方式。
 */
public class BeeHiveParameters {
	/**
	 * 参数格式化器内部类。
	 * 提供了数值和百分比的格式化方法，用于将参数值转换为用户可读的字符串，如 "+0.0" 或 "+0%"。
	 */
	public static class Formatters{
		/** 数字格式，例如 "+0.0;-0.0"。 */
		NumberFormat number=new DecimalFormat("+0.0;-0.0");
		/** 百分比格式，例如 "+0%;-0%"。 */
		NumberFormat percent=new DecimalFormat("+0%;-0%");
		/**
		 * 将浮点数值格式化为百分比字符串。
		 * @param value 浮点数值
		 * @return 格式化后的百分比字符串
		 */
		public String formatPercentage(float value) {
			return percent.format(value);
		}
		/**
		 * 将浮点数值格式化为数字字符串。
		 * @param value 浮点数值
		 * @return 格式化后的数字字符串
		 */
		public String formatNumber(float value) {
			return number.format(value);
		}
	}
	/** 工作速度参数：影响蜂巢工作周期的推进速度。默认值为 1.0，加法合并。 */
	public static final BeehiveParameterType<Float> SPEED=BeeHiveParameterRegistry.registerNumeric(Beecrasy.rl("speed"),1f, (value,adder)->adder.accept(formatPercentageLanguage(Beecrasy.rl("speed"),value)));
	/** 突变率参数：影响蜜蜂杂交时产生突变的概率。默认值为 1.0，加法合并。 */
	public static final BeehiveParameterType<Float> MUTATE=BeeHiveParameterRegistry.registerNumeric(Beecrasy.rl("mutate"),1f, (value,adder)->adder.accept(formatPercentageLanguage(Beecrasy.rl("mutate"),value)));
	/** 寿命参数：影响蜂后的生命周期长度。默认值为 1.0，加法合并。 */
	public static final BeehiveParameterType<Float> LIFESPAN=BeeHiveParameterRegistry.registerNumeric(Beecrasy.rl("lifespan"),1f, (value,adder)->adder.accept(formatPercentageLanguage(Beecrasy.rl("lifespan"),value)));
	/** 产量参数：影响巢脾的产物生成效率。默认值为 1.0，加法合并。 */
	public static final BeehiveParameterType<Float> YIELD=BeeHiveParameterRegistry.registerNumeric(Beecrasy.rl("yield"),1f, (value,adder)->adder.accept(formatPercentageLanguage(Beecrasy.rl("yield"),value)));
	/** 温度参数：环境温度的量化值。默认值为 0.0，加法合并。 */
	public static final BeehiveParameterType<Float> TEMPERATURE=BeeHiveParameterRegistry.registerNumeric(Beecrasy.rl("temperature"),0f, (value,adder)->adder.accept(formatNumberLanguage(Beecrasy.rl("temperature"),value)));
	/** 湿度参数：环境湿度的量化值。默认值为 0.0，加法合并。 */
	public static final BeehiveParameterType<Float> HUMIDITY=BeeHiveParameterRegistry.registerNumeric(Beecrasy.rl("humidity"),0f, (value,adder)->adder.accept(formatNumberLanguage(Beecrasy.rl("humidity"),value)));
	
	private BeeHiveParameters() {}
	
	/**
	 * 线程本地的格式化器实例，避免多线程冲突。
	 */
	public static final ThreadLocal<Formatters> formatter=ThreadLocal.withInitial(Formatters::new);

	/**
	 * 使用百分比格式创建参数的可翻译文本组件。
	 * @param id    参数标识符（ResourceLocation）
	 * @param value 参数浮点数值
	 * @return 可翻译的文本组件
	 */
	public static MutableComponent formatPercentageLanguage(Identifier id,float value) {
		return Component.translatable(getLanguageKey(id),formatter.get().formatPercentage(value));
	}
	/**
	 * 使用数字格式创建参数的可翻译文本组件。
	 * @param id    参数标识符（ResourceLocation）
	 * @param value 参数浮点数值
	 * @return 可翻译的文本组件
	 */
	public static MutableComponent formatNumberLanguage(Identifier id,float value) {
		return Component.translatable(getLanguageKey(id),formatter.get().formatNumber(value));
	}
	/**
	 * 获取参数标识符对应的语言键。
	 * 格式为 "argument.beehive.<namespace>.<path>"。
	 * @param id 参数标识符
	 * @return 语言键字符串
	 */
	public static String getLanguageKey(Identifier id) {
		return id.toLanguageKey("argument.beehive");
	}
}
