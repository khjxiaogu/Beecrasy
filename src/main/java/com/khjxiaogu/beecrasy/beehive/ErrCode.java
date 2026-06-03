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

public enum ErrCode{
	OK,//无错误
	MANUAL_HALT,//手动模式下，需要手动放入蜂后
	MISSING_QUEEN,//无蜂后
	EXTRA_QUEEN,//蜂后过多
	MISSING_DRONE,//无雄蜂
	MALFORMED_SLOT,//蜂巢格子有非雄蜂/蜂后
	NO_FLOWER,//无花
	NO_BIOTOPE,//部分蜜蜂生境不符（警告）
	EMPTY_QUEEN,//王台为空
	INVALID_ENVIRONMENT//环境不适宜
	;
	private final String key="gui.beehive.status."+this.name().toLowerCase();
	private final Component text=Component.translatable(key);
	public static final Codec<ErrCode> CODEC=Codec.INT.xmap(i->ErrCode.values()[i], ErrCode::ordinal);
	public Component getComponents() {
		return text;
	}
	public String getTranslationKey() {
		return key;
	}
}