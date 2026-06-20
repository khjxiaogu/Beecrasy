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

package com.khjxiaogu.beecrasy.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class MailClientHelper {
	@SuppressWarnings("resource")
	public static void simulatePickupMail(float x,float y,float z,ItemStack mailStack,int playerId) {
		ClientLevel l=Minecraft.getInstance().level;
		ItemEntity from=new ItemEntity(l, x, y, z, mailStack);
		l
        .playLocalSound(
            from.getX(),
            from.getY(),
            from.getZ(),
            SoundEvents.ITEM_PICKUP,
            SoundSource.PLAYERS,
            0.2F,
            (l.getRandom().nextFloat() - l.getRandom().nextFloat()) * 1.4F + 2.0F,
            false
        );
		LivingEntity to = (LivingEntity)l.getEntity(playerId);
		   EntityRenderState itemState = Minecraft.getInstance().getEntityRenderDispatcher().extractEntity(from, 1.0F);
		   Minecraft.getInstance().particleEngine.add(new ItemPickupParticle(l, itemState, to, from.getDeltaMovement()));
	}
}
