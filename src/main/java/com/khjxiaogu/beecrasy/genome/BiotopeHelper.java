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

package com.khjxiaogu.beecrasy.genome;

import java.util.HashSet;
import java.util.Set;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Tags;
import com.khjxiaogu.beecrasy.genome.gene.Biotope;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public class BiotopeHelper {
	private BiotopeHelper() {
		
	}
	
	public static Set<Biotope> findBiotope(Level l,BlockPos pos,int radius){
		Set<Biotope> bts=new HashSet<>();
		boolean hasFlower=false;
		final int x0=pos.getX()-radius,x1=pos.getX()+radius;
		final int y0=pos.getY()-radius,y1=pos.getY()+radius;
		final int z0=pos.getZ()-radius,z1=pos.getZ()+radius;
		BlockPos.MutableBlockPos mutable=new MutableBlockPos();
		for(int x=x0;x<=x1;x++) {
			for(int z=z0;z<=z1;z++) {
				mutable.set(x,y0, z);
				ChunkPos cp=ChunkPos.containing(mutable);
				LevelChunk chunk=l.getChunk(cp.x(), cp.z());
				for(int y=y0;y<=y1;y++) {

					mutable.set(x,y, z);
					BlockState bs=chunk.getBlockState(mutable);
					if(bs.is(Tags.FLOWERS)) {
						hasFlower=true;
					}
					for(Biotope bt:Genes.Alleles.BIOTOPE) {
						if(bs.is(bt.getTag())) {
							bts.add(bt);
						}
					}
				}
			}
		}
		return (bts.isEmpty()&&!hasFlower)?null:bts;
	}
}
