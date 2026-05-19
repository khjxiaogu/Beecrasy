package com.khjxiaogu.beecrasy.listeners;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Attachments;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;

@EventBusSubscriber(modid = Beecrasy.MODID)
public class CommonListeners {
	@SubscribeEvent
	public static void onLogin(PlayerLoggedInEvent event) {
		Player p=event.getEntity();
		@Nullable Long comp=p.getData(Attachments.RANDOM_SEED);
		//Neoforge会初始化一个默认值，这里只做备用
		if(comp==null) {
			comp=RandomSupport.generateUniqueSeed();
			p.setData(Attachments.RANDOM_SEED, comp);
		}
	}
}
