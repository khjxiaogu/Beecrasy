/*
 * Copyright (c) 2024 TeamMoeg
 *
 * This file is part of Caupona.
 *
 * Caupona is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Caupona is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Specially, we allow this software to be used alongside with closed source software Minecraft(R) and Forge or other modloader.
 * Any mods or plugins can also use apis provided by forge or com.teammoeg.caupona.api without using GPL or open source.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caupona. If not, see <https://www.gnu.org/licenses/>.
 */

package com.khjxiaogu.beecrasy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.google.common.collect.ImmutableList;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;

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
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.Variant;
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

	public BeecrasyBlockModelProvider(ResourceManager input, Consumer<BlockModelDefinitionGenerator> blockStateOutput, ItemModelOutput itemModelOutput, BiConsumer<Identifier, ModelInstance> modelOutput,
		String modid) {
		super(blockStateOutput, itemModelOutput, modelOutput);
		this.modid = modid;
		this.input = input;
	}

	@Override
	public void run() {
		this.blockItemModel(Blocks.SKEP);
		this.blockStateOutput.accept(
			this.horizontalMultipart(bmf("skep_0"),t->t.term(BlockStateProperties.AGE_2, 0),
			this.horizontalMultipart(bmf("skep_1"),t->t.term(BlockStateProperties.AGE_2, 1),
			this.horizontalMultipart(bmf("skep_2"),t->t.term(BlockStateProperties.AGE_2, 2),
			this.getMultipartBuilder(Blocks.SKEP.get())))));
		
			
		this.blockItemModel(Blocks.SEQUENCER);
		this.blockStateOutput.accept(
		this.horizontalMultipart(bmf("sequencer"),
			this.getMultipartBuilder(Blocks.SEQUENCER.get())
			));
		this.blockItemModel(Blocks.HONEY_PRESS);
		MultiVariant pressModel=bmf("honey_press");
		MultiVariant empty=plainVariant(ModelTemplates.PARTICLE_ONLY.createWithSuffix(Blocks.HONEY_PRESS.get(),"_top", TextureMapping.particle(Blocks.HONEY_PRESS.get()), this.modelOutput));
		this.blockStateOutput.accept(
		this.getVariantBuilder(Blocks.HONEY_PRESS.get()).with(
			PropertyDispatch.initial(BlockStateProperties.DOUBLE_BLOCK_HALF, BlockStateProperties.HORIZONTAL_AXIS)

			.generate((half,axis)->{
				if(half==DoubleBlockHalf.LOWER) {
					return axis==Axis.Z?pressModel:pressModel.with(Y_ROT_90);
					
				}else
					return empty;
				
			})
		));
		
 
	}

	protected Empty getVariantBuilder(Block blk) {
		return MultiVariantGenerator.dispatch(blk);
	}
	private Block cpblock(String name) {
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
			blockItemModel(cpblock(n), Beecrasy.rl(n + p));
		}
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
		return input.getResource(id.withPrefix("models/").withSuffix(".json")).isPresent();

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

		}
		Beecrasy.LOGGER.warn("Model file " + orl + " not exists, using unchecked");
		return super.plainModel(rl);
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
