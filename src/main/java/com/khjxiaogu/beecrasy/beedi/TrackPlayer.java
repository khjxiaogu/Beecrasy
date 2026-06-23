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

/**
 * Minecraft midi player
 * Copyright (C) 2021  khjxiaogu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.beecrasy.beedi;

import java.util.Iterator;
import java.util.function.Consumer;



public class TrackPlayer {
	private NoteTrack nc;
	private Iterator<NoteInfo> it;
	private NoteInfo cache;
	private long curticks = 0;
	private boolean canceled = false;
	private boolean finished = false;

	public synchronized boolean isFinished() {
		return finished;
	}

	public TrackPlayer(NoteTrack nc) {
		this.nc = nc;
	}

	public synchronized void reset() {
		it =null;
		curticks = 0;
		canceled = false;
		finished = false;
	}

	public void cancel() {
		canceled = true;
	}

	public void tick(Consumer<NoteInfo> player) {
		if(it==null)
			it=nc.getNotes().iterator();
		if (canceled)
			return;
		if (!it.hasNext()) {
			finished = true;
			return;
		}
		if(cache==null||curticks>=cache.ticks()) {
			if(cache!=null)
				player.accept(cache);
			cache=null;
			while (it.hasNext() && ((cache=it.next()).ticks()) <= curticks) {
				player.accept(cache);
			}
		}
		curticks++;
	}
}