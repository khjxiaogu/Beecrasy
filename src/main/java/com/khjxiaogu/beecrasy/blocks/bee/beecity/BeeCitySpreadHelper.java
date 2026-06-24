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

package com.khjxiaogu.beecrasy.blocks.bee.beecity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Capability;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Tags;
import com.khjxiaogu.beecrasy.blocks.HiveSlotProvider;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BeeCitySpreadHelper {
	public static Pair<BlockPos,Direction> findFirstValid(Level level,BlockPos core,BlockPos current,Set<BlockPos> poss) {
		List<Direction> dirs=new ArrayList<>(List.of(Direction.values()));
		Collections.shuffle(dirs);
		for(Direction dir:dirs) {
			BlockPos cur=current.relative(dir);
			if(!poss.contains(cur)) {
				BlockState bs=level.getBlockState(cur);
				if(bs.is(Tags.BEECITY_SPREADABLE)||(level.getCapability(Capability.BEE_CITY_BLOCK,cur) instanceof HiveSlotProvider provider&&provider.isBindable(core))) {
					for(Direction dir2:Direction.values()) {
						BlockPos cur2=cur.relative(dir2);
						BlockState bs2=level.getBlockState(cur2);
						if(bs2.canBeReplaced()||bs2.isEmpty()||!bs2.isCollisionShapeFullBlock(level, current)) {
							return Pair.of(cur, dir2);
						}
					}
				}
				if(bs.canBeReplaced()||bs.isEmpty()||!bs.isCollisionShapeFullBlock(level, current)) {
					cur=current.relative(dir,2);
					BlockState bs3=level.getBlockState(cur);
					if(bs3.is(Tags.BEECITY_SPREADABLE)||(level.getCapability(Capability.BEE_CITY_BLOCK,cur) instanceof HiveSlotProvider provider&&provider.isBindable(core))) {
						for(Direction dir2:Direction.values()) {
							BlockPos cur2=cur.relative(dir2);
							BlockState bs2=level.getBlockState(cur2);
							if(bs2.canBeReplaced()||bs2.isEmpty()||!bs2.isCollisionShapeFullBlock(level, current)) {
								return Pair.of(cur, dir2);
							}
						}
					}
				}
			}
		}
		return null;
	}
}
