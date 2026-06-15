package com.khjxiaogu.beecrasy.mail;

import net.minecraft.network.chat.Component;

public enum LetterStatus {
	NOT_ENOUGH_POSTAGE,
	PLAYER_NOT_EXIST,
	EMPTY_LETTER,
	OK;
	public final String transKey="gui.correspondence."+this.name().toLowerCase();
	public final Component text=Component.translatable(transKey);
}
