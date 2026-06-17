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

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.mail.PostalOffice;
import com.khjxiaogu.beecrasy.menu.MailBoxMenu;

import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.neoforged.neoforge.transfer.access.ItemAccess;

public class MailBoxItem extends Item{
	public static final String NOT_EXPOSE="message.mailbox.not_open_area";
	public static final String IN_PROGRESS="message.mailbox.in_progress";
	public static final String NOT_VALID_PATH="message.mailbox.no_valid_path";

	public MailBoxItem(Properties properties) {
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
	public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner, EquipmentSlot slot) {
		if(owner instanceof ServerPlayer sp) {
			if(!(sp.containerMenu instanceof MailBoxMenu)) {
				if(sp.tickCount%20==0) {
					itemStack.set(DataComponents.ITEM_MODEL,PostalOffice.getPostalOffice(level).getMailCount(sp)>0?MailBoxMenu.MAILBOX_ACTIVE:MailBoxMenu.MAILBOX);
				}
			}
		}
		super.inventoryTick(itemStack, level, owner, slot);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		
		if (player instanceof ServerPlayer serverPlayer) {
			ItemStack stack=player.getItemInHand(hand);
			int heightY=level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, serverPlayer.blockPosition());
			if(heightY<=serverPlayer.getY()) {
				stack.set(DataComponents.ITEM_MODEL, MailBoxMenu.MAILBOX_ACTIVE);
				player.openMenu(new MenuProvider() {
	
					@Override
					public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
						return new MailBoxMenu(containerId,inventory,serverPlayer,ItemAccess.forPlayerSlot(serverPlayer, switch (hand) {
		                case MAIN_HAND -> player.getInventory().getSelectedSlot();
		                case OFF_HAND -> Inventory.SLOT_OFFHAND;
		            }));
					}
	
					@Override
					public Component getDisplayName() {
						return stack.getHoverName();
					}
					
				});
			}else {
				serverPlayer.sendSystemMessage(Component.translatable(NOT_EXPOSE));
			}
			return InteractionResult.SUCCESS;
		}
		return super.use(level, player, hand);
	}

}
