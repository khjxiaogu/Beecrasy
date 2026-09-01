/*
 * Copyright (c) 2024 TeamMoeg
 *
 * This file is part of Caupona.
 *
 * Caupona is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Caupona is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Specially, we allow this software to be used alongside with closed source software Minecraft(R) and Forge or other modloader.
 * Any mods or plugins can also use apis provided by forge or com.teammoeg.caupona.api without using GPL or open source.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caupona. If not, see <https://www.gnu.org/licenses/>.
 */

package com.khjxiaogu.beecrasy.worldgen;

import java.util.HashSet;
import java.util.Set;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.WorldGen;
import com.khjxiaogu.beecrasy.utils.BeecrasyMath;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class UnderLeavesPlacer extends TreeDecorator {
	public static final MapCodec<UnderLeavesPlacer> CODEC = RecordCodecBuilder.mapCodec(t ->
	t.group(BlockStateProvider.CODEC.fieldOf("block").forGetter(o->o.state)).apply(t, UnderLeavesPlacer::new)
		);
	BlockStateProvider state;
	public UnderLeavesPlacer(BlockStateProvider state) {
		super();
		this.state = state;
	}

	@Override
	protected TreeDecoratorType<?> type() {
		return WorldGen.UNDER_LEAVES_PLACER.get();
	}

	@Override
	public void place(Context pContext) {
		Set<BlockPos> leaves=new HashSet<>(pContext.leaves());
		Set<BlockPos> available=new HashSet<>();
		for(BlockPos bp:leaves){
			if(pContext.isAir(bp)) {
				available.add(bp);
			}
		}
		BlockPos pos=BeecrasyMath.getRandomElement(available, pContext.random());
		BlockState outState=state.getState(pContext.level(), pContext.random(), pos);
		if(outState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
			outState=outState.setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.Plane.HORIZONTAL.getRandomDirection(pContext.random()));
		}
		pContext.setBlock(pos, outState);
	}

}
