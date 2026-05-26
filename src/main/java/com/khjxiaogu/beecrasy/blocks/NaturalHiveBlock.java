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

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.events.NaturalBeeGenomeGenerateEvent;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.GenomeDataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.common.NeoForge;

public class NaturalHiveBlock extends Block{

	public NaturalHiveBlock(Properties properties) {
		super(properties);
		// TODO Auto-generated constructor stub
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
		BlockPos pos = BlockPos.containing(params.getParameter(LootContextParams.ORIGIN));
		ServerLevel level = params.getLevel();
		NaturalBeeGenomeGenerateEvent event = new NaturalBeeGenomeGenerateEvent(level, pos, level.getBiome(pos), state, Genome.builder());
		NeoForge.EVENT_BUS.post(event);
		List<ItemStack> loot = super.getDrops(state, params);
		Genome genome = event.genome.build();
		@Nullable ItemInstance tool=params.getOptionalParameter(LootContextParams.TOOL);
		if(tool!=null&&tool.count()>0) {
			ItemStack drone = Items.DRONE.toStack(2);
			GenomeDataHelper.setHaploidGenome(drone, genome);
			loot.add(drone);
		}
		ItemStack queen = Items.QUEEN_BEE.toStack();
		GenomeDataHelper.setDiploidGenome(queen, genome, genome);
		loot.add(queen);

		return loot;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		return isValidSupport(level, below, level.getBlockState(below));
	}

	public static boolean isValidSupport(LevelReader level, BlockPos above, BlockState aboveState) {
		return Block.isFaceFull(aboveState.getCollisionShape(level, above), Direction.UP);
	}

	@SuppressWarnings("resource")
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
	}
}
