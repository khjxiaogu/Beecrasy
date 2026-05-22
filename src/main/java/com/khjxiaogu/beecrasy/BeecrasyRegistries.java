/** 
* Copyright (c) 2026 khjxiaogu
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

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.khjxiaogu.beecrasy.blocks.PressBlock;
import com.khjxiaogu.beecrasy.blocks.PressBlockEntity;
import com.khjxiaogu.beecrasy.blocks.SequencerBlock;
import com.khjxiaogu.beecrasy.blocks.SkepBlock;
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.components.TintColorComponent;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
@EventBusSubscriber(modid = Beecrasy.MODID)
public class BeecrasyRegistries {
	public static class Items{
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
	    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Beecrasy.MODID);
	    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BEECRASY_TAB = CREATIVE_MODE_TABS.register("aaa_beacrasy_amain", () -> CreativeModeTab.builder()
	            .title(Component.translatable("itemGroup.beecrasy"))
	            .withTabsBefore(CreativeModeTabs.COMBAT)
	            .icon(() -> Items.DRONE.get().getDefaultInstance())
	            .build());
	}
	public static class Blocks{
	    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Beecrasy.MODID);
	    public static final DeferredBlock<Block> HONEY_PRESS=register("honey_press",PressBlock::new,Blocks::machineProps,UnaryOperator.identity());
	    public static final DeferredBlock<SequencerBlock> SEQUENCER=register("sequencer",SequencerBlock::new,Blocks::machineProps,UnaryOperator.identity());
	    public static final DeferredBlock<Block> SKEP=register("skep",SkepBlock::new,Blocks::skepProps,UnaryOperator.identity());
	    public static final DeferredBlock<Block> EMPTY_COMB_BLOCK=register("empty_comb_block");
	    public static final DeferredBlock<Block> HONEY_COMB_BLOCK=register("honey_comb_block");
	    
	    
	    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Beecrasy.MODID);
	    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PressBlockEntity>> PRESS_BLOCKENTITY=BLOCK_ENTITIES.register("honey_press", makeBlockEntityType(PressBlockEntity::new, HONEY_PRESS));
	    
	    public static DeferredBlock<Block> register(String name){
	    	return register(name,Block::new,Blocks::genalDeco,UnaryOperator.identity());
	    }
	    public static <B extends Block> DeferredBlock<B> register(String name,Function<BlockBehaviour.Properties, ? extends B> func,UnaryOperator<BlockBehaviour.Properties> properties, UnaryOperator<Item.Properties> itemProperties){
	    	return register(name,func,BlockItem::new,properties,itemProperties);
	    }
	    public static <B extends Block> DeferredBlock<B> register(String name,Function<BlockBehaviour.Properties, ? extends B> func,BiFunction<B,Item.Properties, ? extends BlockItem> itemfunc, UnaryOperator<BlockBehaviour.Properties> properties, UnaryOperator<Item.Properties> itemProperties){
	    	DeferredBlock<B> db=BLOCKS.registerBlock(name, func,properties);
	    	Items.ITEMS.registerItem(name,p->itemfunc.apply(db.get(), p),itemProperties);
	    	return db;
	    }
		private static Properties genalDeco(Properties properties) {
			return properties.mapColor(MapColor.COLOR_YELLOW).sound(SoundType.WOOD)
					.strength(0.5f).noOcclusion()
					.isRedstoneConductor(Blocks::notSolid).isSuffocating(Blocks::notSolid);
		}
		private static Properties machineProps(Properties properties) {
			return properties.mapColor(MapColor.METAL).sound(SoundType.METAL).requiresCorrectToolForDrops()
					.strength(5f, 6f).noOcclusion()
					.isRedstoneConductor(Blocks::notSolid).isSuffocating(Blocks::notSolid);
		}
		private static Properties skepProps(Properties properties) {
			return properties.mapColor(MapColor.COLOR_YELLOW).sound(SoundType.GRASS).requiresCorrectToolForDrops()
					.strength(0.5f).noOcclusion()
					.isRedstoneConductor(Blocks::notSolid).isSuffocating(Blocks::notSolid);
		}
		private static boolean notSolid(BlockState state, BlockGetter reader, BlockPos pos) {
			return false;
		}
		private static <T extends BlockEntity> Supplier<BlockEntityType<T>> makeBlockEntityType(BlockEntitySupplier<T> create,
			DeferredHolder<Block,? extends Block> valid) {
			return () -> new BlockEntityType<>(create, ImmutableSet.of(valid.get()));
		}
		private static <T extends BlockEntity> Supplier<BlockEntityType<T>> makeBlockEntityTypes(BlockEntitySupplier<T> create,
				List<? extends Supplier<? extends Block>> valid) {
			return () -> new BlockEntityType<>(create, valid.stream().map(Supplier::get).collect(Collectors.toSet()));
		}
	}
	public static class Components{
	    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Beecrasy.MODID);
	    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GenomeComponent>> GENOME=COMPONENTS.registerComponentType("genome", t->t.cacheEncoding().persistent(GenomeComponent.CODEC).networkSynchronized(GenomeComponent.NETWORK_CODEC));
	    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemStack>> TINT_STACK=COMPONENTS.registerComponentType("tint_stack", t->t.cacheEncoding().persistent(ItemStack.CODEC).networkSynchronized(ItemStack.STREAM_CODEC));
	    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TintColorComponent>> TINT_COLOR=COMPONENTS.registerComponentType("tint_color", t->t.cacheEncoding().persistent(TintColorComponent.CODEC).networkSynchronized(TintColorComponent.NETWORK_CODEC));
	    
	}
	public static class Attachments{
		public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Beecrasy.MODID);
		public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> RANDOM_SEED=ATTACHMENTS.register("seed", ()->AttachmentType.builder(RandomSupport::generateUniqueSeed).serialize(Codec.LONG.fieldOf("seed")).sync(ByteBufCodecs.LONG).copyOnDeath().build());
		
	}
	
    public static void register(IEventBus modEventBus) {
    	// Register the Deferred Register to the mod event bus so blocks get registered
    	Blocks.BLOCKS.register(modEventBus);
    	Blocks.BLOCK_ENTITIES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
    	Items.ITEMS.register(modEventBus);
    	
    	Components.COMPONENTS.register(modEventBus);
    	Attachments.ATTACHMENTS.register(modEventBus);
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
