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
	static final VoxelShape BASE      = Block.box( 0,  0,  0, 16,  4, 16);
	static final VoxelShape MACHINE   = Block.box( 5,  4,  1, 15, 16, 11);
	static final VoxelShape SCREEN    = Block.box( 4,  4, 10, 16, 14, 16);
	static final VoxelShape ANALYZER1 = Block.box( 0, 11,  2,  5, 15,  6);
	static final VoxelShape ANALYZER2 = Block.box( 0,  6,  2,  5, 10,  6);
	static final VoxelShape ALL=Shapes.or(BASE, SCREEN,MACHINE,ANALYZER1,ANALYZER2);
	static final Map<Direction,VoxelShape> SHAPE_BY_FACING=Shapes.rotateHorizontal(ALL);
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
