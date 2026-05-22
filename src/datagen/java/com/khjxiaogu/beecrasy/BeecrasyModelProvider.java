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

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class BeecrasyModelProvider extends ModelProvider {
	ResourceManager resource;
	public BeecrasyModelProvider(PackOutput output, String modId,ResourceManager resource) {
		super(output, modId);
		this.resource=resource;
	}
    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        new BeecrasyBlockModelProvider(resource, blockModels.blockStateOutput,blockModels.itemModelOutput,blockModels.modelOutput,modId).run();
        new BeecrasyItemModelProvider(blockModels.itemModelOutput,blockModels.modelOutput).run();
    
    }
    protected java.util.stream.Stream<? extends net.minecraft.core.Holder<Block>> getKnownBlocks() {
    	//return Stream.empty();
        return BeecrasyRegistries.Blocks.BLOCKS.getEntries().stream().filter(t->!resource.getResource(t.getId().withPrefix("blockstates/").withSuffix(".json")).isPresent());
    }

    protected java.util.stream.Stream<? extends net.minecraft.core.Holder<Item>> getKnownItems() {
    	return BeecrasyRegistries.Items.ITEMS.getEntries().stream();
    }
}
