package com.khjxiaogu.beecrasy;

import com.khjxiaogu.beecrasy.client.BeeTint;
import com.khjxiaogu.beecrasy.client.DynamicModelReference;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
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
			ev.register(DynamicModelReference.createKey(id).name(),SimpleUnbakedStandaloneModel.quadCollection(id));	
		});
	}
}
