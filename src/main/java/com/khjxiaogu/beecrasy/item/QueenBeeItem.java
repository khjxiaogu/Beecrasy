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

package com.khjxiaogu.beecrasy.item;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.blocks.NaturalHiveBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class QueenBeeItem extends Item {

	public QueenBeeItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
		boolean updated = super.onEntityItemUpdate(stack, entity);
		if(updated)
			return true;
		if(entity.getAge()>200) {
			if(entity.onGround()) {
				BlockPos pos=entity.getOnPos().above();
				if(entity.level().getBlockState(pos).canBeReplaced()) {
					BlockState state=Blocks.NATURAL_HIVE.get().defaultBlockState();
					if(state.canSurvive(entity.level(), pos)) {
						entity.level().setBlock(pos, state, Block.UPDATE_ALL_IMMEDIATE);
						if(entity.level().getBlockEntity(pos) instanceof NaturalHiveBlockEntity be) {
							be.setQueen(stack.split(1));
							return true;
						}
					}
				}
			}
		}
		return false;
	}


}
