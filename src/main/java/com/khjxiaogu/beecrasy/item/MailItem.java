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

import java.util.Optional;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Tags;
import com.khjxiaogu.beecrasy.components.ModifiableItemAccessHandler;
import com.khjxiaogu.beecrasy.mail.LetterStatus;
import com.khjxiaogu.beecrasy.mail.Mail;
import com.khjxiaogu.beecrasy.mail.MailComponent;
import com.khjxiaogu.beecrasy.mail.PostalOffice;
import com.khjxiaogu.beecrasy.menu.MailMenu;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class MailItem extends Item{
	

	public MailItem(Properties properties) {
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
			if(stack.count()==1&&player instanceof ServerPlayer serverPlayer)
				MailMenu.openMenu(serverPlayer,ItemAccess.forPlayerSlot(serverPlayer, switch (hand) {
	                case MAIN_HAND -> player.getInventory().getSelectedSlot();
	                case OFF_HAND -> Inventory.SLOT_OFFHAND;
	            }));
			return InteractionResult.SUCCESS;
		}
		return super.use(level, player, hand);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		BlockState bs=context.getLevel().getBlockState(context.getClickedPos());
		if(bs.is(Tags.MAILBOX)) {
			ItemStack stack=context.getItemInHand();
			//ItemContainerContents contents = stack.get(Components.CONTAINER);
			MailComponent mailcomp=stack.get(Components.MAIL);
			if(mailcomp!=null) {
				if(context.getLevel() instanceof ServerLevel sl) {
					ItemAccess ia=ItemAccess.forStack(stack);
					ModifiableItemAccessHandler mah=new ModifiableItemAccessHandler(ia,Components.CONTAINER.get(),9);
					int postage=MailMenu.getPostage(mailcomp, mah);
					try(Transaction trans=Transaction.openRoot()){
						ItemResource rc=mah.getResource(8);
						if(mah.extract(8, rc, postage, trans)==postage) {
							PostalOffice po=PostalOffice.getPostalOffice(sl);
							ItemStack remain=rc.toStack(mah.getAmountAsInt(8));
							mah.set(8, ItemResource.EMPTY, 0,trans);
							ItemContainerContents contents = ia.getResource().get(Components.CONTAINER);
							Optional<Mail> mail=mailcomp.resolveMail(po.createUUID(), context.getPlayer().getUUID(), contents, sl);
							if(mail.isPresent()) {
								po.post(mail.get(),sl);
								stack.consume(1,context.getPlayer());
								context.getPlayer().getInventory().placeItemBackInInventory(remain);
								trans.commit();
								return InteractionResult.CONSUME;
							}
							context.getPlayer().sendSystemMessage(LetterStatus.PLAYER_NOT_EXIST.text);
							
						}else {
							context.getPlayer().sendSystemMessage(LetterStatus.NOT_ENOUGH_POSTAGE.text);
						}
					}
				}
				return InteractionResult.SUCCESS;
			}
		}
		return super.useOn(context);
	}

}
