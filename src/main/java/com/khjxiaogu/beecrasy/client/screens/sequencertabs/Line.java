package com.khjxiaogu.beecrasy.client.screens.sequencertabs;

import java.util.function.Consumer;

import com.khjxiaogu.beecrasy.genome.AllelesHolder;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public interface Line {

	int extractRenderState(GuiGraphicsExtractor graphics, AllelesHolder ah1, AllelesHolder ah2, int x, int y, int mouseX, int mouseY, Consumer<Component> tooltips);


}