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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.google.common.collect.ImmutableList;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.blocks.bee.BeeNestBlock;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator.Empty;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.registries.DeferredBlock;

public class BeecrasyBlockModelProvider extends BlockModelGenerators {
	protected static final List<Vec3i> COLUMN_THREE = ImmutableList.of(BlockPos.ZERO, BlockPos.ZERO.above(),
		BlockPos.ZERO.above(2));
	protected static final Map<Identifier, String> generatedParticleTextures = new HashMap<>();
	String modid;
	ResourceManager input;
	public static final Set<Identifier> generatedModels=new HashSet<>();
	public BeecrasyBlockModelProvider(ResourceManager input, Consumer<BlockModelDefinitionGenerator> blockStateOutput, ItemModelOutput itemModelOutput, BiConsumer<Identifier, ModelInstance> modelOutput,
		String modid) {
		super(blockStateOutput, itemModelOutput,(rl,m)->{
			generatedModels.add(rl);
			modelOutput.accept(rl, m);
			});
		this.modid = modid;
		this.input = input;
	}
	public static final TextureSlot PLANT_TOP=TextureSlot.create("plant_top");
	public static final TextureSlot PLANT_BOTTOM=TextureSlot.create("plant_bottom");
    public static final ModelTemplate FLOWER_POT_DOUBLE_CROSS = ModelTemplates.create(Beecrasy.rl("flower_pot_double_cross").toString(), PLANT_TOP, PLANT_BOTTOM);
    public void createPlantPot(DeferredBlock<?> standAlone, Block potted) {
        MultiVariant model = plainVariant(FLOWER_POT_DOUBLE_CROSS.create(potted, new TextureMapping().put(PLANT_TOP, new Material(standAlone.getId().withPrefix("block/").withSuffix("_top")))
        		.put(PLANT_BOTTOM, new Material(standAlone.getId().withPrefix("block/").withSuffix("_bottom"))), this.modelOutput));
        this.blockStateOutput.accept(createSimpleBlock(potted, model));
    }
	@Override
	public void run() {
		this.blockItemModel(Blocks.SKEP);
		this.blockStateOutput.accept(
			this.getVariantBuilder(Blocks.SKEP.get()).with(
				PropertyDispatch.initial(BlockStateProperties.AGE_2)
				.generate(t->bmf("skep_"+t))
			).with(ROTATION_HORIZONTAL_FACING)
			);
		for(int i=0;i<Blocks.POTTED_FLOWERS.size();i++) {
			createPlantPot(Blocks.FLOWERS.get(i),Blocks.POTTED_FLOWERS.get(i).get());
		}
		this.blockStateOutput.accept(this.getVariantBuilder(Blocks.EMPTY_COMB_BLOCK.get(),genBlock("empty_comb_block")));
		this.blockItemModel(Blocks.EMPTY_COMB_BLOCK);
		this.blockStateOutput.accept(this.getVariantBuilder(Blocks.HONEY_COMB_BLOCK.get(),genBlock("honey_comb_block")));
		this.blockItemModel(Blocks.HONEY_COMB_BLOCK);
		this.blockItemModel(Blocks.SEQUENCER);
		this.blockStateOutput.accept(
			this.getVariantBuilder(Blocks.SEQUENCER.get()).with(PropertyDispatch.initial(BlockStateProperties.LIT)
				.generate(t->t?bmf("sequencer_active"): bmf("sequencer")))
			.with(ROTATION_HORIZONTAL_FACING)
			);
		this.blockItemModel(Blocks.HIVE);
		this.blockStateOutput.accept(
			this.getVariantBuilder(Blocks.HIVE.get(), bmf("hive"))
			.with(ROTATION_HORIZONTAL_FACING)
			);
		this.simpleBlockItem(Blocks.BEE_CITY_CORE.get());
		this.simpleBlockItem(Blocks.BUZZER.get(), Beecrasy.rl("buzzer"));
		this.blockStateOutput.accept(this.getVariantBuilder(Blocks.BEEDIBOX.get()).with(PropertyDispatch.initial(BlockStateProperties.HAS_RECORD)
				.generate(t->t?bmf("beedibox_active"): bmf("beedibox"))
				
				).with(ROTATION_HORIZONTAL_FACING));
		
		this.blockItemModel(Blocks.BEE_CITY_COMB.get(),Beecrasy.rl("bee_city_empty_comb"));
		this.blockStateOutput.accept(this.getVariantBuilder(Blocks.BEE_CITY_COMB.get())
			.with(PropertyDispatch.initial(BlockStateProperties.LIT)
				.generate(t->t?genBlock("bee_city_honey_comb"):genBlock("bee_city_empty_comb"))
				));
		this.blockItemModel(Blocks.BEE_CITY_QUEEN.get(),Beecrasy.rl("bee_city_queen_cell"));
		this.blockStateOutput.accept(this.getVariantBuilder(Blocks.BEE_CITY_QUEEN.get(),bmf("bee_city_queen_cell"))
			.with(ROTATION_FACING));
		this.itemModelOutput.accept(Blocks.HONEY_PRESS.asItem(), 
			ItemModelUtils.composite(
				this.plainBlockModel(Beecrasy.rl("honey_press_base")),
				this.plainBlockModel(Beecrasy.rl("dynamic/honey_press_plate")),
				this.plainBlockModel(Beecrasy.rl("dynamic/honey_press_screw"))
				));
		MultiVariant pressModel=bmf("honey_press_base");
		MultiVariant empty=plainVariant(ModelTemplates.PARTICLE_ONLY.createWithSuffix(Blocks.HONEY_PRESS.get(),"_top", TextureMapping.particle(Blocks.HONEY_PRESS.get()), this.modelOutput));
		this.blockStateOutput.accept(
		this.getVariantBuilder(Blocks.HONEY_PRESS.get()).with(
			PropertyDispatch.initial(BlockStateProperties.DOUBLE_BLOCK_HALF)
			.generate((half)->half==DoubleBlockHalf.LOWER?pressModel:empty)
		).with(ROTATION_HORIZONTAL_FACING));
		for(String s:List.of("small","nascent","medium","big")) {
			this.blockStateOutput.accept(
					this.getVariantBuilder(modBlock("bee_nest_"+s)).with(PropertyDispatch.initial(BeeNestBlock.BEE_NEST_FACING,BeeNestBlock.HAS_HONEY)
							.generate((facing,honey)->bmf("bee_nest_"+(honey?"with_honey_":"")+facing.getSerializedName()+"_"+s))
					).with(ROTATION_HORIZONTAL_FACING));
			blockItemModel(modBlock("bee_nest_"+s), Beecrasy.rl("bee_nest_with_honey_ceiling_"+s));
		}
		this.blockStateOutput.accept(
			this.getVariantBuilder(Blocks.NATURAL_HIVE.get()).with(PropertyDispatch.initial(BlockStateProperties.AGE_2)
					.select(0, bmf("bee_nest_ceiling_nascent").with(X_ROT_180))
					.select(1, bmf("bee_nest_ceiling_small").with(X_ROT_180))
					.select(2, bmf("bee_nest_with_honey_ceiling_small").with(X_ROT_180))
			).with(ROTATION_HORIZONTAL_FACING));
		blockItemModel(Blocks.NATURAL_HIVE.get(), Beecrasy.rl("bee_nest_ceiling_small"));
		
	}

	protected Empty getVariantBuilder(Block blk) {
		return MultiVariantGenerator.dispatch(blk);
	}
	protected MultiVariantGenerator getVariantBuilder(Block blk,MultiVariant model) {
		return MultiVariantGenerator.dispatch(blk,model);
	}
	private Block modBlock(String name) {
		return BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath(this.modid, name));
	}
	protected void blockItemModel(DeferredBlock<?> n) {
		blockItemModel(n.getId().getPath(), "");
	}
	protected void blockItemModel(String n) {
		blockItemModel(n, "");
	}
	public void simpleTexture(String name, String par) {
		this.itemModelOutput.accept(BuiltInRegistries.ITEM.getValue(Beecrasy.rl(name)),
		ItemModelUtils.plainModel(ModelTemplates.FLAT_ITEM.create(Beecrasy.rl("item/" + name),new TextureMapping().put(TextureSlot.LAYER0, new Material(Beecrasy.rl("item/" + par + name),false)), this.modelOutput))
		);

	}
	public void simpleTexture(Item item) {
		this.itemModelOutput.accept(item,
		ItemModelUtils.plainModel(this.createFlatItemModel(item))
		);
	}
	public void texture(String name) {
		texture(name, name);
	}
	public void texture(Item name, String par) {
		this.itemModelOutput.accept(name,
			ItemModelUtils.plainModel(ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(name), TextureMapping.layer0(new Material(Beecrasy.rl("item/"+par))), this.modelOutput)
				));
	}
	public void texture(String name, String par) {
		texture(BuiltInRegistries.ITEM.getValue(Beecrasy.rl(name)),par);
	}

	protected void blockItemModel(String n, String p) {
		if (input.getResource(Identifier.fromNamespaceAndPath(Beecrasy.MODID, "textures/item/" + n + p + ".png")).isPresent()) {

			texture(n, n + p);
		} else {
			blockItemModel(modBlock(n), Beecrasy.rl(n + p));
		}
	}
	protected ItemModel.Unbaked plainBlockModel(Identifier blockModelId) {
		return ItemModelUtils.plainModel(blockModelId.withPrefix("block/"));
	}
	protected void blockItemModel(Block n, Identifier p) {
		Identifier blockModelId=p.withPrefix("block/");
		String name=p.getPath();
		if(existsModel(blockModelId)) {

			this.itemModelOutput.accept(n.asItem(), ItemModelUtils.plainModel(blockModelId));
		}else {
			List<String> rn = Arrays.asList(name.split("_"));
			for (int i = rn.size(); i >= 0; i--) {
				List<String> rrn = new ArrayList<>(rn);
				rrn.add(i, "0");
				blockModelId = Identifier.fromNamespaceAndPath(this.modid, "block/" + String.join("_", rrn));
				if (existsModel(blockModelId)) {
					this.itemModelOutput.accept(n.asItem(), ItemModelUtils.plainModel(blockModelId));
					return;
				}
			}
			
		
			throw new IllegalArgumentException("model does not exists: "+p);
		}
	}
	public boolean existsModel(Identifier id) {
		if(generatedModels.contains(id))
			return true;
		return input.getResource(id.withPrefix("models/").withSuffix(".json")).isPresent();

	}
	public boolean existsTexture(Identifier id) {
		return input.getResource(id.withPrefix("textures/").withSuffix(".png")).isPresent();

	}


	public MultiVariant bmf(String name) {
		return super.variant(bmfs(name));
	}
	
	public Variant bmfs(String name) {
		Identifier orl = Identifier.fromNamespaceAndPath(this.modid, "block/" + name);
		Identifier rl = orl;

		if (!existsModel(rl)) {// not exists, let's guess
			List<String> rn = Arrays.asList(name.split("_"));
			for (int i = rn.size(); i >= 0; i--) {
				List<String> rrn = new ArrayList<>(rn);
				rrn.add(i, "0");
				rl = Identifier.fromNamespaceAndPath(this.modid, "block/" + String.join("_", rrn));
				if (existsModel(rl))
					return super.plainModel(rl);
			}
			Beecrasy.LOGGER.warn("Model file " + orl + " not exists, using unchecked");
		}
		
		return super.plainModel(rl);
	}
	public MultiVariant genBlock(String name) {
		Identifier orl = Identifier.fromNamespaceAndPath(this.modid, "block/" + name);
		Identifier rl = orl;

		if (!existsModel(rl)) {// not exists, let's generate
			List<Variant> ids=new ArrayList<>();
			
			int i=0;
			while (true) {

				rl = Identifier.fromNamespaceAndPath(this.modid, "block/" + name + "_"+i);
				if (existsModel(rl))
					ids.add(super.plainModel(rl));
				else if (existsTexture(rl))
					ids.add(super.plainModel(ModelTemplates.CUBE_ALL.create(rl, TextureMapping.cube(new Material(rl,false)), modelOutput)));
				else
					break;
				i++;
			}
			

			return super.variants(ids.toArray(Variant[]::new));
		}
		return super.plainVariant(rl);
	}
	public MultiVariant bmf(Identifier name) {
		return super.variant(bmfs(name));
	}

	public Variant bmfs(Identifier orl) {
		return super.plainModel(orl);
	}

	protected void simpleBlockItem(Block b, Identifier model) {
		this.blockStateOutput.accept(createSimpleBlock(b, bmf(model.withPrefix("block/"))));
		blockItemModel(b, model);
	}
	protected void simpleBlockItem(Block b) {
		this.blockStateOutput.accept(createSimpleBlock(b, bmf(BuiltInRegistries.BLOCK.getKey(b).getPath())));
		blockItemModel(b, BuiltInRegistries.BLOCK.getKey(b));
	}
	public void horizontalAxisBlock(Block block, MultiVariant mf) {

		this.blockStateOutput
			.accept(getVariantBuilder(block).with(PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_AXIS)
				.select(Axis.Z, mf)
				.select(Axis.X, mf.with(Y_ROT_90))));

	}

	public MultiPartGenerator horizontalMultipart( MultiVariant variant,MultiPartGenerator generator) {
		forEachHorizontalDirection((direction, rotation) -> generator.with(condition(BlockStateProperties.HORIZONTAL_FACING, direction), variant.with(rotation)));
		return generator;
	}

	public MultiPartGenerator horizontalMultipart(MultiVariant variant,
		UnaryOperator<ConditionBuilder> act,MultiPartGenerator generator) {
		forEachHorizontalDirection((direction, rotation) -> generator.with(act.apply(condition(BlockStateProperties.HORIZONTAL_FACING, direction)), variant.with(rotation)));

		return generator;
	}

	protected MultiPartGenerator getMultipartBuilder(Block block) {
		return MultiPartGenerator.multiPart(block);
	}
	

}
