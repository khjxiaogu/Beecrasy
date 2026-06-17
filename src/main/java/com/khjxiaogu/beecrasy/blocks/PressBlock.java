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
import java.util.function.BiConsumer;

import com.khjxiaogu.beecrasy.BeecrasyRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class PressBlock extends Block implements BeecrasyEntityBlock<PressBlockEntity>{

	private static final VoxelShape CYLINDER = Block.box( 3,  8,  3, 13,  16, 13);
	private static final VoxelShape CYLINDER_TOP = Block.box( 2,  13,  2, 14,  16, 14);
	private static final VoxelShape BASE  = Block.box( 1,  1,  1, 15,  8, 15);
	private static final VoxelShape FOOT1 = Block.box( 0,  0,  0,  3,  3,  3);
	private static final VoxelShape FOOT2 = Block.box(13,  0, 13, 16,  3, 16);
	private static final VoxelShape FOOT3 = Block.box(13,  0,  0, 16,  3,  3);
	private static final VoxelShape FOOT4 = Block.box( 0,  0, 13,  3,  3, 16);
	private static final VoxelShape BOTTOM=Shapes.or(BASE, FOOT1,FOOT2,FOOT3,FOOT4,CYLINDER,CYLINDER_TOP).optimize();
	
	private static final VoxelShape HANDLE      = Block.box( 5,  0,  5, 11, 16, 11);
	private static final VoxelShape HANDLE_BASE = Block.box( 2,  0,  2, 14,  3, 14);
	//private static final VoxelShape HANDLE_TOP  = Block.box( 2, 12,  2, 14, 16, 14);
	private static final VoxelShape TOP = Shapes.or(HANDLE_BASE, HANDLE).optimize();
	
	private static final VoxelShape ALL=Shapes.or(TOP.move(0, 1, 0), BOTTOM).optimize();
	private static final VoxelShape ALL_TOP=Shapes.or(HANDLE).optimize();
	public PressBlock(Properties properties) {
		super(properties);
		   this.registerDefaultState(
	            this.stateDefinition
	                .any()
	                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
	                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)
	        );
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
        RandomSource random
    ) {
        DoubleBlockHalf half = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
        if(directionToNeighbour.getAxis()==Axis.Y&&
        	((half == DoubleBlockHalf.LOWER) == (directionToNeighbour == Direction.UP))) {

            return  neighbourState.getBlock()==state.getBlock()&&neighbourState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)!=half
                ? state
                : Blocks.AIR.defaultBlockState();
        }

        return state;
        
    }
	@Override
	protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hitResult) {
		InteractionResult p = super.useItemOn(itemStack,state, level, pos, player, hand, hitResult);
		if (p.consumesAction())
			return p;
		if(level.getBlockEntity(pos) instanceof PressBlockEntity press) {
			if (itemStack.isEmpty() && player.isShiftKeyDown()) {
				press.tank.set(0,FluidResource.EMPTY,0);
				return InteractionResult.SUCCESS;
			}
			if (FluidUtil.interactWithFluidHandler(player, hand, pos, press.getExternTank()))
				return InteractionResult.SUCCESS;
		}
		return p;
	}
    @Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
    	DoubleBlockHalf half = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
    	if(level instanceof ServerLevel serverLevel) {
	    	if(half==DoubleBlockHalf.UPPER) {
	    		onTrigger(serverLevel,pos);
	    	}
			if(level.getBlockEntity(pos) instanceof PressBlockEntity press) {
				if (!level.isClientSide())
					((ServerPlayer) player).openMenu(press);
			}
    	}
    	return InteractionResult.SUCCESS;
	}

	@Override
    protected void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> onHit) {
		
        if (explosion.canTriggerBlocks() && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
            this.onTrigger(level, pos);
        }

        super.onExplosionHit(state, level, pos, explosion, onHit);
    }
    public void onTrigger(ServerLevel level, BlockPos pos) {
    	if(level.getBlockEntity(pos.below()) instanceof PressBlockEntity press) {
    		press.refillPower();
    	}
    }

    @Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
    	List<ItemStack> list=super.getDrops(state, params);
		if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER&&params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof PressBlockEntity press) {
			ItemStacksResourceHandler inv=press.getInternInv();
			for (int i = 0; i < inv.size(); i++) {
				ItemResource is = inv.getResource(i);
				if (!is.isEmpty()) {
					list.add(is.toStack(inv.getAmountAsInt(i)));
				}
			}
		}
		return list;
	}


	@SuppressWarnings("resource")
	@Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() < level.getMaxY() && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite())
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        }
		return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity by, ItemStack itemStack) {
        level.setBlock(pos.above(), state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), 3);
    }


    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? true : belowState.is(this);
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
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		
		return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)==DoubleBlockHalf.UPPER?ALL_TOP:ALL;
	}
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)==DoubleBlockHalf.UPPER?TOP:BOTTOM;
    }
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.HORIZONTAL_FACING);
		builder.add(BlockStateProperties.DOUBLE_BLOCK_HALF);
	}
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		if(state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)==DoubleBlockHalf.LOWER)
			return getBlock().get().create(pos, state);
		return null;
	}





	@Override
	public DeferredHolder<BlockEntityType<?>, BlockEntityType<PressBlockEntity>> getBlock() {
		return BeecrasyRegistries.Blocks.PRESS_BLOCKENTITY;
	}







}
