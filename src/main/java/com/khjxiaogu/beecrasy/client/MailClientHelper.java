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
