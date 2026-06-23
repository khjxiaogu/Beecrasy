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

package com.khjxiaogu.beecrasy.beedi;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.sound.midi.InvalidMidiDataException;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Sounds;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class BeediManager implements ResourceManagerReloadListener{
	public static final BeediManager INSTANCE=new BeediManager();
	private BeediManager() {}
	public Map<Identifier,MidiSheet> loadedFiles;

    private final Map<BlockPos, TrackPlayer> playingBeediSongs = new HashMap<>();
	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		loadedFiles=new HashMap<>();
		resourceManager.listResources("beedi",e->e.getPath().endsWith(".mid")).keySet().forEach(rl->{
			//remove models/ and .json
			String name=rl.getPath().substring(0,rl.getPath().lastIndexOf(".")).substring(6);
			Identifier id=rl.withPath(name);
			try {
				try(InputStream is=resourceManager.open(rl)){
					MidiSheet ms = new MidiSheet(is,0,1);
					ms.bake();
					loadedFiles.put(id, ms);
					Beecrasy.LOGGER.info("midi "+id+" loaded.");
				}
			} catch (InvalidMidiDataException | IOException e1) {
				e1.printStackTrace();
				Beecrasy.LOGGER.error("failed to load midi file "+rl+", skipped.");
			}	
		});
	}
	public void resetClientLevel() {
		playingBeediSongs.clear();
	}
	public void tick(ClientLevel level) {
		for(Entry<BlockPos, TrackPlayer> ent:playingBeediSongs.entrySet()) {
			Consumer<NoteInfo> player;
			if(level.isLoaded(ent.getKey())) {
				player=t->{
					level.playLocalSound(ent.getKey(), Sounds.BEE_NOTE.get(), SoundSource.RECORDS, t.volume(), t.pitch(), false);};
			}else
				player=_->{};
			ent.getValue().tick(player);
		}
	}
    public void playSong(ClientLevel level,Identifier song, BlockPos pos) {
        this.stopSong(pos);
        MidiSheet file=loadedFiles.get(song);
        if(file!=null) {
	        this.playingBeediSongs.put(pos, file.createPlayerBaked());
	        notifyNearbyEntities(level, pos, true);
        }
    }
    public void stopSongAndNotifyNearby(ClientLevel level,BlockPos pos) {
        this.stopSong(pos);
        notifyNearbyEntities(level, pos, false);
    }
    private static void notifyNearbyEntities(Level level, BlockPos pos, boolean isPlaying) {
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(3.0))) {
            entity.setRecordPlayingNearby(pos, isPlaying);
        }
    }
    public void stopSong(BlockPos pos) {
        this.playingBeediSongs.remove(pos);
    }
}
