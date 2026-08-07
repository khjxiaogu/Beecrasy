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

package com.khjxiaogu.beecrasy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.BeecrasyConfig;
import com.khjxiaogu.beecrasy.utils.CraftingSequenceMatcher;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.fml.loading.FMLPaths;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
	@Shadow
    private HolderLookup.Provider registries;
	public RecipeManagerMixin() {
	}
	/**
	 * @param set  
	 * @param cbi 
	 */
	@Inject(at = @At(value = "TAIL", remap = true), method = "Lnet/minecraft/world/item/crafting/RecipeManager;finalizeRecipeLoading(Lnet/minecraft/world/flag/FeatureFlagSet;)V", remap = true, cancellable = true, require = 1, allow = 1)
	public void finishReload(FeatureFlagSet set, CallbackInfo cbi) {
		boolean shouldNotWait=StackWalker.getInstance()
            .walk(stackStream -> stackStream.anyMatch(frame ->
            frame.getClassName().endsWith(".ReloadCommand")
		    ))&&BeecrasyConfig.SERVER.NO_BLOCK_RELOAD.getAsBoolean();
		//Teacon server has a typically bad performance, so add a special file to note that
		boolean isTeacon=(FMLPaths.CONFIGDIR.get().resolve("beecrasy-load.toml").toFile().exists());
		if(isTeacon)
			Beecrasy.LOGGER.info("Teacon mode");
		CraftingSequenceMatcher.bake(getObjthis().recipeMap().byType(RecipeType.CRAFTING),(!shouldNotWait)&&!isTeacon);
	}
	private RecipeManager getObjthis() {
		return (RecipeManager)(Object)this;
	}
}
