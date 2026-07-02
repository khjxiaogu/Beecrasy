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

package com.khjxiaogu.beecrasy.compat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Menus;
import com.khjxiaogu.beecrasy.client.apistle.ApistleScreen;
import com.khjxiaogu.beecrasy.client.screens.MailScreen;
import com.khjxiaogu.beecrasy.client.screens.PressScreen;
import com.khjxiaogu.beecrasy.compat.category.PressCategory;
import com.khjxiaogu.beecrasy.compat.category.PheromoneRecipeExtension;
import com.khjxiaogu.beecrasy.compat.category.RoyalJellyRecipeExtension;
import com.khjxiaogu.beecrasy.data.recipe.PheromoneRecipe;
import com.khjxiaogu.beecrasy.data.recipe.PressRecipe;
import com.khjxiaogu.beecrasy.data.recipe.RoyalJellyRecipe;
import com.khjxiaogu.beecrasy.menu.PressMenu;
import com.khjxiaogu.beecrasy.network.MailSetIconMessage;
import com.khjxiaogu.beecrasy.network.PacketHandler;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

@JeiPlugin
public class JEICompat implements IModPlugin {
	IJeiRuntime runtime;
	@Override
	public Identifier getPluginUid() {
		return Identifier.fromNamespaceAndPath(Beecrasy.MODID, "jei_plugin");
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
	    for(DeferredItem<Item> i:Items.INCENSE_LIST)
	    	registration.registerFromDataComponentTypes(i.get(), Components.ARGUMENTATION.get());
	    /*for(DeferredItem<Item> i:List.of(Items.DRONE,Items.LARVA,Items.PRODUCT_COMB,Items.QUEEN_BEE))
	    	registration.registerFromDataComponentTypes(i.get(), Components.GENOME.get());*/
	    //registration.registerFromDataComponentTypes(Items.PRODUCT_COMB.get(), Components.COMB_PRODUCT.get());
	    
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		registration.addRecipeTransferHandler(PressMenu.class, Menus.PRESS_MENU.get(), PressCategory.TYPE, 0, 1, 0, 1);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addCraftingStation(PressCategory.TYPE, Blocks.HONEY_PRESS);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		registration.addRecipes(PressCategory.TYPE,new ArrayList<>(PressRecipe.recipe.getRecipes()));
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		this.runtime=jeiRuntime;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
		registration.addRecipeCategories(new PressCategory(guiHelper));
	}

	@Override
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
		registration.getCraftingCategory().addExtension(PheromoneRecipe.class, new PheromoneRecipeExtension());
		registration.getCraftingCategory().addExtension(RoyalJellyRecipe.class, new RoyalJellyRecipeExtension());
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registry) {
		registry.addGhostIngredientHandler(MailScreen.class, new IGhostIngredientHandler<>() {

			@Override
			public void onComplete() {
			}

			@Override
			public <I> List<Target<I>> getTargetsTyped(MailScreen gui, ITypedIngredient<I> ingredient,
					boolean doStart) {
				if(ingredient.getIngredient(VanillaTypes.ITEM_STACK).isPresent()) {
					return List.of(new Target<>() {

						@Override
						public Rect2i getArea() {
							return new Rect2i(gui.getLeftPos()+143,gui.getTopPos() +28,16,16);
						}

						@Override
						public void accept(I ingredient) {
							if(ingredient instanceof ItemStack is) {
								PacketHandler.sendToServer(new MailSetIconMessage(gui.getMenu().containerId,is));
							}
						}
						
					});
				}
				return List.of();
			}
			
		});
		registry.addGenericGuiContainerHandler(PressScreen.class, new IGuiContainerHandler<PressScreen>() {
			@Override
			public Collection<IGuiClickableArea> getGuiClickableAreas(PressScreen containerScreen, double mouseX, double mouseY) {
				IGuiClickableArea clickableArea = IGuiClickableArea.createBasic(73, 46, 43, 23, PressCategory.TYPE);
				return List.of(clickableArea);
			}

			@Override
			public Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(IClickableIngredientFactory builder, PressScreen containerScreen, double mouseX, double mouseY) {
				if(containerScreen.isMouseIn((int)mouseX, (int)mouseY,91,9, 17, 34)) {
					return builder.createBuilder(NeoForgeTypes.FLUID_STACK, FluidUtil.getStack(containerScreen.getMenu(), 0)).buildWithArea(91,9, 17, 34);
				}
				return IGuiContainerHandler.super.getClickableIngredientUnderMouse(builder, containerScreen, mouseX, mouseY);
			}
			
		});
		registry.addGenericGuiContainerHandler(ApistleScreen.class, new IGuiContainerHandler<ApistleScreen>() {
			@Override
			public Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(IClickableIngredientFactory builder, ApistleScreen containerScreen, double mouseX, double mouseY) {
	
				return containerScreen.peekPointedItem().flatMap(iaa->builder.createBuilder(iaa.stack())
						.buildWithArea(iaa.x(), iaa.y(), iaa.w(), iaa.h()));
			}
			
		});
		
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registration) {

	}

}
