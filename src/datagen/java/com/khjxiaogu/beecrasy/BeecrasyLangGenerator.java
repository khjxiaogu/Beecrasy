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

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.genome.GeneRegistry;
import com.khjxiaogu.beecrasy.genome.Genes.Alleles;
import com.khjxiaogu.beecrasy.genome.gene.Allele;
import com.khjxiaogu.beecrasy.genome.gene.EnumAlleleType;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

public class BeecrasyLangGenerator extends LanguageProvider{

	public BeecrasyLangGenerator(PackOutput output, String modid, String locale) {
		super(output, modid, locale);
	}

	@Override
	protected void addTranslations() {
		for(Identifier id:GeneRegistry.getGeneTypes()) {
			if(id.getNamespace().equals(Beecrasy.MODID)) {
				this.add(GeneRegistry.get(id).getLanguageKey(), capitalizeWords(id.getPath())+": %s");
			}
			
		}
		addAllele(Alleles.TEMPERATURE);
		addAllele(Alleles.HUMIDITY);
		addAllele(Alleles.BIOTOPE);
		addAllele(Alleles.FERTILITY);
		addAllele(Alleles.LIFESPAN);
		addAllele(Alleles.YIELD);
		for(DeferredHolder<Block, ? extends Block> blk:Blocks.BLOCKS.getEntries()) {
			this.add(blk.get(), capitalizeWords(blk.getId().getPath()));
		}
		for(DeferredHolder<Item, ? extends Item> blk:Items.ITEMS.getEntries()) {
			this.add(blk.get(), capitalizeWords(blk.getId().getPath()));
		}
	}
	public <T extends Allele> void addAllele(EnumAlleleType<T> type) {
		for(T t:type) {
			this.add(type.getLanguageKey(t), capitalizeWords(t.getId()));
		}
	}
    public static String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        str=str.replaceAll("_", " ");
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true; // 下一个非空白字符需要大写

        for (char c : str.toCharArray()) {
            if (Character.isWhitespace(c)) {
                // 空白字符直接追加，并标记下一个非空白字符需要大写
                result.append(c);
                capitalizeNext = true;
            } else {
                // 非空白字符：如果需要大写则大写后追加，否则原样追加
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }
}
