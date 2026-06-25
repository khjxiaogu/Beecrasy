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

package com.khjxiaogu.beecrasy.beehive;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Capability;
import com.khjxiaogu.beecrasy.blocks.bee.beecity.HiveSlotProvider;
import com.khjxiaogu.beecrasy.blocks.bee.beecity.HiveSlotProvider.HiveSlotType;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class BeeCityIterator implements Iterator<HiveSlot> {
	/** 底层迭代器 */
	private Iterator<? extends HiveSlot> iterator;
	/** 缓存的下一个符合条件的元素 */
	private HiveSlot nextItem;
	private final ServerLevel level;
	private final Iterator<BlockPos> pos;
	private final BlockPos corePos;
	private final HiveSlotType type;

	public BeeCityIterator(Iterator<? extends HiveSlot> iterator, ServerLevel level,BlockPos corePos, Iterator<BlockPos> pos, HiveSlotType type) {
		super();
		this.iterator = iterator;
		this.level = level;
		this.pos = pos;
		this.type = type;
		this.corePos = corePos;
		advance();
	}
	/**
	 * 预加载下一个符合条件的元素。
	 */
	private void advance() {
		while(iterator!=null) {
			while (iterator.hasNext()) {
				HiveSlot item = iterator.next();
				if (item.isValid()) {
					nextItem = item;
					return;
				}
			}
			advanceIterator();
		}
		nextItem = null;
	}
	private void advanceIterator() {
		while (pos.hasNext()) {
			BlockPos item = pos.next();
			if(!level.isLoaded(item))
				continue;
			HiveSlotProvider slots=level.getCapability(Capability.BEE_CITY_BLOCK, item);
			if (slots!=null&&slots.isBindable(corePos)) {
				slots.bind(corePos);
				iterator=IntStream.range(0, slots.getSlots(type)).mapToObj(t->slots.getSlot(type,t)).iterator();
				return;
			}
			pos.remove();
		}
		iterator = null;
	}
	@Override
	public boolean hasNext() {
		return nextItem != null;
	}
	@Override
	public HiveSlot next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		HiveSlot result = nextItem;
		advance();
		return result;
	}
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}