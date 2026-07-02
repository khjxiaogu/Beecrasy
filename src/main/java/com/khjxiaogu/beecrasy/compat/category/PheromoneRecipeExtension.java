/*
 *
 * Copyright (C) 2026 khjxiaogu
 *
 * This file is part of Beecrasy.
 *
 * Beecrasy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Beecrasy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beecrasy. If not, see <https://www.gnu.org/licenses/>.
 */

package com.khjxiaogu.beecrasy.compat.category;

import java.util.List;
import java.util.Optional;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.beehive.BeeHiveParameters;
import com.khjxiaogu.beecrasy.components.BeeHiveArgumentation;
import com.khjxiaogu.beecrasy.components.BeehiveArgumenter;
import com.khjxiaogu.beecrasy.data.recipe.PheromoneRecipe;

import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public class PheromoneRecipeExtension implements ICraftingCategoryExtension<PheromoneRecipe> {


	@Override
	public List<SlotDisplay> getIngredients(RecipeHolder<PheromoneRecipe> recipeHolder) {
		List<RecipeDisplay> displays = recipeHolder.value().display();
		if (displays.isEmpty()) {
			return List.of();
		}
		RecipeDisplay display = displays.getFirst();
		if (display instanceof ShapelessCraftingRecipeDisplay shapelessCraftingRecipeDisplay) {
			return shapelessCraftingRecipeDisplay.ingredients();
		} else {
			return List.of();
		}
	}

	@Override
	public void onDisplayedIngredientsUpdate(RecipeHolder<PheromoneRecipe> recipeHolder,
			List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses) {
		ItemStack out=null;
		for(IRecipeSlotDrawable irsd:recipeSlots) {
			if(irsd.getRole()==RecipeIngredientRole.INPUT) {
				Optional<ItemStack> ois=irsd.getDisplayedItemStack();
				if(ois.isPresent()) {
					ItemStack curOut=ois.get();
					if(!curOut.is(Items.PHEROMONO)) {
						out=curOut;
					}
				}
			}
		}
		if(out==null)
			out=Items.PHEROMONO.toStack();
		ItemStack output=Items.PHEROMONO.toStack();
		output.set(Components.ARGUMENTATION, new BeehiveArgumenter(new BeeHiveArgumentation.Builder().addParam(BeeHiveParameters.MUTATION_DIRECTOR,List.of(out.getItem())).build(),true));
		for(IRecipeSlotDrawable irsd:recipeSlots) {
			if(irsd.getRole()==RecipeIngredientRole.OUTPUT) {
				Optional<ItemStack> ois=irsd.getDisplayedItemStack();
				if(ois.isPresent()) {
					ItemStack curOut=ois.get();
					if(curOut.is(Items.PHEROMONO)) {
						irsd.createDisplayOverrides().add(output);
					}
				}
			}
		}
	}


}
