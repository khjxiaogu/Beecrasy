package com.khjxiaogu.beecrasy.beedi;

import java.util.function.IntFunction;

import net.minecraft.sounds.SoundEvent;

public interface NotePlayer {
	void play(IntFunction<SoundEvent> se,int pitch,byte volume,long length);
}
