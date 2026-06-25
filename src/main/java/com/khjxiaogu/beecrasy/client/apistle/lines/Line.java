package com.khjxiaogu.beecrasy.client.apistle.lines;

import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public interface Line {
	
	/**
	 * 渲染基因信息行。
	 *
	 * @param graphics GUI 图形提取器
	 * @param x        行左上角 X 坐标
	 * @param y        行左上角 Y 坐标
	 * @param mouseX   鼠标相对 X 坐标
	 * @param mouseY   鼠标相对 Y 坐标
	 * @param tooltips 提示文本消费者，用于收集悬停时显示的提示信息
	 * @return 该行占用的高度（像素），用于下一行的 Y 坐标偏移
	 */
	int extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int w, int mouseX, int mouseY, Consumer<Component> tooltips);

	int precalculateHeight();
}