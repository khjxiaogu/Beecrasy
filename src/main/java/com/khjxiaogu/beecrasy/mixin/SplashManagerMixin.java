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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.khjxiaogu.beecrasy.utils.DateHelper;
import com.khjxiaogu.beecrasy.utils.Utils;

import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.network.chat.Style;
import net.minecraft.util.SpecialDates;
@Mixin(SplashManager.class)
public class SplashManagerMixin {
	private static final SplashRenderer WORLD_BEE_DAY=new SplashRenderer(Utils.translate("splash.beecrasy.world_bee_day").withStyle(Style.EMPTY.withColor(-256)));
	@Inject(at=@At("HEAD"),require=1,expect=1,allow=1,cancellable=true,method="Lnet/minecraft/client/resources/SplashManager;getSplash()Lnet/minecraft/client/gui/components/SplashRenderer;")
	public void by$getSplash(CallbackInfoReturnable<SplashRenderer> callback) {
		if(SpecialDates.dayNow().equals(DateHelper.WORLD_BEE_DAY)) {
			callback.setReturnValue(WORLD_BEE_DAY);
		}
	}
}
