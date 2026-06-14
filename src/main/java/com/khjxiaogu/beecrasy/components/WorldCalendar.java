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

package com.khjxiaogu.beecrasy.components;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class WorldCalendar extends SavedData {
	public static final Codec<WorldCalendar> CODEC=RecordCodecBuilder.create(t->t.group(
			Codec.LONG.fieldOf("secs").forGetter(WorldCalendar::getSeconds),
			Codec.INT.fieldOf("partialSecs").forGetter(WorldCalendar::getPartialSecs),
			Codec.LONG.fieldOf("lastTicks").forGetter(WorldCalendar::getLastTicks)
			
			).apply(t, WorldCalendar::new));
	public static final SavedDataType<WorldCalendar> TYPE=new SavedDataType<>(Beecrasy.rl("calendar"),WorldCalendar::new,WorldCalendar.CODEC);
	long seconds;
	int partialSecs;
	long lastTicks;
	public WorldCalendar() {
		super();
	}
	public WorldCalendar(long seconds, int partialSecs, long lastTicks) {
		super();
		this.seconds = seconds;
		this.partialSecs = partialSecs;
		this.lastTicks = lastTicks;
	}
	public void tick(long curTicks) {
        long dt = curTicks - lastTicks;
        if (dt < 0) {//切换日
            long nextday = lastTicks + 24000L;
            nextday = nextday - nextday % 24000L;
            dt = curTicks % 24000L + nextday - lastTicks;
        }else if(dt==0){
        	dt=1;
        }
        partialSecs += dt;
        seconds+=partialSecs/20;
        partialSecs=partialSecs%20;
        lastTicks = curTicks;
        this.setDirty();
	}
	public long getSeconds() {
		return seconds;
	}
	public static Codec<WorldCalendar> getCodec() {
		return CODEC;
	}
	public int getPartialSecs() {
		return partialSecs;
	}
	public long getLastTicks() {
		return lastTicks;
	}
	@SuppressWarnings("resource")
	public static WorldCalendar getCalendar(ServerLevel level) {
		return level.getServer().getDataStorage().computeIfAbsent(WorldCalendar.TYPE);
	}
}
