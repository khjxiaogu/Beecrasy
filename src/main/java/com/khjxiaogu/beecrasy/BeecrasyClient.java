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

package com.khjxiaogu.beecrasy;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Menus;
import com.khjxiaogu.beecrasy.client.BeeTint;
import com.khjxiaogu.beecrasy.client.ModelReference;
import com.khjxiaogu.beecrasy.client.renderer.PressBlockEntityRenderer;
import com.khjxiaogu.beecrasy.client.screens.SkepScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = Beecrasy.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = Beecrasy.MODID, value = Dist.CLIENT)
public class BeecrasyClient {
    public BeecrasyClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
    	Beecrasy.LOGGER.info("Beecracy has landed(Not a typo).");
    }
    
	@SubscribeEvent
	public static void registerMenuScreens(RegisterMenuScreensEvent event) {
		event.register(Menus.SKEP_MENU.get(), SkepScreen::new);
	}
	@SubscribeEvent
	public static void registerItemTint(RegisterColorHandlersEvent.ItemTintSources ev) {
		ev.register(Beecrasy.rl("bee_product"), BeeTint.MAP_CODEC);
		
	}
	@SubscribeEvent
	public static void registerModels(ModelEvent.RegisterStandalone ev)
	{
		Minecraft.getInstance().getResourceManager().listResources("models/block/dynamic",e->e.getPath().endsWith(".json")&&Beecrasy.MODID.equals(e.getNamespace())).keySet().forEach(rl->{
			//remove models/ and .json
			String name=rl.getPath().substring(0,rl.getPath().lastIndexOf(".")).substring(7);
			Identifier id=Beecrasy.rl(name);
			ev.register(ModelReference.createKey(id).name(),SimpleUnbakedStandaloneModel.quadCollection(id));	
		});
	}
	@SubscribeEvent
	public static void onRegisterRenderer(RegisterRenderers event) {
		event.registerBlockEntityRenderer(Blocks.PRESS_BLOCKENTITY.get(), PressBlockEntityRenderer::new);
		
	}
}
