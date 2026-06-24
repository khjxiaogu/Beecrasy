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

package com.khjxiaogu.beecrasy.beedi;

import java.util.Iterator;
import java.util.function.IntFunction;

import net.minecraft.sounds.SoundEvent;



public class TrackPlayer {
	private Iterator<NoteInfo> it;
	private NoteInfo cache;
	private long curticks = 0;
	private float speed;
	private int offset;
	private IntFunction<SoundEvent> se;
	public boolean isFinished() {
		return !it.hasNext();
	}

	public TrackPlayer(Iterator<NoteInfo> it,float speed,int offset,IntFunction<SoundEvent> se) {
		this.it = it;
		this.speed=speed;
		this.offset=offset;
		this.se=se;
	}
	public long millsToTick(NoteInfo millis) {
		return Math.round(millis.begin()/50f/speed);
	}
	public void tick(NotePlayer player) {
		if (!it.hasNext())
			return;
		if(cache==null||curticks>=millsToTick(cache)) {
			if(cache!=null)
				player.play(se,cache.pitch()+offset, cache.volume(), Math.round(cache.length()/speed));
			cache=null;
			while (it.hasNext() && curticks>=millsToTick(cache=it.next())) {
				player.play(se,cache.pitch()+offset, cache.volume(), Math.round(cache.length()/speed));
			}
		}
		curticks++;
	}
}