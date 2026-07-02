package com.khjxiaogu.beecrasy.client.apistle;

import java.util.ArrayList;
import java.util.List;

import com.khjxiaogu.beecrasy.client.apistle.ApistleScreen.ItemAndArea;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class GuiInfoCollector {
	List<ItemAndArea> stacks=new ArrayList<>();
	List<Component> tooltips=new ArrayList<>();
	public void accept(Component comp) {
		this.tooltips.add(comp);
	}
	public void accept(ItemStack item,int x,int y,int w,int h) {
		this.stacks.add(new ItemAndArea(item,x,y,w,h));
	}
}
