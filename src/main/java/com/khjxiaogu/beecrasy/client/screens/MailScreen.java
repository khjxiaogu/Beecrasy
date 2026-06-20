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

import java.util.ArrayList;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.mail.LetterStatus;
import com.khjxiaogu.beecrasy.menu.MailMenu;
import com.khjxiaogu.beecrasy.menu.SkepMenu;
import com.khjxiaogu.beecrasy.network.MailEditMessage;
import com.khjxiaogu.beecrasy.network.PacketHandler;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;


public class MailScreen extends AbstractContainerScreen<MailMenu> {
	/** GUI 背景纹理位置 */
	static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Beecrasy.MODID, "textures/gui/correspondence.png");

	public MailScreen(MailMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);

        this.inventoryLabelY = imageHeight - 92;
	}
	private ArrayList<Component> tooltip = new ArrayList<>(2);

	EditBox name;
	EditBox text1;

	EditBox text2;
	/**
	 * 初始化界面——创建工作模式切换按钮。
	 * <p>
	 * 按钮点击时调用 {@link SkepMenu#cycleWork()} 切换工作模式。
	 */
	@Override
	public void init() {
		super.init();
		this.clearWidgets();

		this.addRenderableWidget(name  = new EditBox(this.font,leftPos + 38,topPos + 7,50,8,Component.empty()));
		this.addRenderableWidget(text1 = new EditBox(this.font,leftPos + 38,topPos + 21,98,8,Component.empty()));
		this.addRenderableWidget(text2 = new EditBox(this.font,leftPos + 38,topPos + 35,98,8,Component.empty()));
		name.setTextColor(0xff5a5653);
		name.setTextColorUneditable(0xff5a5653);
		name.setMaxLength(255);
		name.setBordered(false);
		name.setTextShadow(false);
		text1.setTextColor(0xff5a5653);
		text1.setTextColorUneditable(0xff5a5653);
		text1.setMaxLength(255);
		text1.setBordered(false);
		text1.setTextShadow(false);
		text2.setTextColor(0xff5a5653);
		text2.setTextColorUneditable(0xff5a5653);
		text2.setMaxLength(255);
		text2.setBordered(false);
		text2.setTextShadow(false);
		
		text1.setValue(menu.line1);
		text2.setValue(menu.line2);
		if(menu.readOnly) {
			name.setValue(menu.sender);
			name.setEditable(false);
			text1.setEditable(false);
			text2.setEditable(false);
		}else {

			name.setValue(menu.receiver);
		}
		name.setResponder(t->{
			PacketHandler.sendToServer(MailEditMessage.ofReceiver(this.menu.containerId, t));
		});
		text1.setResponder(t->{
			PacketHandler.sendToServer(MailEditMessage.ofLine1(this.menu.containerId, t));
		});
		text2.setResponder(t->{
			PacketHandler.sendToServer(MailEditMessage.ofLine2(this.menu.containerId, t));
		});

	}

	/**
	 * 提取渲染状态——处理错误码提示。
	 * <p>
	 * 判断鼠标是否悬停在错误码图标上，
	 * 收集对应的提示文本并通过
	 * {@link GuiGraphicsExtractor#setComponentTooltipForNextFrame} 提交。
	 *
	 * @param transform GUI 图形提取器
	 * @param mouseX    鼠标 X 坐标
	 * @param mouseY    鼠标 Y 坐标
	 * @param partial   部分 tick 时间
	 */
	@Override
	public void extractRenderState(GuiGraphicsExtractor transform, int mouseX, int mouseY, float partial) {
		tooltip.clear();
		
		super.extractRenderState(transform, mouseX, mouseY, partial);
		LetterStatus errCode=menu.getStatus();
		if(errCode!=LetterStatus.OK) {
			if(isMouseIn(mouseX,mouseY,1,3,19,19))
				tooltip.add(errCode.text);
		}
		if (!tooltip.isEmpty()) {
			transform.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
		}

	}

	@Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
        //graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, -12566464, false);
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
    }
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            this.minecraft.player.closeContainer();
            return true;
        }
		if(this.getFocused() instanceof EditBox name) {
			name.keyPressed(event);
			if(name.canConsumeInput())
				return true;
		}
    	return super.keyPressed(event);
    }
	/**
	 * 提取背景——绘制主纹理、错误码图标
	 *
	 * @param graphics GUI 图形提取器
	 * @param mouseX   鼠标 X 坐标
	 * @param mouseY   鼠标 Y 坐标
	 * @param a        部分 tick 时间
	 */
	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight,256,256);
		if(!menu.readOnly) {
			LetterStatus errCode=menu.getStatus();
			if(errCode!=LetterStatus.OK)
				graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos+1, topPos+3,176+(errCode.ordinal()%4)*19, 0+(errCode.ordinal()/4)*19, 19, 19,256,256);
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos+24, topPos+6,176, 19, 11, 11,256,256);
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos+24, topPos+26,176+11*2, 19, 11, 11,256,256);
		}else {
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos+24, topPos+6,176+11, 19, 11, 11,256,256);
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos+24, topPos+26,176+11*3, 19, 11, 11,256,256);
		}
	}

	public boolean isMouseIn(int mouseX, int mouseY, int x, int y, int w, int h) {
		return mouseX >= leftPos + x && mouseY >= topPos + y && mouseX < leftPos + x + w && mouseY < topPos + y + h;
	}

}
