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

import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

public class SequencerBlock extends Block implements BeecrasyEntityBlock<SequencerBlockEntity>{
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
	protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hitResult) {
		InteractionResult p = super.useItemOn(itemStack,state, level, pos, player, hand, hitResult);
		if (p.consumesAction())
			return p;
		if(level.getBlockEntity(pos) instanceof SequencerBlockEntity sequencer) {
			if (FluidUtil.interactWithFluidHandler(player, hand, pos, sequencer.tank))
				return InteractionResult.SUCCESS;
		}
		return p;
	}
    @Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
    	if(level instanceof ServerLevel) {
			if(level.getBlockEntity(pos) instanceof SequencerBlockEntity sequencer) {
				if (!level.isClientSide())
					((ServerPlayer) player).openMenu(sequencer);
				return InteractionResult.SUCCESS;
			}
    	}
		return super.useWithoutItem(state, level, pos, player, hitResult);
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
	@Override
	public DeferredHolder<BlockEntityType<?>, BlockEntityType<SequencerBlockEntity>> getBlock() {
		return Blocks.SEQUENCER_BLOCKENTITY;
	}

}
