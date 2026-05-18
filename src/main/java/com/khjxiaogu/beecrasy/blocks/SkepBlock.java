package com.khjxiaogu.beecrasy.blocks;

import org.jspecify.annotations.Nullable;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;

public class SkepBlock extends Block {
	public SkepBlock(Properties properties) {
		super(properties);
		
	}
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.HORIZONTAL_FACING);
		builder.add(BlockStateProperties.AGE_2);
	}
	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		RandomSource rnd=new SingleThreadedRandomSource(context.getLevel().getGameTime()^context.getClickedPos().asLong());
		return this.defaultBlockState().setValue(BlockStateProperties.AGE_2, rnd.nextInt(3))
			.setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection());
	}
	
}
