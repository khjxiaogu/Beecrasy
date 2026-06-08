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

import com.khjxiaogu.beecrasy.menu.SequencerMenuHandHeld;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 手持测序仪 GUI 界面。
 * <p>
 * 继承 {@link SequencerScreen}<{@link SequencerMenuHandHeld}>，是测序仪的手持版本。
 * 与方块版本相比，不显示流体储罐和能量条，仅额外绘制底栏背景区域。
 */
public class SequenceHandHeldScreen extends SequencerScreen<SequencerMenuHandHeld> {

	/**
	 * 构造手持测序仪 GUI。
	 *
	 * @param menu      手持测序仪菜单
	 * @param inventory 玩家物品栏
	 * @param title     界面标题
	 */
	public SequenceHandHeldScreen(SequencerMenuHandHeld menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos+142, topPos+97, 176, 60, 32, 30,256,256);
		
	}

	

}
