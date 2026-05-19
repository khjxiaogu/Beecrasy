package com.khjxiaogu.beecrasy.blocks;

import java.util.List;
import java.util.function.BiConsumer;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PressBlock extends Block {

	private static final VoxelShape CYLINDER = Block.box( 3,  8,  3, 13,  16, 13);
	private static final VoxelShape CYLINDER_TOP = Block.box( 2,  12,  2, 14,  16, 14);
	private static final VoxelShape BASE  = Block.box( 1,  1,  1, 15,  8, 15);
	private static final VoxelShape FOOT1 = Block.box( 0,  0,  0,  3,  3,  3);
	private static final VoxelShape FOOT2 = Block.box(13,  0, 13, 16,  3, 16);
	private static final VoxelShape FOOT3 = Block.box(13,  0,  0, 16,  3,  3);
	private static final VoxelShape FOOT4 = Block.box( 0,  0, 13,  3,  3, 16);
	private static final VoxelShape BOTTOM=Shapes.or(BASE, FOOT1,FOOT2,FOOT3,FOOT4,CYLINDER,CYLINDER_TOP);
	
	private static final VoxelShape HANDLE      = Block.box( 5,  0,  5, 11, 16, 11);
	private static final VoxelShape HANDLE_BASE = Block.box( 2,  0,  2, 14,  3, 14);
	private static final VoxelShape HANDLE_TOP  = Block.box( 2, 12,  2, 14, 16, 14);
	private static final VoxelShape TOP = Shapes.or(HANDLE_BASE, HANDLE,HANDLE_TOP);
	public PressBlock(Properties properties) {
		super(properties);
		   this.registerDefaultState(
	            this.stateDefinition
	                .any()
	                .setValue(BlockStateProperties.HORIZONTAL_AXIS, Axis.X)
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
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
    	DoubleBlockHalf half = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
    	if(level instanceof ServerLevel serverLevel) {
	    	if(half==DoubleBlockHalf.UPPER) {
	    		onTrigger(state,serverLevel,pos);
	    		return InteractionResult.SUCCESS_SERVER;
	    	}
    	}
		return super.useWithoutItem(state, level, pos, player, hitResult);
	}

	@Override
    protected void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> onHit) {
        if (explosion.canTriggerBlocks() && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
            this.onTrigger(state, level, pos);
        }

        super.onExplosionHit(state, level, pos, explosion, onHit);
    }
    public void onTrigger(BlockState state, ServerLevel level, BlockPos pos) {
    	
    }

    @Override
	protected List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder params) {
		
		return super.getDrops(state, params);
	}

	@Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() < level.getMaxY() && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_AXIS, context.getHorizontalDirection().getAxis())
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        } else {
            return null;
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack) {
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
        return state.cycle(BlockStateProperties.HORIZONTAL_AXIS);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return mirror == Mirror.NONE ? state : state.cycle(BlockStateProperties.HORIZONTAL_AXIS);
    }
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)==DoubleBlockHalf.UPPER?TOP:BOTTOM;
	}
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.HORIZONTAL_AXIS);
		builder.add(BlockStateProperties.DOUBLE_BLOCK_HALF);
	}
}
