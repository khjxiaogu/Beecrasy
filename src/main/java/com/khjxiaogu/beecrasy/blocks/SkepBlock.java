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

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.utils.Utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class SkepBlock extends Block  implements BeecrasyEntityBlock<SkepBlockEntity>{
	private static final VoxelShape SKEP2 = Block.box( 2,  0,  2, 14,  12, 14);

	private static final VoxelShape SKEP1 = Shapes.or(Block.box( 1,  0,  1, 15,  12, 15), Block.box( 3,  12,  3, 13,  14, 13)).optimize();
	public SkepBlock(Properties properties) {
		super(properties);
		
	}
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return state.getValue(BlockStateProperties.AGE_2)==2?SKEP2:SKEP1;
	}
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.HORIZONTAL_FACING);
		builder.add(BlockStateProperties.AGE_2);
		builder.add(BlockStateProperties.LIT);
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		RandomSource rnd=Utils.getSyncedRandom(context.getPlayer());
		return this.defaultBlockState().setValue(BlockStateProperties.AGE_2, rnd.nextInt(3))
			.setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
	}
	@Override
	public DeferredHolder<BlockEntityType<?>, BlockEntityType<SkepBlockEntity>> getBlock() {
		return Blocks.SKEP_BLOCKENTITY;
	}
    @Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
    	if(level instanceof ServerLevel) {
			if(level.getBlockEntity(pos) instanceof SkepBlockEntity blockEntity) {
				if (!level.isClientSide())
					((ServerPlayer) player).openMenu(blockEntity);
				return InteractionResult.SUCCESS;
			}
    	}
		return super.useWithoutItem(state, level, pos, player, hitResult);
	}

    @Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
    	List<ItemStack> list=super.getDrops(state, params);
		if (params.getParameter(LootContextParams.BLOCK_ENTITY) instanceof SkepBlockEntity blockEntity) {
			ItemStacksResourceHandler inv=blockEntity.component.getInternInv();
			for (int i = 0; i < inv.size(); i++) {
				ItemResource is = inv.getResource(i);
				if (!is.isEmpty()) {
					list.add(is.toStack(inv.getAmountAsInt(i)));
				}
			}
		}
		return list;
	}
}
