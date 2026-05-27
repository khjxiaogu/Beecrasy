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

package com.khjxiaogu.beecrasy.utils;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.BitRandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;

public class SerializableRandomSource implements BitRandomSource {
    private long seed;
    public SerializableRandomSource(long seed) {
    	this.seed = seed;
    }
    public static SerializableRandomSource create(long seed) {
    	return new SerializableRandomSource((seed ^ 25214903917L) & 281474976710655L);
    }
    @Override
    public RandomSource fork() {
        return new SingleThreadedRandomSource(this.nextLong());
    }
    @Override
    public PositionalRandomFactory forkPositional() {
        return new LegacyRandomSource.LegacyPositionalRandomFactory(this.nextLong());
    }
    @Override
    public void setSeed(long seed) {
    	this.seed=seed;
    }
    public long getSeed() {
    	return seed;
    }
    @Override
    public int next(int bits) {
        long newSeed = this.seed * 25214903917L + 11L & 281474976710655L;
        this.seed = newSeed;
        return (int)(newSeed >> 48 - bits);
    }
	@Override
	public double nextGaussian() {
		double d=0;
		d+=nextDouble();
		d+=nextDouble();
		d+=nextDouble();
		return d/3d;
	}

}