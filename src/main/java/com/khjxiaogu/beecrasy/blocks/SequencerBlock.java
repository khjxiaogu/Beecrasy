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
package com.khjxiaogu.beecrasy.blocks;

import java.util.Map;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SequencerBlock extends Block {
	private static final VoxelShape BASE      = Block.box( 0,  0,  0, 16,  4, 16);
	private static final VoxelShape MACHINE   = Block.box( 5,  4,  1, 15, 16, 11);
	private static final VoxelShape SCREEN    = Block.box( 4,  4, 10, 16, 14, 16);
	private static final VoxelShape ANALYZER1 = Block.box( 0, 11,  2,  5, 15,  6);
	private static final VoxelShape ANALYZER2 = Block.box( 0,  6,  2,  5, 10,  6);
	private static final VoxelShape ALL=Shapes.or(BASE, SCREEN,MACHINE,ANALYZER1,ANALYZER2).optimize();
	private static final Map<Direction,VoxelShape> SHAPE_BY_FACING=Shapes.rotateHorizontal(ALL);
	public SequencerBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE_BY_FACING.get(state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite());
	}
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.HORIZONTAL_FACING);
		builder.add(BlockStateProperties.LIT);
	}
	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {

		return this.defaultBlockState().setValue(BlockStateProperties.LIT, false)
			.setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
	}

}
