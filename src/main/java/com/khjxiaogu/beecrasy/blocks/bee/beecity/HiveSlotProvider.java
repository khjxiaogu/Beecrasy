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

import java.util.NoSuchElementException;

import com.khjxiaogu.beecrasy.beehive.HiveSlot;

import net.minecraft.core.BlockPos;

public interface HiveSlotProvider {
	public static enum HiveSlotType{
		QUEEN,
		COMB,
		ARGUMENT;
	}
	/**
	 * @param type  
	 */
	default int getSlots(HiveSlotType type) {
		return 0;
	}
	/**
	 * @param type  
	 * @param index 
	 */
	default HiveSlot getSlot(HiveSlotType type,int index) {
		throw new NoSuchElementException();
	};
	boolean isBindable(BlockPos core);
	boolean bind(BlockPos core);
	boolean unbind(BlockPos core);
}
