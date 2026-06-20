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

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemAccessItemHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class ModifiableItemAccessHandler extends ItemAccessItemHandler {

	public ModifiableItemAccessHandler(ItemAccess itemAccess, DataComponentType<ItemContainerContents> component, int size) {
		super(itemAccess, component, size);
	}

	public void set(int index, ItemResource resource, int amount) {
		try(Transaction trans=Transaction.openRoot()) {
			int amt=itemAccess.getAmount();
			if(itemAccess.exchange(this.update(itemAccess.getResource(), index, resource, amount),amt , trans)==amt) {
				trans.commit();
			}
		}
	}
	public void set(int index, ItemResource resource, int amount,Transaction trans) {
		int amt=itemAccess.getAmount();
		if(itemAccess.exchange(this.update(itemAccess.getResource(), index, resource, amount),amt , trans)==amt) {
			trans.commit();
		}
		
	}
}