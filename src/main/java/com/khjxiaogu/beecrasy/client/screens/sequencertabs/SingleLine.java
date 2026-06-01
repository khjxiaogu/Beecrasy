package com.khjxiaogu.beecrasy.client.screens.sequencertabs;

import java.util.function.Consumer;

import com.khjxiaogu.beecrasy.genome.AllelesHolder;
import com.khjxiaogu.beecrasy.genome.Gene;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public record SingleLine(Gene<?> gene) implements Line {
	@Override
	public int extractRenderState(GuiGraphicsExtractor graphics,AllelesHolder ah1,AllelesHolder ah2,int x,int y,int mouseX, int mouseY, Consumer<Component> tooltips) {
		graphics.text(Minecraft.getInstance().font, gene.getShortReadableText(), x, y, 0x81cfff);
		graphics.text(Minecraft.getInstance().font, gene.getShortReadableText(ah1), x+42, y, 0xf18186);
		if(ah2!=null)
			graphics.text(Minecraft.getInstance().font, gene.getShortReadableText(ah2), x+67, y, 0xb45ba4);
		if(mouseY<8&&mouseY>0) {
			if(mouseX<42) {
				tooltips.accept(gene.getReadableText());
			}else if(mouseX<67) {
				tooltips.accept(gene.getReadableText(ah1));
			}else if(ah2!=null) {
				tooltips.accept(gene.getReadableText(ah2));
			}
		}
		return 8;
	}
}