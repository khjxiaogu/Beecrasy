package com.khjxiaogu.beecrasy.client.screens.sequencertabs;

import net.minecraft.client.input.MouseButtonEvent;

public interface InteractiveLine extends Line {
	public boolean mouseClicked(int mouseX, int mouseY,GeneticModifier ah1,GeneticModifier ah2,MouseButtonEvent event, boolean doubleClick);

	public static boolean isMouseIn(int mouseX, int mouseY, int x, int y, int w, int h) {
		return mouseX >= x && mouseY >= y && mouseX < x + w && mouseY < y + h;
	}

}
