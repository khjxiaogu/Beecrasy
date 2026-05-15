package com.khjxiaogu.beecrasy.utils;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public final class Utils {
	public static ItemStackTemplate getRecipeOutput(List<ItemStack> stacks,CraftingRecipe recipe) {
		return switch(recipe) {
		case ShapedRecipe sr->sr.result;
		case ShapelessRecipe sr->sr.result;
		default->{
			CraftingInput ipt=CraftingInput.of(3, 3, stacks);
			ItemStack is=recipe.assemble(ipt);
			if(is!=null&&!is.isEmpty()) {
				yield new ItemStackTemplate(is.getItem(),is.getCount(),is.getComponentsPatch());
			}
			yield null;
		}
		};
	}
}
