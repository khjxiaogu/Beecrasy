package com.khjxiaogu.beecrasy.mail;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.common.UsernameCache;

public record MailComponent(String sender,String receiver,Optional<ItemStackTemplate> icon,String line1,String line2,boolean readOnly) {
	public static final Codec<MailComponent> CODEC=RecordCodecBuilder.create(t->t.group(
		Codec.STRING.fieldOf("sender").forGetter(MailComponent::sender),
		Codec.STRING.fieldOf("receiver").forGetter(MailComponent::receiver),
		ItemStackTemplate.CODEC.optionalFieldOf("icon").forGetter(MailComponent::icon),
		Codec.STRING.fieldOf("line1").forGetter(MailComponent::line1),
		Codec.STRING.fieldOf("line2").forGetter(MailComponent::line2),
		Codec.BOOL.fieldOf("readOnly").forGetter(MailComponent::readOnly)
		).apply(t, MailComponent::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, MailComponent> STREAM_CODEC=StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8,MailComponent::sender,
		ByteBufCodecs.STRING_UTF8,MailComponent::receiver,
		ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC),MailComponent::icon,
		ByteBufCodecs.STRING_UTF8,MailComponent::line1,
		ByteBufCodecs.STRING_UTF8,MailComponent::line2,
		ByteBufCodecs.BOOL,MailComponent::readOnly,
		MailComponent::new);
	
	public static final MailComponent EMPTY=new MailComponent();
	private MailComponent(){
		this("","",Optional.empty(),"","",false);
	}
	public MailComponent withReceiver(String receiver) {
		return new MailComponent(sender,receiver,icon,line1,line2,readOnly);
	}
	public MailComponent withIcon(ItemStackTemplate icon) {
		return new MailComponent(sender,receiver,Optional.of(icon),line1,line2,readOnly);
	}
	public MailComponent removeIcon() {
		return new MailComponent(sender,receiver,Optional.empty(),line1,line2,readOnly);
	}
	public MailComponent withIcon(Optional<ItemStackTemplate> icon) {
		return new MailComponent(sender,receiver,icon,line1,line2,readOnly);
	}
	public MailComponent withLine1(String line1) {
		return new MailComponent(sender,receiver,icon,line1,line2,readOnly);
	}
	public MailComponent withLine2(String line2) {
		return new MailComponent(sender,receiver,icon,line1,line2,readOnly);
	}
	public MailComponent withLines(String receiver,String line1,String line2) {
		return new MailComponent(sender,receiver,icon,line1,line2,readOnly);
	}
	public MailComponent withItems(ItemContainerContents items) {
		return new MailComponent(sender,receiver,icon,line1,line2,readOnly);
	}
	public Optional<Mail> resolveMail(UUID letterId,UUID sender,ItemContainerContents items,ServerLevel server) {
		UUID uuid=null;
		if(server.getServer().isSingleplayer()) {
			GameProfile profile=server.getServer().getSingleplayerProfile();
			if(receiver.equals(profile.name())) {
				uuid=profile.id();
			}
		}
		if(uuid==null)
			for(Entry<UUID, String> ent:UsernameCache.getMap().entrySet()) {
				if(receiver.equals(ent.getValue())) {
					uuid=ent.getKey();
					break;
				}
			}
		if(uuid!=null) {
			return Optional.of(new Mail(letterId,sender,uuid,icon,line1,line2,items));
		}
		return Optional.empty();
	}
}
