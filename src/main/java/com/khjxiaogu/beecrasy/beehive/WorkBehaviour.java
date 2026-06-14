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

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 蜂巢工作模式枚举。
 * 定义了蜂巢的三种工作控制方式：手动（需玩家放入蜂后）、自动（持续工作）和红石控制（由红石信号驱动）。
 */
public enum WorkBehaviour{
	/** 手动模式：需要玩家将蜂后手动放入蜂巢槽位才会开始工作。 */
	MAUNAL,
	/** 自动模式：蜂巢会自动持续进行工作周期。 */
	AUTO,
	/** 红石模式：仅在接收到红石信号时进行工作。 */
	REDSTONE;
	/** 翻译键，格式为 "gui.beehive.control.<枚举名小写>"。 */
	private final String key="gui.beehive.control."+this.name().toLowerCase();
	/** 缓存的翻译文本组件。 */
	private final Component text=Component.translatable(key);
	/** 使用整数序数进行序列化的编解码器。 */
	public static final Codec<WorkBehaviour> CODEC=Codec.INT.xmap(i->WorkBehaviour.values()[i], WorkBehaviour::ordinal);
	/** 用于网络数据包传输的流式编解码器。 */
	public static final StreamCodec<ByteBuf,WorkBehaviour> STREAM_CODEC=ByteBufCodecs.VAR_INT.map(i->WorkBehaviour.values()[i], WorkBehaviour::ordinal);
	/**
	 * 获取该工作模式对应的可翻译文本组件。
	 * @return 用于 GUI 显示的文本组件
	 */
	public Component getComponents() {
		return text;
	}
	/**
	 * 获取该工作模式的翻译键。
	 * @return 格式为 "gui.beehive.control.<name>" 的翻译键字符串
	 */
	public String getTranslationKey() {
		return key;
	}
}