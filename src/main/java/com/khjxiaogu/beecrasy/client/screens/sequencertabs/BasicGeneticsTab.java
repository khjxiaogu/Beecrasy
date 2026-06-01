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

import com.khjxiaogu.beecrasy.client.screens.SequencerScreen;
import com.khjxiaogu.beecrasy.menu.SequencerMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public class BasicGeneticsTab implements SequencerTab {

	@Override
	public void extractRenderState(GuiGraphicsExtractor transform, SequencerMenu menu, int x, int y, int w, int h, int mouseX, int mouseY, float partial, Consumer<Component> tooltips) {
		
		
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, SequencerMenu menu, int x, int y, int w, int h, int mouseX, int mouseY, float partial) {
		
	}

	@Override
	public void extractButton(GuiGraphicsExtractor graphics, SequencerMenu menu, int x, int y, int w, int h, boolean isActive) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, SequencerScreen.TEXTURE, x, y, 176+(isActive?23:0), 0, w, h,256,256);
	}

	@Override
	public void addButtonTooltip(SequencerMenu menu, Consumer<Component> tooltips) {
		tooltips.accept(Component.translatable("tab.sequencer.beecrasy.basic"));
		
	}

}
