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

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.client.BeecrasyParticles;
import com.khjxiaogu.beecrasy.events.NaturalBeeGenomeGenerateEvent;
import com.khjxiaogu.beecrasy.genome.Genes;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.GenomeDataHelper;
import com.khjxiaogu.beecrasy.genome.GenomeWorkHelper;
import com.khjxiaogu.beecrasy.genome.GenomeWorkHelper.ProductWithCount;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
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
	//nascent
	public static final VoxelShape SHAPE11=Block.box(5, 12, 4, 11, 16, 8);
	public static final VoxelShape SHAPE12=Block.box(4, 12, 8, 12, 16, 12);
	public static final VoxelShape SHAPE13=Block.box(5, 8, 5, 11, 12, 7);
	public static final VoxelShape SHAPE14=Block.box(4, 8, 9, 12, 12, 11);
	public static final VoxelShape NASCENT_SHAPE=Shapes.or(SHAPE11, SHAPE12, SHAPE13, SHAPE14);
	public static final VoxelShape SHAPE15=Block.box(10, 12, 4, 16, 16, 8);
	public static final VoxelShape SHAPE16=Block.box(11, 12, 8, 16, 16, 12);
	public static final VoxelShape SHAPE17=Block.box(8, 8, 5, 16, 15, 7);
	public static final VoxelShape SHAPE18=Block.box(10, 9, 9, 16, 14, 11);
	
	public static final VoxelShape NASCENT_CORNER=Shapes.or(SHAPE15, SHAPE16, SHAPE17, SHAPE18).optimize();
	//small
	public static final VoxelShape SHAPE21=Block.box(4, 12, 2, 12, 16, 10);
	public static final VoxelShape SHAPE22=Block.box(5, 12, 10, 11, 16, 14);
	public static final VoxelShape SHAPE23=Block.box(4, 8, 3, 12, 12, 5);
	public static final VoxelShape SHAPE24=Block.box(4, 6, 7, 12, 12, 9);
	public static final VoxelShape SHAPE25=Block.box(5, 9, 11, 11, 12, 13);
	public static final VoxelShape SMALL_SHAPE=Shapes.or(SHAPE21, SHAPE22, SHAPE23, SHAPE24, SHAPE25);

	public static final VoxelShape SHAPE26=Block.box(11, 12, 2, 16, 16, 6);
	public static final VoxelShape SHAPE27=Block.box(10, 12, 6, 16, 16, 14);
	
	public static final VoxelShape SHAPE28=Block.box(10, 9, 3, 16, 14, 5);
	public static final VoxelShape SHAPE29=Block.box(8, 6, 7, 16, 14, 9);
	public static final VoxelShape SHAPE20=Block.box(8, 8, 11, 16, 15, 13);

	public static final VoxelShape SMALL_CORNER=Shapes.or(SHAPE26, SHAPE27, SHAPE28, SHAPE29, SHAPE20).optimize();
	//medium
	public static final VoxelShape SHAPE31=Block.box(4, 12, 2, 12, 16, 15);
	public static final VoxelShape SHAPE32=Block.box(3, 12, 6, 13, 16, 11);
	public static final VoxelShape SHAPE33=Block.box(4, 6, 3, 12, 12, 5);
	public static final VoxelShape SHAPE34=Block.box(3, 3, 7, 13, 12, 10);
	public static final VoxelShape SHAPE35=Block.box(4, 8, 12, 12, 12, 14);
	public static final VoxelShape MEDIUM_SHAPE=Shapes.or(SHAPE31, SHAPE32, SHAPE33, SHAPE34, SHAPE35).optimize();
	
	public static final VoxelShape SHAPE36=Block.box(10, 12, 2, 16, 16, 15);
	public static final VoxelShape SHAPE37=Block.box(9, 12, 6, 16, 16, 11);
	public static final VoxelShape SHAPE38=Block.box(8, 8, 3, 16, 14, 5);
	public static final VoxelShape SHAPE39=Block.box(6, 3, 7, 16, 14, 10);
	public static final VoxelShape SHAPE30=Block.box(8, 6, 12, 16, 14, 14);
	public static final VoxelShape MEDIUM_CORNER=Shapes.or(SHAPE36, SHAPE37, SHAPE38, SHAPE39, SHAPE30).optimize();
	//large
	public static final VoxelShape SHAPE41=Block.box(3, 12, 1, 13, 16, 11);
	public static final VoxelShape SHAPE42=Block.box(4, 12, 11, 12, 16, 15);
	public static final VoxelShape SHAPE43=Block.box(3, 3, 2, 13, 12, 5);
	public static final VoxelShape SHAPE44=Block.box(3, 3, 7, 13, 12, 10);
	public static final VoxelShape SHAPE45=Block.box(4, 6, 12, 12, 12, 14);
	public static final VoxelShape LARGE_SHAPE=Shapes.or(SHAPE41, SHAPE42, SHAPE43, SHAPE44, SHAPE45);
	public static final VoxelShape SHAPE46=Block.box(10, 12, 1, 16, 16, 5);
	public static final VoxelShape SHAPE47=Block.box(9, 12, 5, 16, 16, 15);
	public static final VoxelShape SHAPE48=Block.box(8, 6, 2, 16, 14, 4);
	public static final VoxelShape SHAPE49=Block.box(6, 3, 6, 16, 14, 9);
	public static final VoxelShape SHAPE40=Block.box(6, 3, 11, 16, 14, 14);
	public static final VoxelShape LARGE_CORNER=Shapes.or(SHAPE46, SHAPE47, SHAPE48, SHAPE49, SHAPE40);
	
	public static final EnumProperty<Facing> BEE_NEST_FACING = EnumProperty.create("faces", Facing.class);
	public static final BooleanProperty HAS_HONEY = BooleanProperty.create("honey");
	protected final int combCountMin, combCountMax;
	protected final Map<Direction, VoxelShape> corner,ceiling;
	public BeeNestBlock(Properties properties, int combCountMin, int combCountMaxExclusive,VoxelShape ceiling,VoxelShape corner) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
				.setValue(BEE_NEST_FACING, Facing.CEILING)
				.setValue(HAS_HONEY, false));

		this.combCountMin = combCountMin;
		this.combCountMax = combCountMaxExclusive;
		this.ceiling = Shapes.rotateHorizontal(ceiling);
		this.corner = Shapes.rotateHorizontal(corner);
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

	@SuppressWarnings("resource")
	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		BlockPos pos = BlockPos.containing(params.getParameter(LootContextParams.ORIGIN));
		ServerLevel level = params.getLevel();
		NaturalBeeGenomeGenerateEvent event = new NaturalBeeGenomeGenerateEvent(level, pos, Genome.builder());
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
			for (ProductWithCount i : GenomeWorkHelper.pickProduct(genome.getAllele(Genes.BIOTOPE), product, level.getRandom(), Mth.lerpInt(level.getRandom().nextFloat(), combCountMin, combCountMax))) {
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

	@SuppressWarnings("resource")
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
		Direction facing=state.getValue(BlockStateProperties.HORIZONTAL_FACING);
		return state.getValue(BEE_NEST_FACING)==Facing.CEILING?ceiling.get(facing):corner.get(facing);
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
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		super.animateTick(state, level, pos, random);
		
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
