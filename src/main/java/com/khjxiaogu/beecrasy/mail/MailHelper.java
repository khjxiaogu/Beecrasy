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

package com.khjxiaogu.beecrasy.mail;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public class MailHelper {
	public static BlockPos findManhattanTopRandomPos(Level level,BlockPos pos,int radius,int mayRange) {
		final int x0=pos.getX()-radius,x1=pos.getX()+radius;
		final int y0=pos.getY()-radius,y1=pos.getY()+radius;
		final int z0=pos.getZ()-radius,z1=pos.getZ()+radius;
		BlockPos.MutableBlockPos mutable=new MutableBlockPos();
		List<BlockPos> availablePos=new ArrayList<>();
		for(int x=x0;x<=x1;x++) {
			for(int z=z0;z<=z1;z++) {
				mutable.set(x,y0, z);
				int heightY=level.getHeight(Types.MOTION_BLOCKING, mutable);
				if(heightY>y1) {
					continue;
				}
				mutable.set(x,heightY+1, z);
				availablePos.add(mutable.immutable());
			}
		}
		if(availablePos.isEmpty())
			return null;
		BlockPos nextpos=availablePos.get(level.getRandom().nextInt(availablePos.size()));
		int lenManhattan=nextpos.distManhattan(pos);
		if(lenManhattan>=mayRange)
			return nextpos;
		if(nextpos.getY()<pos.getY()) {
			int ylen=pos.getY()-nextpos.getY();
			if(ylen+lenManhattan>mayRange) {
				return nextpos.offset(0, lenManhattan-mayRange, 0);
			}
		}
		return nextpos.offset(0,mayRange-lenManhattan,0);
	}
}
