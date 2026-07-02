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

import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig.Entry;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Fluids;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterRegistry.BeehiveParameterType;
import com.khjxiaogu.beecrasy.beehive.BeeHiveParameters;
import com.khjxiaogu.beecrasy.beehive.ErrCode;
import com.khjxiaogu.beecrasy.beehive.WorkBehaviour;
import com.khjxiaogu.beecrasy.compat.ChanceCallback;
import com.khjxiaogu.beecrasy.compat.category.PressCategory;
import com.khjxiaogu.beecrasy.genome.GeneRegistry;
import com.khjxiaogu.beecrasy.genome.Genes.Alleles;
import com.khjxiaogu.beecrasy.genome.gene.Allele;
import com.khjxiaogu.beecrasy.genome.gene.EnumAlleleType;
import com.khjxiaogu.beecrasy.item.MailBoxItem;
import com.khjxiaogu.beecrasy.mail.LetterStatus;

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
				this.add(GeneRegistry.get(id).getLanguageKey(), capitalizeWordSnakeCase(id.getPath()));
				this.add(GeneRegistry.get(id).getShortLanguageKey(), capitalizeWordSnakeCase(id.getPath()).substring(0,Math.min(id.getPath().length(), 4)));
			}
			
		}
		addAllele(Alleles.TEMPERATURE);
		addAllele(Alleles.HUMIDITY);
		addAllele(Alleles.BIOTOPE);
		addAllele(Alleles.FERTILITY);
		addAllele(Alleles.LIFESPAN);
		addAllele(Alleles.YIELD);
		addArgumenter(BeeHiveParameters.MUTATE);
		addArgumenter(BeeHiveParameters.SPEED);
		addArgumenter(BeeHiveParameters.LIFESPAN);
		addArgumenter(BeeHiveParameters.YIELD);
		addArgumenter(BeeHiveParameters.TEMPERATURE);
		addArgumenter(BeeHiveParameters.HUMIDITY);
		addArgumenter(BeeHiveParameters.FERTILITY);
		addMultilineArgumenter(BeeHiveParameters.MUTATION_DIRECTOR);
		addArgumenter(BeeHiveParameters.RADIUS);
		sequencerTab("basic");
		sequencerTab("products");
		for(DeferredHolder<Block, ? extends Block> blk:Blocks.BLOCKS.getEntries()) {
			this.add(blk.get(), capitalizeWordSnakeCase(blk.getId().getPath()));
		}
		for(DeferredHolder<Item, ? extends Item> blk:Items.ITEMS.getEntries()) {
			this.add(blk.get(), capitalizeWordSnakeCase(blk.getId().getPath()));
		}
		for(ErrCode err:ErrCode.values()) {
			this.add(err.getTranslationKey(), capitalizeWordSnakeCase(err.name().toLowerCase()));
		}
		for(WorkBehaviour work:WorkBehaviour.values()) {
			this.add(work.getTranslationKey(), capitalizeWordSnakeCase(work.name().toLowerCase()));
		}
		for(LetterStatus ls:LetterStatus.values()) {
			if(ls!=LetterStatus.OK) {
				this.add(ls.transKey,capitalizeWordSnakeCase(ls.name().toLowerCase()));
			}
		}
		this.add("message.postal.mail_recived", "You got mail.");
		this.add("message.postal.mail_recived_count", "You have %s mail.");
		this.add("tooltip.correspondence.sender", "By %s");
		this.add("tooltip.correspondence.receiver", "To %s");
		this.add(MailBoxItem.IN_PROGRESS, "Deliver in progress.");
		this.add(MailBoxItem.NOT_EXPOSE, "You should be in open air to receive mail.");
		this.add(MailBoxItem.NOT_VALID_PATH, "No vaild path for delivering.");
		this.add(PressCategory.titleId, "Honey Press");
		this.add(ChanceCallback.titleId, "Chance: %s");
		this.addCd("flight_of_the_bumble_bee");
		this.addCd("seikilos_epitaph");

		this.add(Fluids.HONEY.get().getDescriptionId());

		addConfig("",BeecrasyConfig.SERVER_CONFIG.getSpec());
		
		/*addConfig("beecrasy.configuration.beehive");
		addConfig(BeecrasyConfig.SERVER.LIFESPAN.getPath());
		addConfig(BeecrasyConfig.SERVER.INTERVAL.getPath());
		addConfig(BeecrasyConfig.SERVER.RADIUS.getPath());
		addConfig(BeecrasyConfig.SERVER.FLOWER_RADIUS.getPath());
		addConfig(BeecrasyConfig.SERVER.FLOWER_RATE.getPath());

		addConfig("beecrasy.configuration.bees");
		addConfig(BeecrasyConfig.SERVER.LARVA_SURVIVE_SECS.getPath());
		addConfig(BeecrasyConfig.SERVER.MUTATION_CHANCE.getPath());

		addConfig("beecrasy.configuration.sequencer");
		addConfig(BeecrasyConfig.SERVER.SEQUENCER_HONEY.getPath());
		addConfig(BeecrasyConfig.SERVER.SEQUENCER_ENERGY.getPath());
		addConfig(BeecrasyConfig.SERVER.SEQUENCER_THROUGHPUT.getPath());*/
	}
	public <T extends Allele> void addAllele(EnumAlleleType<T> type) {
		for(T t:type) {
			this.add(type.getLanguageKey(t), capitalizeWordSnakeCase(t.getId()));
			this.add(type.getShortLanguageKey(t), capitalizeWordSnakeCase(t.getId()).substring(0,Math.min(t.getId().length(), 4)));
		}
	}
	public void add(String name) {
		String tname=name;
		if(name.contains("."))
			tname=name.substring(name.lastIndexOf(".")+1);
		this.add(name, capitalizeWordSnakeCase(tname));
	}
	public void addCd(String name) {
		this.add(Beecrasy.rl(name).toLanguageKey("record", "title"), capitalizeWordSnakeCase(name));
	}
	public void addConfig(String parent,UnmodifiableConfig cfg) {
		for(Entry ent:cfg.entrySet()) {
			addConfig(ent.getKey());
			if(ent.getValue() instanceof UnmodifiableConfig cfg2) {
				addConfig(parent+ent.getKey()+".",cfg2);
			}
		}
	}
	public void addConfig(String path) {
		String name=path;
		if(path.contains("."))
			name=path.substring(path.lastIndexOf("."));
		this.add("beecrasy.configuration." + path, capitalizeWordCamelCase(name));
	}
	public void addConfig(List<String> path) {
		this.add("beecrasy.configuration." + path.stream().reduce("",(a,b)->a+"."+b), capitalizeWordCamelCase(path.get(path.size()-1)));
	}
	public void sequencerTab(String name) {
		this.add("tab.sequencer.beecrasy."+name, capitalizeWordCamelCase(name));
	}
	public <T> void addArgumenter(BeehiveParameterType<T> type) {
		
		this.add(BeeHiveParameters.getLanguageKey(type.id()), capitalizeWordSnakeCase(type.id().getPath())+" %s");
	
	}
	public <T> void addMultilineArgumenter(BeehiveParameterType<T> type) {
		
		this.add(BeeHiveParameters.getLanguageKey(type.id()), capitalizeWordSnakeCase(type.id().getPath()));
	
	}
	
    public static String capitalizeWordSnakeCase(String str) {
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
    public static String capitalizeWordCamelCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true; // 下一个非空白字符需要大写
        boolean lastCapitalized=true;
        for (char c : str.toCharArray()) {
            if (Character.isWhitespace(c)) {
                // 空白字符直接追加，并标记下一个非空白字符需要大写
                result.append(c);
                capitalizeNext = true;
                lastCapitalized=true;
            } else {
                // 非空白字符：如果需要大写则大写后追加，否则原样追加
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                    lastCapitalized=true;
                } else {
                	if(Character.isUpperCase(c)&&!lastCapitalized) {//小写变大写，添加空格
                		result.append(" ");
                	}
                    result.append(c);
                    lastCapitalized=Character.isUpperCase(c);
                }
            }
        }
        return result.toString();
    }
}
