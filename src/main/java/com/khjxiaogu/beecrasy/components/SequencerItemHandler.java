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
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.utils.ItemValidateHelper;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class SequencerItemHandler extends ModifiableItemAccessHandler {
	public final static Identifier SEQUENCER=       Beecrasy.rl("handheld_sequencer");
	public final static Identifier SEQUENCER_ACTIVE=Beecrasy.rl("handheld_sequencer_active");
	public SequencerItemHandler(ItemAccess itemAccess) {
		super(itemAccess, Components.CONTAINER.get(), 2);
	}
	@Override
	protected ItemResource update(ItemResource accessResource, int index, ItemResource newResource, int newAmount) {
    	ItemResource ir=super.update(accessResource, index, newResource, newAmount);
		if(index==0) {
			if(newResource.isEmpty())
				ir=ir.with(DataComponents.ITEM_MODEL, SEQUENCER);
			else
				ir=ir.with(DataComponents.ITEM_MODEL, SEQUENCER_ACTIVE);
		}
		return ir;
	}
	@Override
	public boolean isValid(int index, ItemResource resource) {
		if(index==0) {
			return resource.has(Components.GENOME);
		}
		return ItemValidateHelper.isHoney(resource.toStack());
	}
}
