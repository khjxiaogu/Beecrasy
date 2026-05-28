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

import java.util.function.IntSupplier;

import com.khjxiaogu.beecrasy.Beecrasy;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

public class BeeHiveButton extends Button {
	Identifier texture;
	IntSupplier slot;
	boolean isOver;
	public static MutableComponent redstone = Component.translatable("gui." + Beecrasy.MODID + ".beehive.redstone");
	public static MutableComponent manual = Component.translatable("gui." + Beecrasy.MODID + ".beehive.redstone");
	public static MutableComponent aut = Component.translatable("gui." + Beecrasy.MODID + ".beehive.redstone");
	public BeeHiveButton(Builder builder, Identifier texture, IntSupplier slot) {
		super(builder);
		this.texture = texture;
		this.slot = slot;
	}

	public boolean isOver() {
		return isOver;
	}

	public void setOver(boolean isOver) {
		this.isOver = isOver;
	}

	@Override
	public Component getMessage() {
		return switch(slot.getAsInt()) {
		case 0->manual;
		case 1->aut;
		case 2->redstone;
		default->manual;
		};
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, super.getX(), super.getY(),
				181+18*slot.getAsInt(), isOver?16:0, 18, 16, 256, 256);

	}

}
