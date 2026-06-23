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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


// TODO: Auto-generated Javadoc
/**
 * Class NoteTrack.
 *
 * @author khjxiaogu
 * file: NoteTrack.java
 * time: 2020年8月9日
 */
public class NoteTrack{
	
	/** The notes.<br> 成员 notes. */
	public final List<NoteInfo> notes = new ArrayList<>();
	public long length;
	/**
	 * Gets the notes.<br>
	 * 获取 notes.
	 *
	 * @return notes<br>
	 */
	public List<NoteInfo> getNotes() {
		return notes;
	}

	/**
	 * Instantiates a new NoteTrack.<br>
	 * 新建一个NoteTrack类<br>
	 */
	public NoteTrack() {
	}

	/**
	 * Adds the.<br>
	 *
	 * @param key the key<br>
	 * @param tick the tick<br>
	 * @param vol the vol<br>
	 */
	public void add(int key, long tick, int vol) {
		NoteInfo ni = new NoteInfo(key, tick, vol);
		if (ni != null) {
			notes.add(ni);
		}
	}

	/**
	 * Adds the all.<br>
	 *
	 * @param ref the ref<br>
	 */
	public void addAll(NoteTrack ref) {
		notes.addAll(ref.notes);
	}

	/**
	 * Gets the size.<br>
	 * 获取 size.
	 *
	 * @return size<br>
	 */
	public int getSize() {
		return notes.size();
	}

	/**
	 * Sort.<br>
	 */
	public void bake() {
		notes.sort(Comparator.comparingLong(NoteInfo::ticks));
		length=notes.get(notes.size()-1).ticks()+20;
	}
}
