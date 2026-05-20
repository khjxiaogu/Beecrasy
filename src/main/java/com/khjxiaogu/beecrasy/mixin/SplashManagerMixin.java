package com.khjxiaogu.beecrasy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.khjxiaogu.beecrasy.utils.DateHelper;
import com.khjxiaogu.beecrasy.utils.Utils;

import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.util.SpecialDates;
@Mixin(SplashManager.class)
public class SplashManagerMixin {
	private static final SplashRenderer WORLD_BEE_DAY=new SplashRenderer(Utils.translate("splash.beecrasy.world_bee_day"));
	@Inject(at=@At("HEAD"),require=1,expect=1,allow=1,cancellable=true,method="Lnet/minecraft/client/resources/SplashManager;getSplash()Lnet/minecraft/client/gui/components/SplashRenderer;")
	public void by$getSplash(CallbackInfoReturnable<SplashRenderer> callback) {
		if(SpecialDates.dayNow().equals(DateHelper.WORLD_BEE_DAY)) {
			callback.setReturnValue(WORLD_BEE_DAY);
		}
	}
}
