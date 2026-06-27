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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Menus;
import com.khjxiaogu.beecrasy.components.ModifiableItemAccessHandler;
import com.khjxiaogu.beecrasy.mail.LetterStatus;
import com.khjxiaogu.beecrasy.mail.MailComponent;
import com.khjxiaogu.beecrasy.utils.ItemValidateHelper;
import com.mojang.authlib.GameProfile;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.common.UsernameCache;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class MailMenu extends BeecrasyContainerMenu{
	ItemAccess iaccess;
	SimpleContainer iconContainer=new SimpleContainer(1);
	SimpleContainerData status=new SimpleContainerData(1);
	ResourceHandler<ItemResource> resources;
	public final static Identifier LETTER = Beecrasy.rl("mail");
	public final static Identifier PACKAGE =Beecrasy.rl("package");
	public String sender="",receiver="",line1="",line2="";
	public boolean readOnly=false;
	private Map<String,UUID> stru=new HashMap<>();
	/**
	 * @param buf  
	 */
	public MailMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
		this(containerId, inventory);
		ItemStacksResourceHandler slots=new ItemStacksResourceHandler(9) {
			@Override
			public boolean isValid(int index, ItemResource resource) {
				if(index==8)
					return ItemValidateHelper.isHoney(resource);
				return super.isValid(index, resource);
			}
		};
		this.addSlots(slots,slots::set);
		if(buf.readBoolean()) {
			sender=buf.readUtf();
			receiver=buf.readUtf();
			line1=buf.readUtf();
			line2=buf.readUtf();
			readOnly=buf.readBoolean();
		}
	}
	public void checkStatus() {
		if(this.inventory.player instanceof ServerPlayer) {
			MailComponent mc=iaccess.getResource().get(Components.MAIL);
			if(mc==null)
				mc=MailComponent.EMPTY;
			UUID id=stru.get(mc.receiver());
			if(id==null) {
				this.setStatus(LetterStatus.PLAYER_NOT_EXIST);
				return;
			}
			int postage=getPostage(mc,resources);
			
			if(postage==0) {
				this.setStatus(LetterStatus.EMPTY_LETTER);
				return;
			}
			if(postage>resources.getAmountAsInt(8)) {
				this.setStatus(LetterStatus.NOT_ENOUGH_POSTAGE);
				return;
			}

			this.setStatus(LetterStatus.OK);
		}
	}
	public static int getPostage(MailComponent mc,ResourceHandler<ItemResource> resources) {
		int postage=0;
		for(int i=0;i<8;i++) {
			ItemResource ir=resources.getResource(i);
			int amount=resources.getAmountAsInt(i);
			int capacity=resources.getCapacityAsInt(i, ir);
			if(amount>0) {
				if(capacity<=4) {
					postage+=amount;
				}else {
					postage+=Math.ceilDiv(amount*4, capacity);
				}
			}
		}
		if(!(mc.line1().isEmpty()&&mc.line2().isEmpty())){
			postage=Math.max(postage, 1);
		}
		return postage;
	}
	public static void openMenu(ServerPlayer player,ItemAccess access) {
		player.openMenu(new MenuProvider() {
			@Override
			public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
				return new MailMenu(containerId,inventory,access);
			}
			@Override
			public Component getDisplayName() {
				return access.getResource().getHoverName();
			}
			
		}, t->{
			MailComponent mc=access.getResource().get(Components.MAIL);
			if(mc!=null) {
				t.writeBoolean(true);
				t.writeUtf(mc.sender());
				t.writeUtf(mc.receiver());
				t.writeUtf(mc.line1());
				t.writeUtf(mc.line2());
				t.writeBoolean(mc.readOnly());
			}else {
				t.writeBoolean(false);
			}
			
		});
		
	}
	public MailMenu(int containerId, Inventory inventory, ItemAccess access) {
		this(containerId, inventory);
		iaccess=access;
		ModifiableItemAccessHandler slots=new ModifiableItemAccessHandler(access,Components.CONTAINER.get(),9) {
			
			@Override
			protected ItemResource update(ItemResource accessResource, int index, ItemResource newResource, int newAmount) {
		    	ItemResource ir=super.update(accessResource, index, newResource, newAmount);
		    	MailComponent mc=ir.get(Components.MAIL);
				if(index<8&&(mc==null||!mc.readOnly())) {
					if(newResource.isEmpty()) {
						for(int i=0;i<8;i++) {
							if(!super.getResourceFrom(ir, i).isEmpty()) {
								return ir.with(DataComponents.ITEM_MODEL, PACKAGE);
							}
						}
						ir=ir.with(DataComponents.ITEM_MODEL, LETTER);
					}else
						ir=ir.with(DataComponents.ITEM_MODEL, PACKAGE);
				}
				return ir;
			}

			@Override
			public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
				
				MailComponent mc=iaccess.getResource().get(Components.MAIL);
				if(mc!=null&&mc.readOnly())
					return 0;
				return super.insert(index, resource, amount, transaction);
			}

			@Override
			public boolean isValid(int index, ItemResource resource) {
				if(index==8)
					return ItemValidateHelper.isHoney(resource);
				return super.isValid(index, resource);
			}
		};
		MailComponent mc=access.getResource().get(Components.MAIL);
		if(mc!=null&&mc.icon().isPresent()) {
			iconContainer.setItem(0, mc.icon().get().create());
		}
		this.addSlots(slots,slots::set);
		Map<UUID, String> map=UsernameCache.getMap();
		for(Entry<UUID, String> maps:map.entrySet()) {
			stru.putIfAbsent(maps.getValue(), maps.getKey());
			
		}
		if(inventory.player.level() instanceof ServerLevel sl) {
			if(sl.getServer().isSingleplayer()) {
				GameProfile profile=sl.getServer().getSingleplayerProfile();
				stru.put(profile.name(), profile.id());
				
			}
		}
		sender=mc.sender();
		receiver=mc.receiver();
		line1=mc.line1();
		line2=mc.line2();
		readOnly=mc.readOnly();
		checkStatus();
	}
	public MailMenu(int containerId, Inventory inventory) {
		super(Menus.MAIL_MENU.get(), containerId, inventory,9);
		
	}
	public void addSlots(ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> slotModifier) {
		resources=handler;
		if(readOnly)
			for(int i=0;i<8;i++)
				this.addSlot(new OutputSlot(handler,slotModifier,i,25+i*17,52));
		else
			for(int i=0;i<8;i++)
				this.addSlot(new ResourceHandlerSlot(handler,slotModifier,i,25+i*17,52));

		this.addSlot(new ResourceHandlerSlot(handler,slotModifier,8,143,7));

		this.addSlot(new Slot(iconContainer,0,143,28) {
			@Override
			public boolean mayPlace(ItemStack itemStack) {
				return false;
			}
			@Override
			public boolean mayPickup(Player player) {
				return false;
			}
			
		});
		this.addDataSlots(status);
		this.addPlayerInventory(8, 84, 142);
	}
	@Override
	public boolean quickMoveIn(ItemStack slotStack) {
		return this.moveItemStackTo(slotStack, 8, 9, false)||this.moveItemStackTo(slotStack, 0, 8, false);
	}
	@Override
	public boolean stillValid(Player pPlayer) {
		return iaccess==null||iaccess.getAmount()==1;
	}
	@Override
	public void removed(Player player) {
		super.removed(player);
	}
	@Override
	public void receiveOperation(short opCode, int opData) {
		
	}
	public void setIcon(ItemStack stack) {
		iconContainer.setItem(0, stack);
		if(this.iaccess!=null)
			try (Transaction trans=Transaction.openRoot()){
				ItemResource in=this.iaccess.getResource();
				MailComponent mc=in.get(Components.MAIL);
				if(mc==null)
					mc=MailComponent.EMPTY;
				if(stack.isEmpty()) {
					if(this.iaccess.exchange(in.with(Components.MAIL, mc.removeIcon()), 1, trans)==1) {
						trans.commit();
					}
				}else if(this.iaccess.exchange(in.with(Components.MAIL, mc.withIcon(ItemStackTemplate.fromNonEmptyStack(stack))), 1, trans)==1) {
					trans.commit();
				}
			}
	}
	@Override
	public void clicked(int slotIndex, int buttonNum, ContainerInput containerInput, Player player) {
		if(slotIndex==9) {
			if(buttonNum==0) {
				ItemStack carStack=this.getCarried().copyWithCount(1);
				setIcon(carStack);
			}else {
				setIcon(ItemStack.EMPTY);
			}
			return;
		}
		super.clicked(slotIndex, buttonNum, containerInput, player);
		if(this.iaccess!=null)
			checkStatus();
	}
	public LetterStatus getStatus() {
		return LetterStatus.values()[status.get(0)];
	}
	public void setStatus(LetterStatus status) {
		this.status.set(0, status.ordinal());
	}
	public void setLetterText(byte field, String text) {
		try (Transaction trans=Transaction.openRoot()){
			ItemResource in=this.iaccess.getResource();
			MailComponent mc=in.get(Components.MAIL);
			if(mc==null)
				mc=MailComponent.EMPTY;
			mc=switch(field) {
			case 0->mc.withReceiver(text);
			case 1->mc.withLine1(text);
			case 2->mc.withLine2(text);
			default->mc;
			};
			if(this.iaccess.exchange(in.with(Components.MAIL, mc), 1, trans)==1) {
				trans.commit();
			}
		}
		checkStatus();
	}
	
	
}
