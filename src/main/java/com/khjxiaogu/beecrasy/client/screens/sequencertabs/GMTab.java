package com.khjxiaogu.beecrasy.client.screens.sequencertabs;

import java.util.function.Consumer;

import com.khjxiaogu.beecrasy.menu.GMMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public interface GMTab {
	public boolean mouseClicked(int mouseX, int mouseY,int x, int y, int w, int h,GMMenu menu,MouseButtonEvent event, boolean doubleClick);
	/**
	 * 渲染标签页的内容区域。
	 *
	 * @param graphics GUI 图形提取器
	 * @param menu     测序仪菜单
	 * @param x        内容区域 X 坐标
	 * @param y        内容区域 Y 坐标
	 * @param w        内容区域宽度
	 * @param h        内容区域高度
	 * @param mouseX   鼠标 X 坐标
	 * @param mouseY   鼠标 Y 坐标
	 * @param partial  部分 tick 时间
	 * @param tooltips 提示文本消费者
	 */
	public void extractRenderState(GuiGraphicsExtractor graphics,GMMenu menu,int x,int y,int w,int h,int mouseX, int mouseY, float partial,Consumer<Component> tooltips);

	/**
	 * 渲染标签页的背景区域。
	 *
	 * @param graphics GUI 图形提取器
	 * @param menu     测序仪菜单
	 * @param x        背景区域 X 坐标
	 * @param y        背景区域 Y 坐标
	 * @param w        背景区域宽度
	 * @param h        背景区域高度
	 * @param mouseX   鼠标 X 坐标
	 * @param mouseY   鼠标 Y 坐标
	 * @param partial  部分 tick 时间
	 */
	public void extractBackground(GuiGraphicsExtractor graphics,GMMenu menu,int x,int y,int w,int h, int mouseX, int mouseY, float partial);
	
	/**
	 * 渲染左侧的标签按钮。
	 *
	 * @param graphics GUI 图形提取器
	 * @param menu     测序仪菜单
	 * @param x        按钮 X 坐标
	 * @param y        按钮 Y 坐标
	 * @param w        按钮宽度
	 * @param h        按钮高度
	 * @param isOver   鼠标是否悬停
	 * @param isActive 是否是当前选中标签
	 */
	public void extractButton(GuiGraphicsExtractor graphics,GMMenu menu,int x,int y,int w,int h,boolean isOver,boolean isActive);

	/**
	 * 添加按钮的悬停提示。
	 *
	 * @param menu     测序仪菜单
	 * @param tooltips 提示文本消费者
	 */
	public void addButtonTooltip(GMMenu menu,Consumer<Component> tooltips);
	public static boolean isMouseIn(int mouseX, int mouseY, int x, int y, int w, int h) {
		return mouseX >= x && mouseY >= y && mouseX < x + w && mouseY < y + h;
	}
}
