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

import java.util.function.BiConsumer;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.client.BeeTint;
import com.khjxiaogu.beecrasy.client.model.GuiOnlySpecialModelWrapper;
import com.khjxiaogu.beecrasy.client.model.MailModel;
import com.khjxiaogu.beecrasy.menu.MailBoxMenu;

import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel.Unbaked;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;


public class BeecrasyItemModelProvider extends ItemModelGenerators {

	public BeecrasyItemModelProvider(ItemModelOutput itemModelOutput, BiConsumer<Identifier, ModelInstance> modelOutput) {
		super(itemModelOutput, modelOutput);
	}
	public static final ModelTemplate BEE_TEMPLATE=ModelTemplates.createItem("generated", TextureSlot.LAYER0, TextureSlot.LAYER1, TextureSlot.PARTICLE);
	@Override
	public void run() {
		this.generateBeeTint(Items.DRONE);
		this.generateBeeTint(Items.LARVA);
		this.generateBeeTint(Items.PRODUCT_COMB);
		this.generateBeeTint(Items.QUEEN_BEE);
		
		this.texture(Items.BEESWAX);
		this.texture(Items.COMB_FOUNDATION);
		this.texture(Items.HONEY_DROP);
		this.texture(Items.SEQUENCER);
	    this.texture(Items.INCENSE_ARIDITY);
	    this.texture(Items.INCENSE_HUMIDITY);
	    
	    this.texture(Items.INCENSE_COLD);
	    this.texture(Items.INCENSE_HEAT);
	    
	    this.texture(Items.INCENSE_YIELD);
	    this.texture(Items.INCENSE_LONG_LIFESPAN);
	    this.texture(Items.INCENSE_SHORT_LIFESPAN);
	    
	    textureFlower(Blocks.FLOWER_ASPHODEL);
	    textureFlower(Blocks.FLOWER_DELPHINIUM_BLUE);
	    textureFlower(Blocks.FLOWER_DELPHINIUM_PINK);
	    textureFlower(Blocks.FLOWER_DELPHINIUM_VIOLET);
	    textureFlower(Blocks.FLOWER_FOXTAIL_LILY);
	    textureFlower(Blocks.FLOWER_FOXTAIL_LILY_FIERY);
	    textureFlower(Blocks.FLOWER_HOLLYHOCK_PINK);
	    textureFlower(Blocks.FLOWER_HOLLYHOCK_RED);
	    textureFlower(Blocks.FLOWER_HOLLYHOCK_WHITE);
	    textureFlower(Blocks.FLOWER_PROTEA);
	    textureFlower(Blocks.FLOWER_PROTEA_ARTISAN);
		this.itemModelOutput.register(Beecrasy.rl("handheld_sequencer_active"), new ClientItem(ItemModelUtils.plainModel(ModelTemplates.FLAT_ITEM
			.create(Beecrasy.rl("item/"+"handheld_sequencer_active"), TextureMapping.layer0(new Material(
				Beecrasy.rl("item/"+"handheld_sequencer_active"))), this.modelOutput)),ClientItem.Properties.DEFAULT));
		this.texture(Items.APITE);
		this.texture(Items.BUMBLEBEE_JASPER);
		this.generateBeeTint(Items.PHEROMONO);
		this.texture(Items.BUTTERFLY_NET);
		this.texture(Items.ROYAL_JELLY);
		this.texture(Items.HONEY_BUCKET);
		this.texture(Items.MAILBOX);

		this.itemModelOutput.register(MailBoxMenu.MAILBOX_ACTIVE, new ClientItem(ItemModelUtils.plainModel(ModelTemplates.FLAT_ITEM
			.create(MailBoxMenu.MAILBOX_ACTIVE.withPrefix("item/"), TextureMapping.layer0(new Material(MailBoxMenu.MAILBOX_ACTIVE.withPrefix("item/"))), this.modelOutput)),ClientItem.Properties.DEFAULT));
		this.itemModelOutput.accept(  Items.MAIL.get()      ,               ItemModelUtils.composite(plain("letter" ),new GuiOnlySpecialModelWrapper.Unbaked(Beecrasy.rl("item/letter" ), new MailModel.Unbaked(7/16f,1-9/16f,.25f))));
		this.itemModelOutput.register(Beecrasy.rl("package"),new ClientItem(ItemModelUtils.composite(plain("package"),new GuiOnlySpecialModelWrapper.Unbaked(Beecrasy.rl("item/package"), new MailModel.Unbaked(4/16f,1-10/16f,.5f))),ClientItem.Properties.DEFAULT));

		this.itemModelOutput.accept(Items.MUSIC_SEIKILOS_EPITAPH.get(),ItemModelUtils.plainModel(Identifier.withDefaultNamespace("item/music_disc_13")));
		this.itemModelOutput.accept(Items.MUSIC_FLIGHT_OF_THE_BUMBLE_BEE.get(),ItemModelUtils.plainModel(Identifier.withDefaultNamespace("item/music_disc_13")));
		this.itemModelOutput.accept(Items.MUSIC_FLIGHT_OF_THE_BUMBLE_BEE_BITS.get(),ItemModelUtils.plainModel(Identifier.withDefaultNamespace("item/music_disc_13")));
		
	}
    public void generateBeeTint(ItemLike item) {
    	Identifier rkey=BuiltInRegistries.ITEM.getKey(item.asItem());
    	Identifier texture=rkey.withPrefix("item/");
    	Identifier overlay=texture.withSuffix("_overlay");
        Identifier model = BEE_TEMPLATE.create(item.asItem(), new TextureMapping().put(TextureSlot.LAYER0, mat(overlay)).put(TextureSlot.LAYER1, mat(texture)).put(TextureSlot.PARTICLE, mat(texture)), modelOutput);
        this.itemModelOutput.accept(item.asItem(), ItemModelUtils.tintedModel(model, new BeeTint()));
    }
    public Material mat(Identifier path) {
    	return new Material(path,false);
    }
	public void simpleTexture(String name, String par) {
		this.itemModelOutput.accept(BuiltInRegistries.ITEM.getValue(Beecrasy.rl(name)),
		ItemModelUtils.plainModel(ModelTemplates.FLAT_ITEM.create(Beecrasy.rl("item/" + name),new TextureMapping().put(TextureSlot.LAYER0, new Material(Beecrasy.rl("item/" + par + name),false)), this.modelOutput))
		);

	}
	public Unbaked plain(String name) {
		return plain(name,"");
	}
	public Unbaked plain(String name, String par) {
		return ItemModelUtils.plainModel(ModelTemplates.FLAT_ITEM.create(Beecrasy.rl("item/" + name),new TextureMapping().put(TextureSlot.LAYER0, new Material(Beecrasy.rl("item/" + par + name),false)), this.modelOutput))
		;

	}
	public void texture(DeferredItem<?> item) {
		texture(item.getId().getPath());
	}
	public void textureFlower(DeferredBlock<?> item) {
		this.itemModelOutput.accept(item.asItem(),
				ItemModelUtils.plainModel(ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(item.asItem()), TextureMapping.layer0(new Material(Identifier.fromNamespaceAndPath(Beecrasy.MODID, "block/"+item.getId().getPath()+"_top"))), this.modelOutput)
					));
	}
	public void texture(String name) {
		texture(name, name);
	}
	public void texture(Item name, String par) {

		this.itemModelOutput.accept(name,
			ItemModelUtils.plainModel(ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(name), TextureMapping.layer0(new Material(Identifier.fromNamespaceAndPath(Beecrasy.MODID, "item/"+par))), this.modelOutput)
				));
	}
	public void texture(String name, String par) {
		texture(BuiltInRegistries.ITEM.getValue(Beecrasy.rl(name)),par);
	}
}
