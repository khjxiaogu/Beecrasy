package com.khjxiaogu.beecrasy.network;

import java.util.Optional;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.client.MailClientHelper;
import com.khjxiaogu.beecrasy.mail.MailComponent;
import com.khjxiaogu.beecrasy.menu.MailMenu;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientMailReceivedMessage(int playerId,Optional<ItemStackTemplate> icon,boolean pack,float x,float y,float z) implements CustomPacketPayload {

	public static final Type<ClientMailReceivedMessage> TYPE=new Type<>(Beecrasy.rl("mail_received_client"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientMailReceivedMessage> CODEC=StreamCodec.composite(
		ByteBufCodecs.VAR_INT,ClientMailReceivedMessage::playerId,
		ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), ClientMailReceivedMessage::icon,
		ByteBufCodecs.BOOL, ClientMailReceivedMessage::pack,
		ByteBufCodecs.FLOAT, ClientMailReceivedMessage::x,
		ByteBufCodecs.FLOAT, ClientMailReceivedMessage::y,
		ByteBufCodecs.FLOAT, ClientMailReceivedMessage::z,
		ClientMailReceivedMessage::new);
	void handle(IPayloadContext context) {
		context.enqueueWork(()->{
			ItemStack is=new ItemStack(Items.MAIL.get());
			is.set(DataComponents.ITEM_MODEL, pack?MailMenu.PACKAGE:MailMenu.LETTER);
			is.set(Components.MAIL, MailComponent.EMPTY.withIcon(icon));
			MailClientHelper.simulatePickupMail(x, y, z, is, playerId);
		});
	}
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
