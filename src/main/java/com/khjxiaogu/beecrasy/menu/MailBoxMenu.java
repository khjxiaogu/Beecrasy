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

package com.khjxiaogu.beecrasy.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Attachments;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Entities;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Menus;
import com.khjxiaogu.beecrasy.entity.BeeSwarmEntity;
import com.khjxiaogu.beecrasy.item.MailBoxItem;
import com.khjxiaogu.beecrasy.mail.Mail;
import com.khjxiaogu.beecrasy.mail.MailHelper;
import com.khjxiaogu.beecrasy.mail.PlayerPostalOffice;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class MailBoxMenu extends BeecrasyContainerMenu {
	protected ItemStacksResourceHandler data;

	public final static Identifier MAILBOX = Beecrasy.rl("handheld_mailbox");
	public final static Identifier MAILBOX_ACTIVE =Beecrasy.rl("handheld_mailbox_active");
	public static class MailSlot extends ResourceHandlerSlot{

		public MailSlot(ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> slotModifier,
				int handlerSlot, int xPosition, int yPosition) {
			super(handler, slotModifier, handlerSlot, xPosition, yPosition);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}

		@Override
		public boolean mayPickup(Player player) {
			return false;
		}
		
	}
	List<UUID> mailIds=new ArrayList<>();
	ItemAccess hand;
	/**
	 * @param bytebuf  
	 */
	public MailBoxMenu(int containerId,Inventory inventory,RegistryFriendlyByteBuf bytebuf) {
		this(Menus.MAILBOX_MENU.get(), containerId,inventory,new ItemStacksResourceHandler(27));
	}

	@SuppressWarnings("resource")
	public MailBoxMenu(int containerId,Inventory inventory,ServerPlayer player,ItemAccess handAccess) {
		this(Menus.MAILBOX_MENU.get(), containerId,inventory,new ItemStacksResourceHandler(27));
		List<Mail> mails=player.getData(Attachments.MAIL).collectMails(player);
		this.hand=handAccess;
		for(int i=0;i<27;i++) {
			if(i>=mails.size()) {
				break;
			}
			Mail mail=mails.get(i);
			mailIds.add(mail.letterId());
			data.set(i, ItemResource.of(Items.MAIL.getDelegate()).with(Components.MAIL, mail.getMail()).with(DataComponents.ITEM_MODEL, mail.items().nonEmptyItemCopyStream().findAny().isPresent()?MailMenu.PACKAGE:MailMenu.LETTER), 1);
		}
	}
	private MailBoxMenu(MenuType<?> menuType,int containerId, Inventory inventory, ItemStacksResourceHandler slots) {
		super(menuType, containerId, inventory, 27);
		this.data=slots;
		addSlots(slots,slots::set);
	}
	public void addSlots(ResourceHandler<ItemResource> slot, IndexModifier<ItemResource> slotModifier) {
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlot(new MailSlot(slot,slotModifier, j + i * 9 , 12 + j * 17, 11 + i * 17));
		this.addPlayerInventory(8, 84, 142);
	};
	public void tryDeliver(int slotIndex,Player player) {
		if(slotIndex<mailIds.size()&&player.level() instanceof ServerLevel level) {

			int height=level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, player.blockPosition());
			if(height<=player.getY()) {
				UUID uuid=mailIds.get(slotIndex);
				if(uuid!=null) {
					PlayerPostalOffice po=player.getData(Attachments.MAIL);
					if(!po.hasDeliveryTask(level, uuid)) {
						BlockPos pos=MailHelper.findManhattanTopRandomPos(level, player.blockPosition(), 5, 20);
						if(pos!=null) {
							BeeSwarmEntity entity=Entities.BEE_SWARM.get().create(level, EntitySpawnReason.NATURAL);
							if(entity!=null) {
								entity.setPos(pos.getCenter());
								entity.setTraceTarget(player);
								entity.setMailId(uuid);
								entity.setLifeSpanTicks(400);
								level.addFreshEntity(entity);
								po.addDeliveryTask(entity, uuid);
								mailIds.set(slotIndex, null);
								data.set(slotIndex, ItemResource.EMPTY, 0);
							}
							return;
						}
						player.sendSystemMessage(Component.translatable(MailBoxItem.NOT_VALID_PATH));
						return;
					}
				}
				player.sendSystemMessage(Component.translatable(MailBoxItem.IN_PROGRESS));
			}else {
				player.sendSystemMessage(Component.translatable(MailBoxItem.NOT_EXPOSE));
			}
			
		}
	}
	@Override
	public void clicked(int slotIndex, int buttonNum, ContainerInput containerInput, Player player) {
		tryDeliver(slotIndex,player);
		super.clicked(slotIndex, buttonNum, containerInput, player);

	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return super.stillValid(pPlayer)&&hand.getAmount()>0;
	}

	@Override
	public void receiveOperation(short opCode, int opData) {

	}

	@Override
	public boolean quickMoveIn(ItemStack slotStack) {
		return false;
	}

	@Override
	public void removed(Player player) {
		if(player instanceof ServerPlayer)
			try(Transaction trans=Transaction.openRoot()){
				if(hand.exchange(hand.getResource().with(DataComponents.ITEM_MODEL, player.getData(Attachments.MAIL).getMailCount()>0?MAILBOX_ACTIVE:MAILBOX), 1, trans)==1)
					trans.commit();
			}
		super.removed(player);
	}


}