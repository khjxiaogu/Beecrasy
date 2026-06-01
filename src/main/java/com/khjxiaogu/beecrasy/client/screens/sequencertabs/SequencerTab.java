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

import java.util.function.Consumer;

import com.khjxiaogu.beecrasy.menu.SequencerMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public interface SequencerTab {
	
	public void extractRenderState(GuiGraphicsExtractor transform,SequencerMenu menu,int x,int y,int w,int h,int mouseX, int mouseY, float partial,Consumer<Component> tooltips);


	public void extractBackground(GuiGraphicsExtractor graphics,SequencerMenu menu,int x,int y,int w,int h, int mouseX, int mouseY, float partial);
	
	public void extractButton(GuiGraphicsExtractor graphics,SequencerMenu menu,int x,int y,int w,int h,boolean isActive);
	public void addButtonTooltip(SequencerMenu menu,Consumer<Component> tooltips);

}
