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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.IntFunction;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Sounds;
import com.khjxiaogu.beecrasy.utils.BeecrasyMath;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.loading.FMLPaths;

public class BeediManager implements ResourceManagerReloadListener{
	public static final BeediManager INSTANCE=new BeediManager();
	private BeediManager() {}
	public Map<Identifier,MidiSheet> loadedFiles;

    private final Map<BlockPos, TrackPlayer> playingBeediSongs = new HashMap<>();
	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		loadedFiles=new HashMap<>();
		Path local=FMLPaths.GAMEDIR.get().resolve("beedi");
		local.toFile().mkdirs();
		try {
			Files.walk(local).filter(Files::isRegularFile).forEach(path-> {
				Identifier rl=Beecrasy.rl(path.getFileName().toString());
				String name=rl.getPath().substring(0,rl.getPath().lastIndexOf(".")).substring(6);
				Identifier id=rl.withPath(name);
				MidiSheet ms=null;
				if(rl.getPath().endsWith(".mid")) {
					try {
						try(InputStream is=new FileInputStream(path.toFile())){
							ms = new MidiSheet(is);
						}
					} catch (Exception e1) {
						e1.printStackTrace();
						Beecrasy.LOGGER.error("failed to load midi file "+rl+", skipped.");
					}	
				}else if(rl.getPath().endsWith(".bmid")) {
					try {
						try(InputStream is=new FileInputStream(path.toFile())){
							ms = MidiSheet.readFromBinaryFile(is);
						}
					} catch (Exception e1) {
						e1.printStackTrace();
						Beecrasy.LOGGER.error("failed to load midi binary file "+rl+", skipped.");
					}	
				}else if(rl.getPath().endsWith(".json")) {
					try {
						try(FileReader is=new FileReader(path.toFile(),StandardCharsets.UTF_8)){
							ms = MidiSheet.readFromJsonFile(is);
						}
					} catch (Exception e1) {
						e1.printStackTrace();
						Beecrasy.LOGGER.error("failed to load midi json file "+rl+", skipped.");
					}	
				}
				if(ms!=null) {
					ms.bake();
					loadedFiles.put(id, ms);
					Beecrasy.LOGGER.info("midi "+id+" loaded.");
				}
				
			});
		} catch (IOException e) {
			e.printStackTrace();
			Beecrasy.LOGGER.error("failed to load midi from local folder.");
		}
		resourceManager.listResources("beedi",_->true).keySet().forEach(rl->{
			//remove models/ and .json
			String name=rl.getPath().substring(0,rl.getPath().lastIndexOf(".")).substring(6);
			Identifier id=rl.withPath(name);
			MidiSheet ms=null;
			if(rl.getPath().endsWith(".mid")) {
				try {
					try(InputStream is=resourceManager.open(rl)){
						ms = new MidiSheet(is);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					Beecrasy.LOGGER.error("failed to load midi file "+rl+", skipped.");
				}	
			}else if(rl.getPath().endsWith(".bmid")) {
				try {
					try(InputStream is=resourceManager.open(rl)){
						ms = MidiSheet.readFromBinaryFile(is);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					Beecrasy.LOGGER.error("failed to load midi binary file "+rl+", skipped.");
				}	
			}else if(rl.getPath().endsWith(".json")) {
				try {
					try(BufferedReader is=resourceManager.openAsReader(rl)){
						ms = MidiSheet.readFromJsonFile(is);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					Beecrasy.LOGGER.error("failed to load midi json file "+rl+", skipped.");
				}	
			}
			if(ms!=null) {
				ms.bake();
				loadedFiles.put(id, ms);
				Beecrasy.LOGGER.info("midi "+id+" loaded.");
			}
		});
	}
	public void resetClientLevel() {
		playingBeediSongs.clear();
	}
	public void tick(ClientLevel level) {
		for(Entry<BlockPos, TrackPlayer> ent:playingBeediSongs.entrySet()) {
			NotePlayer player;
			BlockPos pos=ent.getKey();
			if(level.isLoaded(pos)) {
				player=(e,p,v,l)->{
					float pitch=BeecrasyMath.noteToPitch(p);
					int len=(int) (l*pitch);
					level.addParticle(ParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, p%24 / 24.0, 0.0, 0.0);
					level.playLocalSound(pos, e.apply(len), SoundSource.RECORDS, v/127f, pitch, false);};
			}else
				player=(_,_,_,_)->{};
			ent.getValue().tick(player);
		}
	}
    public void playSong(ClientLevel level,Identifier song,Optional<Identifier> id, BlockPos pos,int offset,float speed) {
        this.stopSong(pos);
        MidiSheet file=loadedFiles.get(song);
        if(file!=null) {
	        this.playingBeediSongs.put(pos, file.createPlayerBaked(id.map(BuiltInRegistries.SOUND_EVENT::getValue).map(t->(IntFunction<SoundEvent>)_->t).orElse(this::getSound),speed,offset));
	        Minecraft.getInstance().gui.setNowPlaying(Component.translatable(song.toLanguageKey("record", "title")));
	        notifyNearbyEntities(level, pos, true);
        }
    }
    private SoundEvent getSound(int len) {
    	if(len<250)
    		return Sounds.BEE_NOTE_FAST.get();
    	if(len<750)
    		return Sounds.BEE_NOTE.get();
    	if(len<1500)
    		return Sounds.BEE_NOTE_SLOW.get();
		return Sounds.BEE_NOTE_EXTRA_SLOW.get();
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
