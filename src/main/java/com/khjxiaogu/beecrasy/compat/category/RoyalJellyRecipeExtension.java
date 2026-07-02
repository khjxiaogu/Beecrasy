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
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.data.recipe.RoyalJellyRecipe;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public class RoyalJellyRecipeExtension implements ICraftingCategoryExtension<RoyalJellyRecipe> {


	@Override
	public void setRecipe(RecipeHolder<RoyalJellyRecipe> recipeHolder, IRecipeLayoutBuilder builder,
			ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		RoyalJellyRecipe recipe = recipeHolder.value();
		RecipeDisplay display = recipe.display().getFirst();
		SlotDisplay resultItem = display.result();
		craftingGridHelper.createAndSetOutputs(builder, resultItem);

		List<Ingredient> ingredients = recipe.ingredients;
		int width = getWidth(recipeHolder);
		int height = getHeight(recipeHolder);
		craftingGridHelper.createAndSetIngredients(builder, ingredients, width, height);
	}

	@Override
	public List<SlotDisplay> getIngredients(RecipeHolder<RoyalJellyRecipe> recipeHolder) {
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
	public void onDisplayedIngredientsUpdate(RecipeHolder<RoyalJellyRecipe> recipeHolder,
			List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses) {
		GenomeComponent out=null;
		for(IFocus<ItemStack> irsd:(Iterable<IFocus<ItemStack>>)()->focuses.getItemStackFocuses(RecipeIngredientRole.INPUT).iterator()) {
					
			Optional<ItemStack> ois=irsd.getTypedValue().getItemStack();
			if(ois.isPresent()) {
				ItemStack curOut=ois.get();
				if(curOut.is(Items.LARVA)) {
					out=curOut.get(Components.GENOME);
				}
			}
			
		}
		if(out!=null) {
			ItemStack output=Items.QUEEN_BEE.toStack();
			output.set(Components.GENOME, out);

			ItemStack input=Items.LARVA.toStack();
			input.set(Components.GENOME, out);
			for(IRecipeSlotDrawable irsd:recipeSlots) {
				if(irsd.getRole()==RecipeIngredientRole.OUTPUT) {
					Optional<ItemStack> ois=irsd.getDisplayedItemStack();
					if(ois.isPresent()) {
						ItemStack curOut=ois.get();
						if(curOut.is(Items.QUEEN_BEE)) {
							irsd.createDisplayOverrides().add(output);
						}
					}
				}
				if(irsd.getRole()==RecipeIngredientRole.INPUT) {
					Optional<ItemStack> ois=irsd.getDisplayedItemStack();
					if(ois.isPresent()) {
						ItemStack curOut=ois.get();
						if(curOut.is(Items.LARVA)) {
							irsd.createDisplayOverrides().add(input);
						}
					}
				}
			}
		}
	}


}
