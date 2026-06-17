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

package com.khjxiaogu.beecrasy.item;

import java.util.List;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.menu.SequencerMenuHandHeld;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.access.ItemAccess;

public class SequencerHandHeld extends Item{
	

	public SequencerHandHeld(Properties properties) {
		super(properties);
	}

    @Override
    public void onDestroyed(ItemEntity entity) {
        ItemContainerContents contents = entity.getItem().get(Components.CONTAINER);
        if (contents != null) {
            entity.getItem().set(Components.CONTAINER, ItemContainerContents.EMPTY);
            ItemUtils.onContainerDestroyed(entity, contents.nonEmptyItemCopyStream());
        }
    }

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		
		if (!level.isClientSide()) {
			ItemStack stack=player.getItemInHand(hand);
			((ServerPlayer) player).openMenu(new MenuProvider() {

				@Override
				public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
					return new SequencerMenuHandHeld(containerId,inventory,ItemAccess.forPlayerSlot(player, switch (hand) {
	                case MAIN_HAND -> player.getInventory().getSelectedSlot();
	                case OFF_HAND -> Inventory.SLOT_OFFHAND;
	            }));
				}

				@Override
				public Component getDisplayName() {
					return stack.getDisplayName();
				}
				
			});
			return InteractionResult.SUCCESS;
		}
		return super.use(level, player, hand);
	}

	@Override
	public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner, EquipmentSlot slot) {
		
		super.inventoryTick(itemStack, level, owner, slot);

        ItemContainerContents contents = itemStack.get(Components.CONTAINER);
        if(contents.getSlots()<2)
        	return;
		ItemStack honey=contents.getStackInSlot(1);
    	ItemStack bee=contents.getStackInSlot(0);
    	if(!honey.isEmpty()&&!bee.isEmpty()) {
    		GenomeComponent genome=bee.get(Components.GENOME);
    		if(genome!=null&&!genome.isInspected()) {
    			honey.split(1);
    			bee.set(Components.GENOME, genome.asInspected());
    	    	itemStack.set(Components.CONTAINER, ItemContainerContents.fromItems(List.of(bee,honey)));
    		}
    	}
	}
}
