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

package com.khjxiaogu.beecrasy.mail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.menu.MailMenu;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class PostalOffice extends SavedData {
	Map<UUID,Mail> pending=new LinkedHashMap<>();
	Map<UUID,EntityReference<Entity>> entities=new LinkedHashMap<>();
	
	public static final Codec<PostalOffice> CODEC=RecordCodecBuilder.create(t->t.group(
		Mail.CODEC.listOf().fieldOf("letters").forGetter(o->new ArrayList<>(o.pending.values())),
		Codec.unboundedMap(UUIDUtil.CODEC, EntityReference.<Entity>codec()).fieldOf("deliveries").forGetter(o->o.entities)
		).apply(t, PostalOffice::new));
	public static final SavedDataType<PostalOffice> TYPE=new SavedDataType<>(Beecrasy.rl("post"),PostalOffice::new,PostalOffice.CODEC);
	
	public PostalOffice() {
		super();
	}

	public PostalOffice(List<Mail> mails,Map<UUID,EntityReference<Entity>> entities) {
		super();
		for(Mail m:mails) {
			pending.put(m.letterId(), m);
		}
		this.entities.putAll(entities);
		
	}
	public UUID createUUID() {
		while(true) {
			UUID toUse=UUID.randomUUID();
			if(!pending.containsKey(toUse))
				return toUse;
		}
	}
	public void post(Mail mail,ServerLevel sl) {
		pending.put(mail.letterId(), mail);
		@Nullable ServerPlayer reciver=sl.getServer().getPlayerList().getPlayer(mail.receiver());
		if(reciver!=null) {
			reciver.sendSystemMessage(Component.translatable("message.postal.mail_recived"));
		}
		this.setDirty();
	}
	public boolean isStillValid(UUID mail) {
		return pending.containsKey(mail);
	}
	public boolean hasDeliveryTask(ServerLevel level,UUID mailId) {
		EntityReference<Entity> delivery=entities.get(mailId);

		if(delivery==null||delivery.getEntity(level, Entity.class)==null) {
			return false;
		}
		return true;
	}
	public void addDeliveryTask(Entity entity,UUID mailId) {
		entities.put(mailId, EntityReference.of(entity));
		this.setDirty();
	}
	public boolean deliver(UUID mailId,ServerPlayer sp) {
		Mail mail=pending.get(mailId);
		if(mail==null||!mail.receiver().equals(sp.getUUID()))
			return false;
		ItemStack is=new ItemStack(Items.MAIL.getDelegate());
		is.set(Components.MAIL, mail.getMail(sp.level()));
		is.set(Components.CONTAINER,mail.items());
		boolean hasItem=false;
		for(int i=0;i<mail.items().getSlots();i++) {
			if(!mail.items().getStackInSlot(i).isEmpty()) {
				hasItem=true;
			}
		}
		is.set(DataComponents.ITEM_MODEL, hasItem?MailMenu.PACKAGE:MailMenu.LETTER);
		pending.remove(mailId);
		entities.remove(mailId);
		sp.getInventory().placeItemBackInInventory(is);
		this.setDirty();
		return true;
	}
	public int getMailCount(ServerPlayer sp){
		UUID puid=sp.getGameProfile().id();
		int count=0;
		for(Mail mail:pending.values()) {
			if(puid.equals(mail.receiver())) {
				count++;
			}
		}
		return count;
	}
	public List<Mail> collectMails(ServerPlayer sp){
		UUID puid=sp.getGameProfile().id();
		List<Mail> available=new ArrayList<>(pending.size());
		for(Mail mail:pending.values()) {
			if(puid.equals(mail.receiver())&&!hasDeliveryTask(sp.level(),mail.letterId())) {
				available.add(mail);
			}
		}
		return available;
	}
	@SuppressWarnings("resource")
	public static PostalOffice getPostalOffice(ServerLevel level) {
		return level.getServer().getDataStorage().computeIfAbsent(PostalOffice.TYPE);
	}
}
