package com.khjxiaogu.beecrasy.client.apistle;

import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public interface UnbakedPage {
	BakedPage bake(int width);
	int order();
	void extractIcon(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int mouseX, int mouseY, Consumer<Component> tooltips) ;
	
}
