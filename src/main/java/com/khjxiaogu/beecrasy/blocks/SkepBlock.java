package com.khjxiaogu.beecrasy.blocks;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.utils.Utils;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SkepBlock extends Block {
	private static final VoxelShape SKEP2 = Block.box( 2,  0,  2, 14,  12, 14);

	private static final VoxelShape SKEP1 = Shapes.or(Block.box( 1,  0,  1, 15,  12, 15), Block.box( 3,  12,  3, 13,  14, 13));
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
	}
	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		RandomSource rnd=Utils.getSyncedRandom(context.getPlayer());
		return this.defaultBlockState().setValue(BlockStateProperties.AGE_2, rnd.nextInt(3))
			.setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
	}
	
}
