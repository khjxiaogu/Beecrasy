package com.khjxiaogu.beecrasy.beehive;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Capability;
import com.khjxiaogu.beecrasy.blocks.HiveSlotProvider;
import com.khjxiaogu.beecrasy.blocks.HiveSlotProvider.HiveSlotType;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class BeeCityIterator implements Iterator<HiveSlot> {
	/** 底层迭代器 */
	private Iterator<? extends HiveSlot> iterator;
	/** 缓存的下一个符合条件的元素 */
	private HiveSlot nextItem;
	private final ServerLevel level;
	private final Iterator<BlockPos> pos;
	private final HiveSlotType type;

	public BeeCityIterator(Iterator<? extends HiveSlot> iterator, ServerLevel level, Iterator<BlockPos> pos, HiveSlotType type) {
		super();
		this.iterator = iterator;
		this.level = level;
		this.pos = pos;
		this.type = type;
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
			if (slots!=null) {
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