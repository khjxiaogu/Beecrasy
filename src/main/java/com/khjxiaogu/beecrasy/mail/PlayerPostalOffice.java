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

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.menu.MailMenu;
import com.khjxiaogu.beecrasy.network.ClientMailReceivedMessage;
import com.mojang.serialization.Codec;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.network.PacketDistributor;

public class PlayerPostalOffice implements ValueIOSerializable{
	Map<UUID,Mail> pending=new LinkedHashMap<>();
	Map<UUID,EntityReference<Entity>> entities=new LinkedHashMap<>();
	public PlayerPostalOffice(PlayerPostalOffice t) {
		pending.putAll(t.pending);
		entities.putAll(t.entities);
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
	}
	@SuppressWarnings("resource")
	public boolean deliver(Vec3 source,UUID mailId,ServerPlayer sp) {
		Mail mail=pending.get(mailId);
		if(mail==null)
			return false;
		ItemStack is=new ItemStack(Items.MAIL.getDelegate());
		is.set(Components.MAIL, mail.getMail());
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
		PacketDistributor.sendToPlayersTrackingChunk(sp.level(), sp.chunkPosition(), new ClientMailReceivedMessage(sp.getId(),mail.icon(), hasItem, (float)source.x(), (float)source.y(), (float)source.z()));
		return true;
	}
	public void post(Mail mail) {
		pending.put(mail.letterId(), mail);
	}
	public int getMailCount(){
		return pending.size();
	}
	private static final Codec<List<Mail>> MAILS_CODEC=Mail.CODEC.listOf();
	private static final Codec<Map<UUID,EntityReference<Entity>>> DELIVERIES_CODEC=Codec.unboundedMap(UUIDUtil.CODEC, EntityReference.<Entity>codec());
	@Override
	public void serialize(ValueOutput output) {
		output.store("mails", MAILS_CODEC, new ArrayList<>(pending.values()));
		output.store("deliveries", DELIVERIES_CODEC, entities);
	}
	@Override
	public void deserialize(ValueInput input) {
		pending.clear();
		for(Mail m:input.read("mails", MAILS_CODEC).orElse(List.of())) {
			pending.put(m.letterId(), m);
		}
		entities.clear();
		entities.putAll(input.read("deliveries", DELIVERIES_CODEC).orElse(Map.of()));
	}
	@SuppressWarnings("resource")
	public List<Mail> collectMails(ServerPlayer sp){
		List<Mail> available=new ArrayList<>(pending.size());
		for(Mail mail:pending.values()) {
			if(!hasDeliveryTask(sp.level(),mail.letterId())) {
				available.add(mail);
			}
		}
		return available;
	}
	public PlayerPostalOffice() {
		super();
	}
}
