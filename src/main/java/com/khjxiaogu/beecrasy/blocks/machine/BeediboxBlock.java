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

package com.khjxiaogu.beecrasy.blocks.machine;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.blocks.BeecrasyEntityBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class BeediboxBlock extends Block implements BeecrasyEntityBlock<BeediboxBlockEntity>{

	public BeediboxBlock(Properties properties) {
		super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.HAS_RECORD, false).setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.NORTH));
	}

	@Override
	public DeferredHolder<BlockEntityType<?>, BlockEntityType<BeediboxBlockEntity>> getBlock() {
		return Blocks.BEEDIBOX_BLOCKENTITY;
	}
	

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack) {
        super.setPlacedBy(level, pos, state, by, itemStack);
        if (level.getBlockEntity(pos) instanceof BeediboxBlockEntity box&&box.disk.getAmountAsInt(0)>0) {
            level.setBlock(pos, state.setValue(BlockStateProperties.HAS_RECORD, true), 2);
        }
    }
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState()
			.setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
	}
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (state.getValue(BlockStateProperties.HAS_RECORD) && level.getBlockEntity(pos) instanceof BeediboxBlockEntity jukebox) {
            jukebox.popOutTheItem();
            return InteractionResult.SUCCESS;
        }
		return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
    ) {
        if (state.getValue(BlockStateProperties.HAS_RECORD)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
		ItemStack toInsert = player.getItemInHand(hand);
		if(!toInsert.isEmpty()) {
	        if (!level.isClientSide()) {
	            
	            if (level.getBlockEntity(pos) instanceof BeediboxBlockEntity box) {
	            	try(Transaction trans=Transaction.openRoot()){
	            		if(box.disk.insert(0, ItemResource.of(toInsert), 1, trans)==1) {
	            			trans.commit();
	            			toInsert.consume(1, player);
	            			level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));
	            		}
	            	}
	            }
	
	            player.awardStat(Stats.PLAY_RECORD);
	        }
	
	        return InteractionResult.SUCCESS;
		}
		return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        Containers.updateNeighboursAfterDestroy(state, level, pos);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }


	@Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof BeediboxBlockEntity jukebox && jukebox.isPlaying() ? 15 : 0;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof BeediboxBlockEntity jukebox ? jukebox.getComparatorOutput() : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HAS_RECORD);
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }
}
