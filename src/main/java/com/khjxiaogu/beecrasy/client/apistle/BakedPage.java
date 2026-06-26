package com.khjxiaogu.beecrasy.client.apistle;

import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public interface BakedPage {
	void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int w, int h,int viewY,int viewHeight, int mouseX, int mouseY, Consumer<Component> tooltips);
	int height();
	default void extractBackground(GuiGraphicsExtractor graphics, int x, int y, int w, int h,int viewY,int viewHeight, int mouseX, int mouseY) {};
	
}
