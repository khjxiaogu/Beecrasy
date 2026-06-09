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

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.khjxiaogu.beecrasy.beehive.BeeHiveBaseComponent;
import com.khjxiaogu.beecrasy.beehive.BeeHiveParameters;
import com.khjxiaogu.beecrasy.blocks.BeeNestBlock;
import com.khjxiaogu.beecrasy.blocks.NaturalHiveBlock;
import com.khjxiaogu.beecrasy.blocks.NaturalHiveBlockEntity;
import com.khjxiaogu.beecrasy.blocks.PressBlock;
import com.khjxiaogu.beecrasy.blocks.PressBlockEntity;
import com.khjxiaogu.beecrasy.blocks.SequencerBlock;
import com.khjxiaogu.beecrasy.blocks.SequencerBlockEntity;
import com.khjxiaogu.beecrasy.blocks.SkepBlock;
import com.khjxiaogu.beecrasy.blocks.SkepBlockEntity;
import com.khjxiaogu.beecrasy.client.BeecrasyParticles;
import com.khjxiaogu.beecrasy.components.BeeHiveArgumentation;
import com.khjxiaogu.beecrasy.components.BeehiveArgumenter;
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.components.LarvaProductivity;
import com.khjxiaogu.beecrasy.components.TintColorComponent;
import com.khjxiaogu.beecrasy.data.GenomePresets;
import com.khjxiaogu.beecrasy.data.PressRecipe;
import com.khjxiaogu.beecrasy.entity.BeeSwarmEntity;
import com.khjxiaogu.beecrasy.item.BeehiveBlockItem;
import com.khjxiaogu.beecrasy.item.LarvaItem;
import com.khjxiaogu.beecrasy.item.QueenBeeItem;
import com.khjxiaogu.beecrasy.item.SequencerHandHeld;
import com.khjxiaogu.beecrasy.menu.PressMenu;
import com.khjxiaogu.beecrasy.menu.SequencerMenuBlock;
import com.khjxiaogu.beecrasy.menu.SequencerMenuHandHeld;
import com.khjxiaogu.beecrasy.menu.SkepMenu;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.NeoForgeRegistries.Keys;
@EventBusSubscriber(modid = Beecrasy.MODID)
public class BeecrasyRegistries {
	public static class Items{
	    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Beecrasy.MODID);
	    //基础原料
	    public static final DeferredItem<Item> BEESWAX=ITEMS.registerSimpleItem("beeswax");
	    public static final DeferredItem<Item> COMB_FOUNDATION=ITEMS.registerSimpleItem("comb_foundation");
	    public static final DeferredItem<Item> HONEY_DROP=ITEMS.registerSimpleItem("honey_drop");
	    public static final DeferredItem<Item> APITE=ITEMS.registerSimpleItem("apite");
	    public static final DeferredItem<Item> BUMBLEBEE_JASPER=ITEMS.registerSimpleItem("bumblebee_jasper");
	    public static final DeferredItem<Item> PHEROMONO=ITEMS.registerSimpleItem("pheromone");
	    public static final DeferredItem<Item> ROYAL_JELLY=ITEMS.registerSimpleItem("royal_jelly");
	    public static final DeferredItem<Item> HONEY_BUCKET=ITEMS.registerItem("honey_bucket",p->new BucketItem(Fluids.HONEY_STILL.get(),p),p->p.craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1));
	    //熏香
	    public static final DeferredItem<Item> INCENSE_ARIDITY=ITEMS.registerSimpleItem("incense_aridity_tolerance",p->pheromono(p,s->s.addParam(BeeHiveParameters.HUMIDITY, -.3f)));
	    public static final DeferredItem<Item> INCENSE_HUMIDITY=ITEMS.registerSimpleItem("incense_humidity_tolerance",p->pheromono(p,s->s.addParam(BeeHiveParameters.HUMIDITY, .3f)));
	    
	    public static final DeferredItem<Item> INCENSE_COLD=ITEMS.registerSimpleItem("incense_cold_tolerance",p->pheromono(p,s->s.addParam(BeeHiveParameters.TEMPERATURE, .5f)));
	    public static final DeferredItem<Item> INCENSE_HEAT=ITEMS.registerSimpleItem("incense_heat_tolerance",p->pheromono(p,s->s.addParam(BeeHiveParameters.TEMPERATURE, -.5f)));
	    
	    public static final DeferredItem<Item> INCENSE_YIELD=ITEMS.registerSimpleItem("incense_higher_yield",p->pheromono(p,s->s.addParam(BeeHiveParameters.YIELD, .5f)));
	    public static final DeferredItem<Item> INCENSE_LONG_LIFESPAN=ITEMS.registerSimpleItem("incense_longer_lifespan",p->pheromono(p,s->s.addParam(BeeHiveParameters.LIFESPAN, 1f)));
	    public static final DeferredItem<Item> INCENSE_SHORT_LIFESPAN=ITEMS.registerSimpleItem("incense_shorter_lifespan",p->pheromono(p,s->s.addParam(BeeHiveParameters.LIFESPAN, -.5f)));
	    
	    //蜜蜂相关
	    public static final DeferredItem<Item> DRONE=ITEMS.registerSimpleItem("drone",t->t.component(Components.GENOME, GenomeComponent.HAPLOID_EMPTY).stacksTo(1));
	    public static final DeferredItem<Item> LARVA=ITEMS.registerItem("larva",LarvaItem::new,t->t.component(Components.GENOME, GenomeComponent.DIPLOID_EMPTY.asInspected()).stacksTo(1));
	    public static final DeferredItem<Item> PRODUCT_COMB=ITEMS.registerSimpleItem("product_comb");
	    public static final DeferredItem<Item> QUEEN_BEE=ITEMS.registerItem("queen_bee",QueenBeeItem::new,t->t.component(Components.GENOME, GenomeComponent.DIPLOID_EMPTY).stacksTo(1));
	    //工具
	    public static final DeferredItem<Item> SEQUENCER=ITEMS.registerItem("handheld_sequencer",SequencerHandHeld::new,t->t.component(Components.CONTAINER, ItemContainerContents.EMPTY).stacksTo(1));
	    public static final DeferredItem<Item> BUTTERFLY_NET=ITEMS.registerSimpleItem("butterfly_net",s->s.tool(ToolMaterial.WOOD,Tags.MINABLE_NET, 1.0f, -2.8f, 0).stacksTo(1));
	    public static Item.Properties pheromono(Item.Properties p,Consumer<BeeHiveArgumentation.Builder> components) {

	    	BeeHiveArgumentation.Builder arb=new BeeHiveArgumentation.Builder();
	    	components.accept(arb);
	    	p.component(Components.ARGUMENTATION, new BeehiveArgumenter(arb.build(),true));
	    	return p;
	    }
	}
	public static class Tabs{
	    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Beecrasy.MODID);
	    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BEECRASY_TAB = CREATIVE_MODE_TABS.register("1aaa_beacrasy_amain", () -> CreativeModeTab.builder()
	            .title(Component.translatable("itemGroup.beecrasy"))
	            .withTabsBefore(CreativeModeTabs.COMBAT)
	            .icon(() -> Items.DRONE.get().getDefaultInstance())
	            .build());
	}
	public static class Fluids{
		public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Beecrasy.MODID);
		public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(Keys.FLUID_TYPES, Beecrasy.MODID);
		public static final DeferredHolder<FluidType,FluidType> HONEY=FLUID_TYPES.register("honey",()->new FluidType(FluidType.Properties.create().viscosity(1200)
				.temperature(333).rarity(Rarity.UNCOMMON)));
		
		public static final DeferredHolder<Fluid,Fluid> HONEY_STILL=FLUIDS.register("honey",()->new BaseFlowingFluid.Source(honeyProps()));
		public static final DeferredHolder<Fluid,Fluid> HONEY_FLOWING=FLUIDS.register("honey_flow",()->new BaseFlowingFluid.Flowing(honeyProps()));
		public static BaseFlowingFluid.Properties honeyProps() {
			return new BaseFlowingFluid.Properties(HONEY,HONEY_STILL,HONEY_FLOWING)
			.slopeFindDistance(1)
			.explosionResistance(100F).bucket(Items.HONEY_BUCKET);
		}
	}
	public static class Blocks{
	    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Beecrasy.MODID);
	    public static final DeferredBlock<PressBlock> HONEY_PRESS=register("honey_press",PressBlock::new,Blocks::machineProps,UnaryOperator.identity());
	    public static final DeferredBlock<SequencerBlock> SEQUENCER=register("sequencer",SequencerBlock::new,Blocks::machineProps,UnaryOperator.identity());
	    public static final DeferredBlock<Block> SKEP=register("skep",SkepBlock::new,(b,p)->new BeehiveBlockItem(b, p, ()->new BeeHiveBaseComponent(1,4,4,0)),Blocks::skepProps,t->t.component(Components.BEE_HIVE, SkepBlockEntity.EMPTY));
	    public static final DeferredBlock<Block> EMPTY_COMB_BLOCK=register("empty_comb_block");
	    public static final DeferredBlock<Block> HONEY_COMB_BLOCK=register("honey_comb_block");
	    public static final DeferredBlock<BeeNestBlock> BEE_NEST_NASCENT=register("bee_nest_nascent",p->new BeeNestBlock(p,0,3,BeeNestBlock.NASCENT_SHAPE,BeeNestBlock.NASCENT_CORNER),Blocks::nestProps,UnaryOperator.identity());
	    public static final DeferredBlock<BeeNestBlock> BEE_NEST_SMALL=register("bee_nest_small",p->new BeeNestBlock(p,0,2,BeeNestBlock.SMALL_SHAPE,BeeNestBlock.SMALL_CORNER),Blocks::nestProps,UnaryOperator.identity());
	    public static final DeferredBlock<BeeNestBlock> BEE_NEST_MEDIUM=register("bee_nest_medium",p->new BeeNestBlock(p,1,3,BeeNestBlock.MEDIUM_SHAPE,BeeNestBlock.MEDIUM_CORNER),Blocks::nestProps,UnaryOperator.identity());
	    public static final DeferredBlock<BeeNestBlock> BEE_NEST_BIG=register("bee_nest_big",p->new BeeNestBlock(p,2,4,BeeNestBlock.LARGE_SHAPE,BeeNestBlock.LARGE_CORNER),Blocks::nestProps,UnaryOperator.identity());
	    public static final DeferredBlock<NaturalHiveBlock> NATURAL_HIVE=register("natural_hive",NaturalHiveBlock::new,Blocks::nestProps,UnaryOperator.identity());
	    public static final DeferredBlock<DoublePlantBlock> FLOWER_ASPHODEL=register("asphodel",DoublePlantBlock::new,Blocks::flower,UnaryOperator.identity());
	    public static final DeferredBlock<DoublePlantBlock> FLOWER_DELPHINIUM_BLUE = register("delphinium_blue", DoublePlantBlock::new, Blocks::flower, UnaryOperator.identity());
	    public static final DeferredBlock<DoublePlantBlock> FLOWER_DELPHINIUM_PINK = register("delphinium_pink", DoublePlantBlock::new, Blocks::flower, UnaryOperator.identity());
	    public static final DeferredBlock<DoublePlantBlock> FLOWER_DELPHINIUM_VIOLET = register("delphinium_violet", DoublePlantBlock::new, Blocks::flower, UnaryOperator.identity());
	    public static final DeferredBlock<DoublePlantBlock> FLOWER_FOXTAIL_LILY = register("foxtail_lily", DoublePlantBlock::new, Blocks::flower, UnaryOperator.identity());
	    public static final DeferredBlock<DoublePlantBlock> FLOWER_FOXTAIL_LILY_FIERY = register("foxtail_lily_fiery", DoublePlantBlock::new, Blocks::flower, UnaryOperator.identity());
	    public static final DeferredBlock<DoublePlantBlock> FLOWER_HOLLYHOCK_PINK = register("hollyhock_pink", DoublePlantBlock::new, Blocks::flower, UnaryOperator.identity());
	    public static final DeferredBlock<DoublePlantBlock> FLOWER_HOLLYHOCK_RED = register("hollyhock_red", DoublePlantBlock::new, Blocks::flower, UnaryOperator.identity());
	    public static final DeferredBlock<DoublePlantBlock> FLOWER_HOLLYHOCK_WHITE = register("hollyhock_white", DoublePlantBlock::new, Blocks::flower, UnaryOperator.identity());
	    public static final DeferredBlock<DoublePlantBlock> FLOWER_PROTEA = register("protea", DoublePlantBlock::new, Blocks::flower, UnaryOperator.identity());
	    public static final DeferredBlock<DoublePlantBlock> FLOWER_PROTEA_ARTISAN = register("protea_artisan", DoublePlantBlock::new, Blocks::flower, UnaryOperator.identity());
	    
	    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Beecrasy.MODID);
	    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PressBlockEntity>> PRESS_BLOCKENTITY=BLOCK_ENTITIES.register("honey_press", makeBlockEntityType(PressBlockEntity::new, HONEY_PRESS));
	    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NaturalHiveBlockEntity>> NATURAL_HIVE_BLOCKENTITY=BLOCK_ENTITIES.register("natural_hive",makeBlockEntityType(NaturalHiveBlockEntity::new, NATURAL_HIVE));
	    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SkepBlockEntity>> SKEP_BLOCKENTITY=BLOCK_ENTITIES.register("skep", makeBlockEntityType(SkepBlockEntity::new, SKEP));
	    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SequencerBlockEntity>> SEQUENCER_BLOCKENTITY=BLOCK_ENTITIES.register("sequencer", makeBlockEntityType(SequencerBlockEntity::new, SEQUENCER));

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
	    static Properties flower(Properties properties) {
	    	return properties.mapColor(MapColor.PLANT)
            .noCollision()
            .instabreak()
            .sound(SoundType.GRASS)
            .offsetType(BlockBehaviour.OffsetType.XZ)
            .ignitedByLava()
            .pushReaction(PushReaction.DESTROY);
	    }
		static Properties genalDeco(Properties properties) {
			return properties.mapColor(MapColor.COLOR_YELLOW).sound(SoundType.WOOD)
					.strength(0.5f).noOcclusion()
					.isRedstoneConductor(Blocks::notSolid).isSuffocating(Blocks::notSolid);
		}
		static Properties machineProps(Properties properties) {
			return properties.mapColor(MapColor.METAL).sound(SoundType.METAL).requiresCorrectToolForDrops()
					.strength(5f, 6f).noOcclusion()
					.isRedstoneConductor(Blocks::notSolid).isSuffocating(Blocks::notSolid);
		}
		static Properties skepProps(Properties properties) {
			return properties.mapColor(MapColor.COLOR_YELLOW).sound(SoundType.GRASS)
					.strength(0.5f).noOcclusion()
					.isRedstoneConductor(Blocks::notSolid).isSuffocating(Blocks::notSolid);
		}
		static Properties nestProps(Properties properties) {
			return properties.mapColor(MapColor.COLOR_YELLOW).sound(SoundType.GRASS).requiresCorrectToolForDrops()
					.strength(0.5f).noOcclusion()
					.isRedstoneConductor(Blocks::notSolid).isSuffocating(Blocks::notSolid);
		}
		static boolean notSolid(BlockState state, BlockGetter reader, BlockPos pos) {
			return false;
		}
		static <T extends BlockEntity> Supplier<BlockEntityType<T>> makeBlockEntityType(BlockEntitySupplier<T> create,
			DeferredHolder<Block,? extends Block> valid) {
			return () -> new BlockEntityType<>(create, ImmutableSet.of(valid.get()));
		}
		static <T extends BlockEntity> Supplier<BlockEntityType<T>> makeBlockEntityTypes(BlockEntitySupplier<T> create,
				List<? extends Supplier<? extends Block>> valid) {
			return () -> new BlockEntityType<>(create, valid.stream().map(Supplier::get).collect(Collectors.toSet()));
		}
	}
	public static class Components{
	    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Beecrasy.MODID);
	    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GenomeComponent>> GENOME=COMPONENTS.registerComponentType("genome", t->t.cacheEncoding().persistent(GenomeComponent.CODEC).networkSynchronized(GenomeComponent.NETWORK_CODEC));
	    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemStackTemplate>> TINT_STACK=COMPONENTS.registerComponentType("tint_stack", t->t.cacheEncoding().persistent(ItemStackTemplate.CODEC).networkSynchronized(ItemStackTemplate.STREAM_CODEC));
	    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TintColorComponent>> TINT_COLOR=COMPONENTS.registerComponentType("tint_color", t->t.cacheEncoding().persistent(TintColorComponent.CODEC).networkSynchronized(TintColorComponent.NETWORK_CODEC));
	    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemStackTemplate>> COMB_PRODUCT=COMPONENTS.registerComponentType("comb_product", t->t.cacheEncoding().persistent(ItemStackTemplate.CODEC).networkSynchronized(ItemStackTemplate.STREAM_CODEC));
	    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LarvaProductivity>> LARVA_PRODUCT=COMPONENTS.registerComponentType("larva_product", t->t.cacheEncoding().persistent(LarvaProductivity.CODEC).networkSynchronized(LarvaProductivity.STREAM_CODEC));
	    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> LARVA_EXPIRES=COMPONENTS.registerComponentType("larva_expire", t->t.cacheEncoding().persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.LONG));
	    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BeehiveArgumenter>> ARGUMENTATION=COMPONENTS.registerComponentType("beehive_argumentation", t->t.cacheEncoding().persistent(BeehiveArgumenter.CODEC).networkSynchronized(BeehiveArgumenter.STREAM_CODEC));
	    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> CONTAINER=COMPONENTS.registerComponentType("container", t->t.cacheEncoding().persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC));
	    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BeeHiveBaseComponent.BeeHiveBaseData>> BEE_HIVE=COMPONENTS.registerComponentType("bee_hive", t->t.cacheEncoding().ignoreSwapAnimation().persistent(BeeHiveBaseComponent.BeeHiveBaseData.CODEC).networkSynchronized(BeeHiveBaseComponent.BeeHiveBaseData.STREAM_CODEC));

	    
	}
	public static class Entities{
		public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(Beecrasy.MODID);
		public static final DeferredHolder<EntityType<?>, EntityType<BeeSwarmEntity>> BEE_SWARM=ENTITY_TYPES.registerEntityType("bee_swarm", BeeSwarmEntity::new, MobCategory.MISC,t->t.sized(0.1f, 0.1f).noLootTable());
		
	}
	public static class Attachments{
		public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Beecrasy.MODID);
		public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> RANDOM_SEED=ATTACHMENTS.register("seed", ()->AttachmentType.builder(RandomSupport::generateUniqueSeed).serialize(Codec.LONG.fieldOf("seed")).sync(ByteBufCodecs.LONG).copyOnDeath().build());
		
	}
	public static class Tags{
		public static final TagKey<Block> MINABLE_NET=BlockTags.create(Beecrasy.rl("minable_net"));
		public static final TagKey<Block> FLOWERS=BlockTags.create(Beecrasy.rl("flowers"));
		public static final TagKey<Fluid> HONEY=FluidTags.create(Beecrasy.rl("honey"));
		public static final TagKey<Item> HONEY_DROP=ItemTags.create(Beecrasy.rl("honey"));
		
	}
	public static class Recipes{
		public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
			.create(Registries.RECIPE_SERIALIZER, Beecrasy.MODID);
		public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister
				.create(Registries.RECIPE_TYPE, Beecrasy.MODID);
		
		public static final DeferredHolder<RecipeType<?>, RecipeType<PressRecipe>> PRESS_TYPE=createType("press");
		public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PressRecipe>> PRESS_SERIALIZER=createSerializer("press",PressRecipe.CODEC,PressRecipe.STREAM_CODEC);
		
		public static final DeferredHolder<RecipeType<?>, RecipeType<GenomePresets>> GENOME_PRESET_TYPE=createType("genome_preset");
		public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GenomePresets>> GENOME_PRESET=createSerializer("genome_preset",GenomePresets.CODEC,GenomePresets.STREAM_CODEC);
		
		public static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> createType(String name) {
			return RECIPE_TYPES.register(name,RecipeType::simple);
		}
		public static <T extends Recipe<?>> DeferredHolder<RecipeSerializer<?>, RecipeSerializer<T>> createSerializer(String name,MapCodec<T> codec,StreamCodec<RegistryFriendlyByteBuf,T> stream){
			return RECIPE_SERIALIZERS.register(name,()->new RecipeSerializer<>(codec,stream));
		}
	}

	public static class Menus{
		public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister
			.create(Registries.MENU, Beecrasy.MODID);
		public static final DeferredHolder<MenuType<?>, MenuType<PressMenu>> PRESS_MENU=MENU_TYPES.register("press", () -> IMenuTypeExtension.create(PressMenu::new));
		public static final DeferredHolder<MenuType<?>, MenuType<SkepMenu>> SKEP_MENU=MENU_TYPES.register("skep", () -> IMenuTypeExtension.create(SkepMenu::new));
		public static final DeferredHolder<MenuType<?>, MenuType<SequencerMenuHandHeld>> SEQUENCER_HANDHELD_MENU=MENU_TYPES.register("sequencer_handheld", () -> IMenuTypeExtension.create(SequencerMenuHandHeld::new));
		public static final DeferredHolder<MenuType<?>, MenuType<SequencerMenuBlock>> SEQUENCER_BLOCK_MENU=MENU_TYPES.register("sequencer_block", () -> IMenuTypeExtension.create(SequencerMenuBlock::new));
		
	}
    public static void register(IEventBus modEventBus) {
    	// Register the Deferred Register to the mod event bus so blocks get registered
    	Blocks.BLOCKS.register(modEventBus);
    	Blocks.BLOCK_ENTITIES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
    	Items.ITEMS.register(modEventBus);
    	
    	Components.COMPONENTS.register(modEventBus);
    	Attachments.ATTACHMENTS.register(modEventBus);
    	Entities.ENTITY_TYPES.register(modEventBus);
    	Recipes.RECIPE_SERIALIZERS.register(modEventBus);
    	Recipes.RECIPE_TYPES.register(modEventBus);
    	Menus.MENU_TYPES.register(modEventBus);
    	Fluids.FLUID_TYPES.register(modEventBus);
    	Fluids.FLUIDS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        Tabs.CREATIVE_MODE_TABS.register(modEventBus);
        
		BeecrasyParticles.REGISTER.register(modEventBus);
        
    }
    public static ItemStack pheromono(Consumer<BeeHiveArgumentation.Builder> components) {
    	ItemStack is=new ItemStack(Items.PHEROMONO.asItem());
    	BeeHiveArgumentation.Builder arb=new BeeHiveArgumentation.Builder();
    	components.accept(arb);
    	is.set(Components.ARGUMENTATION, new BeehiveArgumenter(arb.build(),true));
    	return is;
    }
    @SubscribeEvent
    public static void onBuildTabs(BuildCreativeModeTabContentsEvent ev) {
    	if(ev.getTab()==Tabs.BEECRASY_TAB.get()) {
    		for(DeferredHolder<Item, ? extends Item> i:Items.ITEMS.getEntries()) {
    			ev.accept(i.get());
    		}
    		ev.accept(pheromono(t->t.setParam(BeeHiveParameters.SPEED, 9f)));
    		ev.accept(pheromono(t->t.setParam(BeeHiveParameters.MUTATE, 9f)));
    	}
    }
}
