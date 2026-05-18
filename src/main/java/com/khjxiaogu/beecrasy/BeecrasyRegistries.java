package com.khjxiaogu.beecrasy;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.components.TintColorComponent;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
@EventBusSubscriber(modid = Beecrasy.MODID, value = Dist.CLIENT)
public class BeecrasyRegistries {
	public static class Items{
	    // Create a Deferred Register to hold Items which will all be registered under the "beecrasy" namespace
	    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Beecrasy.MODID);
	    //基础原料
	    public static final DeferredItem<Item> BEESWAX=ITEMS.registerSimpleItem("beeswax");
	    public static final DeferredItem<Item> COMB_FOUNDATION=ITEMS.registerSimpleItem("comb_foundation");
	    public static final DeferredItem<Item> HONEY_DROP=ITEMS.registerSimpleItem("honey_drop");
	    //蜜蜂相关
	    public static final DeferredItem<Item> DRONE=ITEMS.registerSimpleItem("drone");
	    public static final DeferredItem<Item> LARVA=ITEMS.registerSimpleItem("larva");
	    public static final DeferredItem<Item> PRODUCT_COMB=ITEMS.registerSimpleItem("product_comb");
	    public static final DeferredItem<Item> QUEEN_BEE=ITEMS.registerSimpleItem("queen_bee");
	    //工具
	    public static final DeferredItem<Item> SEQUENCER=ITEMS.registerSimpleItem("handheld_sequencer");
	}
	public static class Tabs{
	    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "beecrasy" namespace
	    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Beecrasy.MODID);
	    // Creates a creative tab with the id "beecrasy:example_tab" for the example item, that is placed after the combat tab
	    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BEECRASY_TAB = CREATIVE_MODE_TABS.register("aaa_beacrasy_amain", () -> CreativeModeTab.builder()
	            .title(Component.translatable("itemGroup.beecrasy")) //The language key for the title of your CreativeModeTab
	            .withTabsBefore(CreativeModeTabs.COMBAT)
	            .icon(() -> Items.DRONE.get().getDefaultInstance())
	            .build());
	}
	public static class Blocks{
	    // Create a Deferred Register to hold Blocks which will all be registered under the "beecrasy" namespace
	    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Beecrasy.MODID);
	    public static final DeferredBlock<Block> HONEY_PRESS=register("honey_press");
	    public static final DeferredBlock<Block> SEQUENCER=register("sequencer");
	    public static final DeferredBlock<Block> SKEP=register("skep");
	    public static DeferredBlock<Block> register(String name){
	    	return register(name,Block::new,UnaryOperator.identity(),UnaryOperator.identity());
	    }
	    public static <B extends Block> DeferredBlock<B> register(String name,Function<BlockBehaviour.Properties, ? extends B> func,UnaryOperator<BlockBehaviour.Properties> properties, UnaryOperator<Item.Properties> itemProperties){
	    	return register(name,func,BlockItem::new,properties,itemProperties);
	    }
	    public static <B extends Block> DeferredBlock<B> register(String name,Function<BlockBehaviour.Properties, ? extends B> func,BiFunction<B,Item.Properties, ? extends BlockItem> itemfunc, UnaryOperator<BlockBehaviour.Properties> properties, UnaryOperator<Item.Properties> itemProperties){
	    	DeferredBlock<B> db=BLOCKS.registerBlock(name, func,properties);
	    	Items.ITEMS.registerItem(name,p->itemfunc.apply(db.get(), p),itemProperties);
	    	return db;
	    }
	}
	public static class Components{
	    // Create a Deferred Register to hold Blocks which will all be registered under the "beecrasy" namespace
	    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Beecrasy.MODID);
	    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GenomeComponent>> GENOME=COMPONENTS.registerComponentType("genome", t->t.cacheEncoding().persistent(GenomeComponent.CODEC).networkSynchronized(GenomeComponent.NETWORK_CODEC));
	    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemStack>> TINT_STACK=COMPONENTS.registerComponentType("tint_stack", t->t.cacheEncoding().persistent(ItemStack.CODEC).networkSynchronized(ItemStack.STREAM_CODEC));
	    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TintColorComponent>> TINT_COLOR=COMPONENTS.registerComponentType("tint_color", t->t.cacheEncoding().persistent(TintColorComponent.CODEC).networkSynchronized(TintColorComponent.NETWORK_CODEC));
		
	}
	
    public static void register(IEventBus modEventBus) {
    	// Register the Deferred Register to the mod event bus so blocks get registered
    	Blocks.BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
    	Items.ITEMS.register(modEventBus);
    	

        // Register the Deferred Register to the mod event bus so tabs get registered
        Tabs.CREATIVE_MODE_TABS.register(modEventBus);
    }
    @SubscribeEvent
    public static void onBuildTabs(BuildCreativeModeTabContentsEvent ev) {
    	if(ev.getTab()==Tabs.BEECRASY_TAB.get()) {
    		for(DeferredHolder<Item, ? extends Item> i:Items.ITEMS.getEntries()) {
    			ev.accept(i.get());
    		}
    	}
    }
}
