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

package com.khjxiaogu.beecrasy.client.apistle;

import java.util.ArrayList;
import java.util.List;

import com.khjxiaogu.beecrasy.client.apistle.ApistleScreen.ItemAndArea;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class GuiInfoCollector {
	List<ItemAndArea> stacks=new ArrayList<>();
	List<Component> tooltips=new ArrayList<>();
	public void accept(Component comp) {
		this.tooltips.add(comp);
	}
	public void accept(ItemStack item,int x,int y,int w,int h) {
		this.stacks.add(new ItemAndArea(item,x,y,w,h));
	}
}
