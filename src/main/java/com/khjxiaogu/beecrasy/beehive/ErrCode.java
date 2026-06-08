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

import com.mojang.serialization.Codec;

import net.minecraft.network.chat.Component;

/**
 * 蜂巢工作状态错误码枚举。
 * 用于在 GUI 上显示蜂巢的当前工作状态，以及在工作流程中进行条件判断。
 * 每个枚举常量对应一种特定的状态或错误条件。
 */
public enum ErrCode{
	/** 无错误，一切正常。 */
	OK,
	/** 手动模式下，需要玩家手动放入蜂后方可开始工作。 */
	MANUAL_HALT,
	/** 蜂巢内没有蜂后，无法开始工作周期。 */
	MISSING_QUEEN,
	/** 蜂巢内存在多个蜂后，不符合要求。 */
	EXTRA_QUEEN,
	/** 缺少雄蜂或雄蜂数量不足。 */
	MISSING_DRONE,
	/** 蜂巢槽位中含有非雄蜂/非蜂后的物品，格式不正确。 */
	MALFORMED_SLOT,
	/** 附近没有花，蜜蜂无法采蜜/授粉。 */
	NO_FLOWER,
	/** 部分蜜蜂的生境（生物群系等）与当前环境不匹配（警告级别）。 */
	NO_BIOTOPE,
	/** 王台（空的蜂后槽位）为空，无法产出新的蜂后。 */
	EMPTY_QUEEN,
	/** 当前环境完全不适宜蜜蜂生存或工作。 */
	INVALID_ENVIRONMENT
	;
	/** 翻译键，格式为 "gui.beehive.status.<枚举名小写>"。 */
	private final String key="gui.beehive.status."+this.name().toLowerCase();
	/** 缓存的翻译文本组件。 */
	private final Component text=Component.translatable(key);
	/** 使用整数序数进行序列化的编解码器。 */
	public static final Codec<ErrCode> CODEC=Codec.INT.xmap(i->ErrCode.values()[i], ErrCode::ordinal);
	/**
	 * 获取该错误码对应的可翻译文本组件。
	 * @return 用于 GUI 显示的文本组件
	 */
	public Component getComponents() {
		return text;
	}
	/**
	 * 获取该错误码的翻译键。
	 * @return 格式为 "gui.beehive.status.<name>" 的翻译键字符串
	 */
	public String getTranslationKey() {
		return key;
	}
}