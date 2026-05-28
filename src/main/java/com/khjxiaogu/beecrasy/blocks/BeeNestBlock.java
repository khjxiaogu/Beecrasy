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

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.events.NaturalBeeGenomeGenerateEvent;
import com.khjxiaogu.beecrasy.genome.Genes;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.GenomeDataHelper;
import com.khjxiaogu.beecrasy.genome.ProductHelper;
import com.khjxiaogu.beecrasy.genome.ProductHelper.ProductWithCount;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.NeoForge;

public class BeeNestBlock extends Block {
	public static enum Facing implements StringRepresentable {
		CEILING,
		CORNER;

		@Override
		public String getSerializedName() {
			return name().toLowerCase();
		}
	}

	public static final EnumProperty<Facing> BEE_NEST_FACING = EnumProperty.create("faces", Facing.class);
	public static final BooleanProperty HAS_HONEY = BooleanProperty.create("honey");
	protected final int combCountMin, combCountMax;

	public BeeNestBlock(Properties properties, int combCountMin, int combCountMaxExclusive) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
				.setValue(BEE_NEST_FACING, Facing.CEILING)
				.setValue(HAS_HONEY, false));

		this.combCountMin = combCountMin;
		this.combCountMax = combCountMaxExclusive;
	}
	public static final VoxelShape shape=Block.box(3, 3, 3, 13, 16, 13);
	@Override
	protected BlockState updateShape(
		BlockState state,
		LevelReader level,
		ScheduledTickAccess ticks,
		BlockPos pos,
		Direction directionToNeighbour,
		BlockPos neighbourPos,
		BlockState neighbourState,
		RandomSource random) {
		if (directionToNeighbour == Direction.UP)
			return isValidSupport(level, neighbourPos, neighbourState)
				? state
				: Blocks.AIR.defaultBlockState();
		if (state.getValue(BEE_NEST_FACING) == Facing.CORNER) {
			Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
			if (directionToNeighbour == facing.getClockWise()) {
				return getFacedState(level, pos, state);
			}

		}
		return state;

	}

	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		BlockPos pos = BlockPos.containing(params.getParameter(LootContextParams.ORIGIN));
		ServerLevel level = params.getLevel();
		NaturalBeeGenomeGenerateEvent event = new NaturalBeeGenomeGenerateEvent(level, pos, state, Genome.builder());
		NeoForge.EVENT_BUS.post(event);
		List<ItemStack> loot = super.getDrops(state, params);
		Genome genome = event.genome.build();
		@Nullable ItemInstance tool=params.getOptionalParameter(LootContextParams.TOOL);
		if(tool!=null&&tool.count()>0) {
			ItemStack drone = Items.DRONE.toStack(2);
			GenomeDataHelper.setHaploidGenome(drone, genome);
			loot.add(drone);
		}
		ItemStack queen = Items.QUEEN_BEE.toStack();
		GenomeDataHelper.setDiploidGenome(queen, genome, genome);
		loot.add(queen);
		List<ProductItem> product = genome.getAllele(Genes.PRODUCTS);
		if (!product.isEmpty()) {
			for (ProductWithCount i : ProductHelper.pickProduct(genome.getAllele(Genes.BIOTOPE), product, level.getRandom(), Mth.lerpInt(level.getRandom().nextFloat(), combCountMin, combCountMax))) {
				loot.add(i.createProductComb());
			}
		}
		return loot;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos above = pos.above();
		return isValidSupport(level, above, level.getBlockState(above));
	}

	public static boolean isValidSupport(LevelReader level, BlockPos above, BlockState aboveState) {
		return Block.isFaceFull(aboveState.getCollisionShape(level, above), Direction.DOWN);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		BlockPos above = pos.above();
		Level level = context.getLevel();
		if (isValidSupport(level, above, level.getBlockState(above))) {
			Direction facing = context.getHorizontalDirection().getOpposite();
			return getFacedState(level, pos, this.defaultBlockState()
				.setValue(BlockStateProperties.HORIZONTAL_FACING, facing));
		}
		return null;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return shape;
	}

	public static BlockState getFacedState(LevelReader level, BlockPos pos, BlockState state) {
		Direction nxtFacing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
		for (int i = 0; i < 4; i++) {
			BlockPos nxtPos = pos.relative(nxtFacing);

			if (Block.isFaceFull(level.getBlockState(nxtPos).getCollisionShape(level, nxtPos), nxtFacing.getOpposite())) {
				return state.setValue(BlockStateProperties.HORIZONTAL_FACING, nxtFacing.getCounterClockWise()).setValue(BEE_NEST_FACING, Facing.CORNER);
			}
			nxtFacing = nxtFacing.getCounterClockWise();
		}
		return state.setValue(BEE_NEST_FACING, Facing.CEILING);
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
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.HORIZONTAL_FACING);
		builder.add(BEE_NEST_FACING);
		builder.add(HAS_HONEY);
	}
}
