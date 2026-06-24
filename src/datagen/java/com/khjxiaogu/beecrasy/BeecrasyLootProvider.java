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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Tags;
import com.khjxiaogu.beecrasy.data.loot.BeeFamilyPool;
import com.khjxiaogu.beecrasy.data.loot.GenerateGenomesFunction;

import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class BeecrasyLootProvider extends BlockLootSubProvider {

   private static final Set<Item> EXPLOSION_RESISTANT = Stream.of(
        Blocks.SEQUENCER.get()
    )
    .map(ItemLike::asItem)
    .collect(Collectors.toSet());

	public BeecrasyLootProvider(HolderLookup.Provider registries) {
	    super(EXPLOSION_RESISTANT, FeatureFlags.REGISTRY.allFlags(), registries);
	}


	@Override
	protected void generate() {

		this.add(Blocks.HONEY_PRESS.get(),
		LootTable.lootTable()
        .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.HONEY_PRESS.get())
        	.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)))
        	.add(LootItem.lootTableItem(Blocks.HONEY_PRESS.asItem()))));
		this.dropSelf(Blocks.SEQUENCER.get());
		this.dropSelf(Blocks.EMPTY_COMB_BLOCK.get());
		this.dropSelf(Blocks.HONEY_COMB_BLOCK.get());
		this.dropSelf(Blocks.FLOWER_ASPHODEL.get());
		this.dropSelf(Blocks.FLOWER_DELPHINIUM_BLUE.get());
		this.dropSelf(Blocks.FLOWER_DELPHINIUM_PINK.get());
		this.dropSelf(Blocks.FLOWER_DELPHINIUM_VIOLET.get());
		this.dropSelf(Blocks.FLOWER_FOXTAIL_LILY.get());
		this.dropSelf(Blocks.FLOWER_FOXTAIL_LILY_FIERY.get());
		this.dropSelf(Blocks.FLOWER_HOLLYHOCK_PINK.get());
		this.dropSelf(Blocks.FLOWER_HOLLYHOCK_RED.get());
		this.dropSelf(Blocks.FLOWER_HOLLYHOCK_WHITE.get());
		this.dropSelf(Blocks.FLOWER_PROTEA.get());
		this.dropSelf(Blocks.FLOWER_PROTEA_ARTISAN.get());

		this.dropSelf(Blocks.BEE_CITY_CORE.get());
		this.dropOther(Blocks.BEE_CITY_COMB.get(),Blocks.EMPTY_COMB_BLOCK.asItem());
		this.dropOther(Blocks.BEE_CITY_QUEEN.get(),Blocks.EMPTY_COMB_BLOCK.asItem());
		
		this.dropSelf(Blocks.BEEPER.get());
		this.dropSelf(Blocks.BEEDIBOX.get());
		this.add(Blocks.NATURAL_HIVE.get(),BlockLootSubProvider.noDrop());
		
		this.add(Blocks.SKEP.get(),
		LootTable.lootTable()
        .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
        	.add(LootItem.lootTableItem(Blocks.SKEP.asItem())
        		.apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)))));
		this.add(Blocks.HIVE.get(),
		LootTable.lootTable()
        .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
        	.add(LootItem.lootTableItem(Blocks.HIVE.asItem())
        		.apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)))));
		this.add(Blocks.BEE_NEST_NASCENT.get(),
		LootTable.lootTable()
        .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(this.hasNet())
        	.add(BeeFamilyPool.builder(GenerateGenomesFunction.builder().begin().setNatural()
        		.addPool(Constants.BASE_ID)
        		.addPool(Constants.FOREST_ID)
        		.add().build(), UniformGenerator.between(1,2), new ConstantValue(1), UniformGenerator.between(0,1)))));

		this.add(Blocks.BEE_NEST_SMALL.get(),
		LootTable.lootTable()
        .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(this.hasNet())
        	.add(BeeFamilyPool.builder(GenerateGenomesFunction.builder().begin().setNatural()
        		.addPool(Constants.BASE_ID)
        		.addPool(Constants.FOREST_ID)
        		.add().build(), UniformGenerator.between(2,3), new ConstantValue(1), UniformGenerator.between(1,2)))));

		this.add(Blocks.BEE_NEST_MEDIUM.get(),
		LootTable.lootTable()
        .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(this.hasNet())
        	.add(BeeFamilyPool.builder(GenerateGenomesFunction.builder().begin().setNatural()
        		.addPool(Constants.BASE_ID)
        		.addPool(Constants.FOREST_ID)
        		.add().build(), UniformGenerator.between(2,4), new ConstantValue(1), UniformGenerator.between(1,3)))));

		this.add(Blocks.BEE_NEST_BIG.get(),
		LootTable.lootTable()
        .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(this.hasNet())
        	.add(BeeFamilyPool.builder(GenerateGenomesFunction.builder().begin().setNatural()
        		.addPool(Constants.BASE_ID)
        		.addPool(Constants.FOREST_ID)
        		.add().build(), UniformGenerator.between(2,5), new ConstantValue(1), UniformGenerator.between(2,4)))));
	}

    protected LootItemCondition.Builder hasNet() {
        return MatchTool.toolMatches(ItemPredicate.Builder.item().of(this.registries.lookupOrThrow(Registries.ITEM), Tags.NET));
    }


	@Override
	protected Iterable<Block> getKnownBlocks() {

		return ()->Blocks.BLOCKS.getEntries().stream().map(t->(Block)t.get()).iterator();
	}
}
