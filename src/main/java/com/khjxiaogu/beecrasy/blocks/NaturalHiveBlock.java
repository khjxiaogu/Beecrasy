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

package com.khjxiaogu.beecrasy.blocks;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyRegistries;
import com.khjxiaogu.beecrasy.beehive.slot.StacksHiveSlot;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.registries.DeferredHolder;

public class NaturalHiveBlock extends Block implements BeecrasyEntityBlock<NaturalHiveBlockEntity>{

	public NaturalHiveBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
				.setValue(BlockStateProperties.AGE_2, 0));
	}
	@Override
	protected BlockState updateShape(
		BlockState state,
		LevelReader level,
		ScheduledTickAccess ticks,
		BlockPos pos,
		Direction directionToNeighbour,
		BlockPos neighbourPos,
		BlockState neighbourState,
		RandomSource random) {
		if (directionToNeighbour == Direction.DOWN)
			return isValidSupport(level, neighbourPos, neighbourState)
				? state
				: Blocks.AIR.defaultBlockState();
		return state;

	}

    @Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
    	List<ItemStack> list=super.getDrops(state, params);
		if (params.getParameter(LootContextParams.BLOCK_ENTITY) instanceof NaturalHiveBlockEntity hive) {
			if(hive.isGrowthStarted&&!hive.hiveInfo.isWorking()) {
				for(StacksHiveSlot slot:hive.queenSlot) {
					list.add(slot.getItem().copy());
				}
				for(StacksHiveSlot slot:hive.combSlot) {
					list.add(slot.getItem().copy());
				}
				for(StacksHiveSlot slot:hive.droneSlot) {
					list.add(slot.getItem().copy());
				}
			}else {
				list.add(hive.queen.copy());
			}
		}
		return list;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		return isValidSupport(level, below, level.getBlockState(below));
	}

	public static boolean isValidSupport(LevelReader level, BlockPos above, BlockState aboveState) {
		return Block.isFaceFull(aboveState.getCollisionShape(level, above), Direction.UP);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		BlockPos below = pos.below();
		Level level = context.getLevel();
		if (isValidSupport(level, below, level.getBlockState(below))) {
			Direction facing = context.getHorizontalDirection().getOpposite();
			return this.defaultBlockState()
				.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
		}
		return null;
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return mirror == Mirror.NONE ? state : state.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite());
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.HORIZONTAL_FACING);
		builder.add(BlockStateProperties.AGE_2);
	}
	@Override
	public DeferredHolder<BlockEntityType<?>, BlockEntityType<NaturalHiveBlockEntity>> getBlock() {
		return BeecrasyRegistries.Blocks.NATURAL_HIVE_BLOCKENTITY;
	}
}
