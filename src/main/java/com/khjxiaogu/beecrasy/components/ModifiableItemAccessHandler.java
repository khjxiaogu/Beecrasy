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

}