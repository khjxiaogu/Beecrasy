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
import com.khjxiaogu.beecrasy.client.BeecrasyParticles;
import com.khjxiaogu.beecrasy.utils.Utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class HiveBlock extends Block  implements BeecrasyEntityBlock<HiveBlockEntity>{
	public HiveBlock(Properties properties) {
		super(properties);
		
	}
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.HORIZONTAL_FACING);
		builder.add(BlockStateProperties.LIT);
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		RandomSource rnd=Utils.getSyncedRandom(context.getPlayer());
		return this.defaultBlockState().setValue(BlockStateProperties.LIT, false)
			.setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
	}
	@Override
	public DeferredHolder<BlockEntityType<?>, BlockEntityType<HiveBlockEntity>> getBlock() {
		return Blocks.HIVE_BLOCKENTITY;
	}
    @Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
    	if(level instanceof ServerLevel) {
			if(level.getBlockEntity(pos) instanceof HiveBlockEntity blockEntity) {
				if (!level.isClientSide())
					((ServerPlayer) player).openMenu(blockEntity);
				
			}
    	}
    	return InteractionResult.SUCCESS;
	}

    @Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
    	List<ItemStack> list=super.getDrops(state, params);
		if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof HiveBlockEntity blockEntity) {
			ItemStacksResourceHandler inv=blockEntity.component.getInternInv();
			for (int i = 14; i < inv.size(); i++) {
				ItemResource is = inv.getResource(i);
				if (!is.isEmpty()) {
					list.add(is.toStack(inv.getAmountAsInt(i)));
				}
			}
			
			list.add(blockEntity.getItem());
		}else {
			list.add(new ItemStack(Blocks.HIVE.asItem()));
		}
		return list;
	}
    
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		super.animateTick(state, level, pos, random);
		if(state.getValue(BlockStateProperties.LIT)) {
			int count = 2;
			if(random.nextInt(20)==0)
			level.playLocalSound(pos, SoundEvents.BEE_LOOP, SoundSource.BLOCKS, Mth.lerp(random.nextFloat(), 0.0f, 0.5f), Mth.lerp(random.nextFloat(), 0.7f, 1.1f), true);
			while (--count != 0) {
				
				Direction dir=Direction.Plane.HORIZONTAL.getRandomDirection(random);
				for(int i=0;i<4;i++) {
					BlockPos moved=pos.relative(dir);
					if(level.getBlockState(moved).isFaceSturdy(level, moved, dir.getOpposite())) {
						dir=dir.getClockWise();
					}
				}
				BlockPos moved=pos.relative(dir);
				if(!level.getBlockState(moved).isFaceSturdy(level, moved, dir.getOpposite())) {
					double dx=0,dy=0,dz=0;
					if(dir.getAxis()!=Axis.X) {
						dx=random.nextGaussian()*0.5;
					}
					if(dir.getAxis()!=Axis.Y) {
						dy=random.nextGaussian()*0.5;
					}
					if(dir.getAxis()!=Axis.Z) {
						dz=random.nextGaussian()*0.5;
					}
					Vec3 speedvec=dir.getUnitVec3().scale(0.02);
					Vec3 mpos=pos.getCenter().add(dir.getUnitVec3().scale(0.5f)).add(dx, dy, dz);
						level.addParticle(BeecrasyParticles.BEE.get().random(), mpos.x(), mpos.y(),mpos.z(), speedvec.x(),
								0.0D, speedvec.z());
				}
			}
		}
	}
	@Override
	public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData,
			Player player) {
		return (level.getBlockEntity(pos) instanceof HiveBlockEntity blockEntity) ? blockEntity.getItem() : super.getCloneItemStack(level, pos, state, includeData,player);
	}
}
