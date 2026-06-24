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
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Entities;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Fluids;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Menus;
import com.khjxiaogu.beecrasy.beedi.BeediManager;
import com.khjxiaogu.beecrasy.client.BeeTint;
import com.khjxiaogu.beecrasy.client.BeecrasyParticles;
import com.khjxiaogu.beecrasy.client.ModelReference;
import com.khjxiaogu.beecrasy.client.model.GuiOnlySpecialModelWrapper;
import com.khjxiaogu.beecrasy.client.model.MailModel;
import com.khjxiaogu.beecrasy.client.particles.BeeParticle;
import com.khjxiaogu.beecrasy.client.particles.BeeSwarmParticle;
import com.khjxiaogu.beecrasy.client.renderer.BeeSwarmRenderer;
import com.khjxiaogu.beecrasy.client.renderer.PressBlockEntityRenderer;
import com.khjxiaogu.beecrasy.client.screens.HiveScreen;
import com.khjxiaogu.beecrasy.client.screens.MailBoxScreen;
import com.khjxiaogu.beecrasy.client.screens.MailScreen;
import com.khjxiaogu.beecrasy.client.screens.PressScreen;
import com.khjxiaogu.beecrasy.client.screens.SequenceBlockScreen;
import com.khjxiaogu.beecrasy.client.screens.SequenceHandHeldScreen;
import com.khjxiaogu.beecrasy.client.screens.SkepScreen;
import com.khjxiaogu.beecrasy.client.screens.sequencertabs.SequencerTabs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = Beecrasy.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = Beecrasy.MODID, value = Dist.CLIENT)
public class BeecrasyClient {
    public BeecrasyClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    /**
	 * @param event  
	 */
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
    	Beecrasy.LOGGER.info("Beecracy has landed(Not a typo).");
    }
    
	@SubscribeEvent
	public static void registerMenuScreens(RegisterMenuScreensEvent event) {
		event.register(Menus.SKEP_MENU.get(), SkepScreen::new);
		event.register(Menus.HIVE_MENU.get(), HiveScreen::new);
		event.register(Menus.PRESS_MENU.get(), PressScreen::new);
		event.register(Menus.SEQUENCER_HANDHELD_MENU.get(), SequenceHandHeldScreen::new);
		event.register(Menus.SEQUENCER_BLOCK_MENU.get(), SequenceBlockScreen::new);
		event.register(Menus.MAIL_MENU.get(), MailScreen::new);
		event.register(Menus.MAILBOX_MENU.get(), MailBoxScreen::new);

		
		
		SequencerTabs.init();
	}
	@SubscribeEvent
	public static void registerItemTint(RegisterColorHandlersEvent.ItemTintSources ev) {
		ev.register(Beecrasy.rl("bee_product"), BeeTint.MAP_CODEC);
		
	}
	
	@SubscribeEvent
	public static void onWorldUnload(LevelEvent.Unload ev)
	{
		if(ev.getLevel() instanceof ClientLevel)
			BeediManager.INSTANCE.resetClientLevel();
		
	}
	@SubscribeEvent
	public static void onWorldTick(LevelTickEvent.Pre ev)
	{
		if(ev.getLevel() instanceof ClientLevel cl)
			BeediManager.INSTANCE.tick(cl);
	}
	@SubscribeEvent
	public static void onReload(AddClientReloadListenersEvent ev)
	{
		ev.addListener(Beecrasy.rl("midi"),BeediManager.INSTANCE);
		
	}
	@SuppressWarnings("resource")
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
		event.registerEntityRenderer(Entities.BEE_SWARM.get(),BeeSwarmRenderer::new);
		
	}
	@SubscribeEvent
	public static void registerSpecialModelType(RegisterSpecialModelRendererEvent event) {
		event.register(Beecrasy.rl("mail"), MailModel.Unbaked.MAP_CODEC);
	}
	@SubscribeEvent
	public static void registerItemModelType(RegisterItemModelsEvent event) {
		event.register(Beecrasy.rl("gui_only"), GuiOnlySpecialModelWrapper.Unbaked.MAP_CODEC);
	}
	
    @SubscribeEvent
    static void onRegisterFluidModels(RegisterFluidModelsEvent event) {

		event.register(new FluidModel.Unbaked(
                new Material(Beecrasy.rl("block/honey")),
                new Material(Beecrasy.rl("block/honey_flow")),
                null,
                null),Fluids.HONEY_STILL,Fluids.HONEY_FLOWING);
	}
	@SubscribeEvent
	public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(BeecrasyParticles.BEE.get(), BeeParticle.Factory::new);
		event.registerSpriteSet(BeecrasyParticles.BEE_SWARM.get(), BeeSwarmParticle.Factory::new);
	}
    
}
