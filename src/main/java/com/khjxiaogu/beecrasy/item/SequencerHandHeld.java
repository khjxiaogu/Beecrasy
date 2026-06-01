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

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.menu.SequencerMenuHandHeld;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
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
		// TODO Auto-generated constructor stub
	}

    @Override
    public void onDestroyed(ItemEntity entity) {
        ItemContainerContents contents = entity.getItem().get(DataComponents.CONTAINER);
        if (contents != null) {
            entity.getItem().set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            ItemUtils.onContainerDestroyed(entity, contents.nonEmptyItemCopyStream());
        }
    }

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		
		if (!level.isClientSide()) {
			ItemStack stack=player.getItemInHand(hand);
			((ServerPlayer) player).openMenu(new MenuProvider() {

				@Override
				public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
					return new SequencerMenuHandHeld(containerId,inventory,ItemAccess.forPlayerInteraction(player, hand));
				}

				@Override
				public Component getDisplayName() {
					return stack.getDisplayName();
				}
				
			});
		}
		return super.use(level, player, hand);
	}
}
