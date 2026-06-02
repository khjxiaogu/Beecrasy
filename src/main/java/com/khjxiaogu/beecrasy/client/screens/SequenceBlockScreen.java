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

package com.khjxiaogu.beecrasy.client.screens;

import java.util.function.Consumer;

import com.khjxiaogu.beecrasy.blocks.SequencerBlockEntity;
import com.khjxiaogu.beecrasy.client.FluidRenderHelper;
import com.khjxiaogu.beecrasy.menu.SequencerMenuBlock;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SequenceBlockScreen extends SequencerScreen<SequencerMenuBlock> {

	public SequenceBlockScreen(SequencerMenuBlock menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos+142, topPos+85, 176, 90, 32, 42,256,256);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos+11, topPos+113, (menu.getEnergy()>=menu.getWorkEnergy()?199:176), 45, 12, 8,256,256);
		
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial,Consumer<Component> adder) {
		FluidRenderHelper.handleGuiTank(graphics, menu, 0, leftPos+152, topPos+89, 16, 34, mouseX, mouseY, adder);
		if(super.isMouseIn(mouseX, mouseY, 11, 113, 12, 8)) {
			adder.accept(Component.literal(menu.getEnergy()+"/"+(menu.getWorkEnergy()*SequencerBlockEntity.ENERGY_BUFF)+" FE"));
		}
	}

	

}
