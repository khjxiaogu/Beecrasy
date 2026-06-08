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

package com.khjxiaogu.beecrasy.genome.gene;

import com.khjxiaogu.beecrasy.Beecrasy;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * 生境等位基因，关联一个方块标签用于检测世界中对应生境的方块。
 */
public class Biotope extends BaseAllele {
	/** 该生境关联的方块标签。 */
	final TagKey<Block> tag;
	/**
	 * 创建生境并自动生成对应的方块标签（{@code flowers/biotope_<id>}）。
	 *
	 * @param id 生境标识符
	 */
	public Biotope(String id) {
		super(id);
		tag=BlockTags.create(Beecrasy.rl("flowers/biotope_"+id));
	}
	/**
	 * 获取关联的方块标签。
	 *
	 * @return 方块标签
	 */
	public TagKey<Block> getTag() {
		return tag;
	}
	

}
