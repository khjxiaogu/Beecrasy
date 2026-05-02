package com.khjxiaogu.beecrasy;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BeecrasyRegistries {

	public static class Tabs{
	    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "beecrasy" namespace
	    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Beecrasy.MODID);
	    // Creates a creative tab with the id "beecrasy:example_tab" for the example item, that is placed after the combat tab
	    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("aaa_beacrasy_amain", () -> CreativeModeTab.builder()
	            .title(Component.translatable("itemGroup.beecrasy")) //The language key for the title of your CreativeModeTab
	            .withTabsBefore(CreativeModeTabs.COMBAT)
	            //.icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
	            .displayItems((parameters, output) -> {
	                //output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
	            }).build());
	}
	public static class Blocks{
	    // Create a Deferred Register to hold Blocks which will all be registered under the "beecrasy" namespace
	    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Beecrasy.MODID);
	}
	public static class Items{
	    // Create a Deferred Register to hold Items which will all be registered under the "beecrasy" namespace
	    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Beecrasy.MODID);
	}
    public static void register(IEventBus modEventBus) {
    	// Register the Deferred Register to the mod event bus so blocks get registered
    	Blocks.BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
    	Items.ITEMS.register(modEventBus);
    	

        // Register the Deferred Register to the mod event bus so tabs get registered
        Tabs.CREATIVE_MODE_TABS.register(modEventBus);
    }
}
