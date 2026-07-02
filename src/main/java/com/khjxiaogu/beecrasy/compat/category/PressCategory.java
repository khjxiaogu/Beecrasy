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
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.compat.ChanceCallback;
import com.khjxiaogu.beecrasy.compat.CombCallback;
import com.khjxiaogu.beecrasy.data.recipe.PossibleOutput;
import com.khjxiaogu.beecrasy.data.recipe.PressRecipe;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

public class PressCategory implements IRecipeCategory<RecipeHolder<PressRecipe>> {

	@SuppressWarnings("rawtypes")
	public static IRecipeType<RecipeHolder> TYPE=IRecipeType.create(Beecrasy.MODID, "grinding",RecipeHolder.class);
	public static String titleId="jei.category.beecrasy.press.title";
	private IDrawable BACKGROUND;
	private IDrawable ICON;

	public PressCategory(IGuiHelper guiHelper) {
		this.ICON = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.HONEY_PRESS));
		Identifier guiMain = Beecrasy.rl("textures/gui/jei/honey_press.png");
		this.BACKGROUND = guiHelper.createDrawable(guiMain, 0, 0, 128, 64);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public IRecipeType<RecipeHolder<PressRecipe>> getRecipeType() {
		return (IRecipeType)TYPE;
	}

	@Override
	public Component getTitle() {
		return Component.translatable(titleId);
	}

	@Override
	public int getWidth() {
		return 128;
	}

	@Override
	public int getHeight() {
		return 64;
	}

	@Override
	public IDrawable getIcon() {
		return ICON;
	}
	private static List<ItemStack> unpack(SizedIngredient ingredient) {

		return ingredient.ingredient().getValues().stream().map(t->new ItemStack(t,ingredient.count())).collect(Collectors.toList());
	}
	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<PressRecipe> recipe, IFocusGroup focuses) {
		PressRecipe recipev=recipe.value();
		builder.addInputSlot(10, 42).addItemStacks(unpack(recipev.input()));
		
		if(recipev.fluid().isPresent()) {
			IRecipeSlotBuilder output=builder.addOutputSlot(41,6);
			FluidStackTemplate template=recipev.fluid().get();
			output.add(template.fluid().value(),template.amount(),template.components())
			.setFluidRenderer(Math.max(250, template.amount()), false, 16, 34);
		}
		List<PossibleOutput> is=recipev.output();
		for(int i=0;i<9;i++) {
			IRecipeSlotBuilder slot=builder.addOutputSlot(65+(i%3)*18, 6+(i/3)*18);
			if(i<is.size()) {
				PossibleOutput ev=is.get(i);
				slot.add(ev.stack()).addRichTooltipCallback(new ChanceCallback(ev));
			}else {
				slot.add(ItemStack.EMPTY).addRichTooltipCallback(new CombCallback());
			}
		}
	}
	
	@Override
	public void onDisplayedIngredientsUpdate(RecipeHolder<PressRecipe> recipe, List<IRecipeSlotDrawable> recipeSlots,
			IFocusGroup focuses) {
		@Nullable ItemStackTemplate out=null;
		
		for(IFocus<ItemStack> irsd:(Iterable<IFocus<ItemStack>>)()->focuses.getItemStackFocuses(RecipeIngredientRole.INPUT).iterator()) {
			Optional<ItemStack> ois=irsd.getTypedValue().getItemStack();
			if(ois.isPresent()) {
				ItemStack curOut=ois.get();
				out=curOut.get(Components.COMB_PRODUCT);
			}
		}
		if(out!=null) {
			for(IRecipeSlotDrawable irsd:recipeSlots) {
				if(irsd.getRole()==RecipeIngredientRole.OUTPUT&&irsd.getItemStacks().allMatch(ItemStack::isEmpty)) {
					irsd.createDisplayOverrides().add(out);
					break;
				}
			}
		}
	}
	@Override
	public void draw(RecipeHolder<PressRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
		IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
		BACKGROUND.draw(guiGraphics);
	}


}
