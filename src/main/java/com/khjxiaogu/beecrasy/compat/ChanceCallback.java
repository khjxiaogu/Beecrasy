package com.khjxiaogu.beecrasy.compat;

import com.khjxiaogu.beecrasy.data.PossibleOutput;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.network.chat.Component;

public class ChanceCallback implements IRecipeSlotRichTooltipCallback {
	PossibleOutput po;
	public static String titleId="jei.tooltip.beecrasy.chance";
	public ChanceCallback(PossibleOutput po) {
		super();
		this.po = po;
	}

	@Override
	public void onRichTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip) {
		if (po.chance() !=1)
			tooltip.add(Component.translatable(titleId,po.chance()));
	}

}