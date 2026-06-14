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
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyRegistries;
import com.khjxiaogu.beecrasy.beehive.slot.StacksHiveSlot;
import com.khjxiaogu.beecrasy.client.BeecrasyParticles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredHolder;

public class NaturalHiveBlock extends Block implements BeecrasyEntityBlock<NaturalHiveBlockEntity>{
	public static final VoxelShape SHAPE=Block.box(4, 0, 6, 12, 4, 14);
	public static final VoxelShape SHAPE0=Block.box(5, 0, 2, 11, 4, 6);
	public static final VoxelShape SHAPE1=Block.box(4, 4, 11, 12, 8, 13);
	public static final VoxelShape SHAPE2=Block.box(4, 4, 7, 12, 10, 9);
	public static final VoxelShape SHAPE3=Block.box(5, 4, 3, 11, 7, 5);
	public static final VoxelShape SHAPE11=Block.box(4, 0, 4, 12, 4, 12);
	public static final VoxelShape SHAPE12=Block.box(4, 4, 5, 12, 8, 7);
	public static final VoxelShape SHAPE13=Block.box(4, 4, 9, 12, 8, 11);
	private static final Map<Direction,VoxelShape> SHAPE_BY_FACING2=Shapes.rotateHorizontal(Shapes.or(SHAPE,SHAPE0,SHAPE1,SHAPE2,SHAPE3));
	private static final Map<Direction,VoxelShape> SHAPE_BY_FACING1=Shapes.rotateHorizontal(Shapes.or(SHAPE11,SHAPE12,SHAPE13));
	public NaturalHiveBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
				.setValue(BlockStateProperties.AGE_2, 0));
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
		RandomSource random) {
		if (directionToNeighbour == Direction.DOWN)
			return isValidSupport(level, neighbourPos, neighbourState)
				? state
				: Blocks.AIR.defaultBlockState();
		return state;

	}

    @Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
    	List<ItemStack> list=super.getDrops(state, params);
		if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof NaturalHiveBlockEntity hive) {
			if(hive.isGrowthStarted&&!hive.hiveInfo.isWorking()) {
				for(StacksHiveSlot slot:hive.queenSlot) {
					list.add(slot.getItem());
				}
				for(StacksHiveSlot slot:hive.combSlot) {
					list.add(slot.getItem());
				}
				for(StacksHiveSlot slot:hive.droneSlot) {
					list.add(slot.getItem());
				}
			}else {
				list.add(hive.queen.copy());
			}
		}
		return list;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		return isValidSupport(level, below, level.getBlockState(below));
	}

	public static boolean isValidSupport(LevelReader level, BlockPos above, BlockState aboveState) {
		return Block.isFaceFull(aboveState.getCollisionShape(level, above), Direction.UP);
	}

	@SuppressWarnings("resource")
	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		BlockPos below = pos.below();
		Level level = context.getLevel();
		if (isValidSupport(level, below, level.getBlockState(below))) {
			Direction facing = context.getHorizontalDirection().getOpposite();
			return this.defaultBlockState()
				.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
		}
		return null;
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
		builder.add(BlockStateProperties.AGE_2);
	}
	@Override
	public DeferredHolder<BlockEntityType<?>, BlockEntityType<NaturalHiveBlockEntity>> getBlock() {
		return BeecrasyRegistries.Blocks.NATURAL_HIVE_BLOCKENTITY;
	}
	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(BlockStateProperties.AGE_2)==0?SHAPE_BY_FACING1.get(state.getValue(BlockStateProperties.HORIZONTAL_FACING)):SHAPE_BY_FACING2.get(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
	}
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		super.animateTick(state, level, pos, random);
		if(random.nextInt(30)==0)
		level.playLocalSound(pos, SoundEvents.BEE_LOOP, SoundSource.BLOCKS, Mth.lerp(random.nextFloat(), 0.0f, 0.5f), Mth.lerp(random.nextFloat(), 0.7f, 1.1f), true);
		if (random.nextFloat()<0.25) {
			Direction dir=Direction.Plane.HORIZONTAL.getRandomDirection(random);
			for(int i=0;i<4;i++) {
				BlockPos moved=pos.relative(dir);
				if(level.getBlockState(moved).isFaceSturdy(level, moved, dir.getOpposite())) {
					dir=dir.getClockWise();
				}
			}
			double dx=0,dy=0,dz=0;
			if(dir.getAxis()!=Axis.X) {
				dx=random.nextGaussian();
			}
			if(dir.getAxis()!=Axis.Y) {
				dy=random.nextGaussian();
			}
			if(dir.getAxis()!=Axis.Z) {
				dz=random.nextGaussian();
			}
			Vec3 speedvec=dir.getUnitVec3().scale(0.02);
			Vec3 mpos=pos.getCenter().add(dir.getUnitVec3().scale(0.5f)).add(dx, dy, dz);
				level.addParticle(BeecrasyParticles.BEE.get().random(), mpos.x(), mpos.y(),mpos.z(), speedvec.x(),
						0.0D, speedvec.z());
		}
		
	}
}
