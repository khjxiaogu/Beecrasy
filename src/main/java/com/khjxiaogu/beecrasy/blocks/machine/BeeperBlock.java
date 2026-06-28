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

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Sounds;
import com.khjxiaogu.beecrasy.utils.BeecrasyMath;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

public class BeeperBlock extends Block {

	public BeeperBlock(Properties properties) {
		super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.NOTE, 0).setValue(BlockStateProperties.POWERED, false));
	}

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,Orientation orientation, boolean movedByPiston) {
        boolean signal = level.hasNeighborSignal(pos);
        if (signal != state.getValue(BlockStateProperties.POWERED)) {
            if (signal) {
                this.playNote(null, state, level, pos);
            }

            level.setBlock(pos, state.setValue(BlockStateProperties.POWERED, signal), 3);
        }
    }

    /**
	 * @param state  
	 */
    private void playNote(Entity source, BlockState state, Level level, BlockPos pos) {
        if (level.getBlockState(pos.above()).isAir()) {
            level.blockEvent(pos, this, 0, 0);
            level.gameEvent(source, GameEvent.NOTE_BLOCK_PLAY, pos);
        }
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
    ) {
        return itemStack.is(ItemTags.NOTE_BLOCK_TOP_INSTRUMENTS) && hitResult.getDirection() == Direction.UP
            ? InteractionResult.PASS
            : super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            int _new = net.neoforged.neoforge.common.CommonHooks.onNoteChange(level, pos, state, state.getValue(BlockStateProperties.NOTE), state.cycle(BlockStateProperties.NOTE).getValue(BlockStateProperties.NOTE));
            if (_new == -1) return InteractionResult.FAIL;
            state = state.setValue(BlockStateProperties.NOTE, _new);
            level.setBlock(pos, state, 3);
            this.playNote(player, state, level, pos);
            player.awardStat(Stats.TUNE_NOTEBLOCK);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide()) {
            this.playNote(player, state, level, pos);
            player.awardStat(Stats.PLAY_NOTEBLOCK);
        }
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int b0, int b1) {

        int note = state.getValue(BlockStateProperties.NOTE);
        float pitch = BeecrasyMath.noteToPitch(note-12+60+2);
        level.addParticle(ParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, note / 24.0, 0.0, 0.0);


        level.playSeededSound(
            null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, Sounds.BEE_NOTE.get(), SoundSource.RECORDS, 1.0F, pitch, level.getRandom().nextLong()
        );
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.NOTE,BlockStateProperties.POWERED);
    }
}
