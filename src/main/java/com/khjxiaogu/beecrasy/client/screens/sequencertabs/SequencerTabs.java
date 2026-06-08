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

package com.khjxiaogu.beecrasy.client.screens.sequencertabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.khjxiaogu.beecrasy.events.SequencerTabRegistryEvent;

import net.neoforged.neoforge.common.NeoForge;

/**
 * 测序仪标签页注册中心。
 * <p>
 * 在初始化时注册内置标签页（{@link BasicGeneticsTab}、{@link ProductsTab}），
 * 并通过 NeoForge 事件总线发送 {@link SequencerTabRegistryEvent}，
 * 允许其他模组动态注册自定义标签页。
 * 标签页列表在初始化后变为不可变集合。
 */
public class SequencerTabs {
	/** 最终的标签页列表（初始化后不可变） */
	private static List<SequencerTab> tabs;
	/**
	 * 初始化标签页集合。
	 * <p>
	 * 先添加内置标签页，然后广播 {@link SequencerTabRegistryEvent} 让其他模组
	 * 注册自定义标签页，最后包装为不可变列表。
	 */
	public static void init() {
		List<SequencerTab> tabs=new ArrayList<>();
		tabs.add(new BasicGeneticsTab());
		tabs.add(new ProductsTab());
		SequencerTabRegistryEvent ev=new SequencerTabRegistryEvent(tabs::add);
		NeoForge.EVENT_BUS.post(ev);
		SequencerTabs.tabs=Collections.unmodifiableList(tabs);
	}
	/**
	 * 获取不可变的标签页列表。
	 *
	 * @return 所有已注册的 {@link SequencerTab} 实例列表
	 */
	public static List<SequencerTab> getTabs(){
		return tabs;
	}
}
