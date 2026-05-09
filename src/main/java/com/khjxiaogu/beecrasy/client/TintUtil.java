package com.khjxiaogu.beecrasy.client;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.sprite.Material.Baked;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class TintUtil {

	private TintUtil() {
	}
	public static int getTint(ItemStack tintStack) {
		Identifier modelId = tintStack.get(DataComponents.ITEM_MODEL);
		if (modelId != null) {
			Minecraft minecraft=Minecraft.getInstance();
			ItemStackRenderState layer=new ItemStackRenderState();
			minecraft.getModelManager().getItemModel(modelId)
			.update(layer, tintStack, minecraft.getItemModelResolver(), ItemDisplayContext.GUI, minecraft.level, minecraft.player, 0);
			Baked material=layer.pickParticleMaterial(minecraft.level.getRandom());
			NativeImage ni=material.sprite().contents().getOriginalImage();
			if(!ni.isClosed()) {//some optimization mod deleted native image, fall back
				return ni.getPixel(0, 0);
			}
			
		}
		return 0xFFFFFFFF;
	}

}
