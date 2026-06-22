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
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Attachments;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class PostalOffice extends SavedData {
	List<Mail> pending=new ArrayList<>();

	public static final Codec<PostalOffice> CODEC=RecordCodecBuilder.create(t->t.group(
		Mail.CODEC.listOf().fieldOf("letters").forGetter(o->o.pending)
		).apply(t, PostalOffice::new));
	public static final SavedDataType<PostalOffice> TYPE=new SavedDataType<>(Beecrasy.rl("post"),PostalOffice::new,PostalOffice.CODEC);
	
	public PostalOffice() {
		super();
	}

	public PostalOffice(List<Mail> mails) {
		super();
		pending.addAll(mails);
		
	}
	public UUID createUUID() {
		/*while(true) {
			UUID toUse=UUID.randomUUID();
			if(!pending.containsKey(toUse))
				return toUse;
		}*/
		return UUID.randomUUID();
	}
	public void post(Mail mail,ServerLevel sl) {
		ServerPlayer receiver=sl.getServer().getPlayerList().getPlayer(mail.receiver());
		if(receiver!=null) {
			receiver.sendSystemMessage(Component.translatable("message.postal.mail_recived"));
			receiver.getData(Attachments.MAIL).post(mail);;
		}else {
			pending.add(mail);
			this.setDirty();
		}
	}

	public void updatePendingMails(ServerPlayer sp) {
		if(!pending.isEmpty()) {
			PlayerPostalOffice ppo=sp.getData(Attachments.MAIL);
			for(Iterator<Mail> iterator=pending.iterator();iterator.hasNext();) {
				Mail m=iterator.next();
				if(m.receiver().equals(sp.getUUID())) {
					ppo.post(m);
					iterator.remove();
					this.setDirty();
				}
			}
		}
	}
	@SuppressWarnings("resource")
	public static PostalOffice getPostalOffice(ServerLevel level) {
		return level.getServer().getDataStorage().computeIfAbsent(PostalOffice.TYPE);
	}
}
