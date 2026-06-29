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
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntFunction;
import java.util.zip.InflaterInputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import com.google.common.base.Objects;
import com.google.gson.JsonParser;
import com.khjxiaogu.beecrasy.beedi.NoteInfo.NoteOn;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;



public class MidiSheet {
	
	public List<List<NoteInfo>> tracks = new ArrayList<>();
	public static final StreamCodec<ByteBuf,MidiSheet> STREAM_CODEC=StreamCodec.composite(
		NoteInfo.STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs.list()),o->o.tracks,
		MidiSheet::new
		);
	public static final Codec<MidiSheet> CODEC=RecordCodecBuilder.create(
		t->t.group(Codec.list(Codec.list(NoteInfo.CODEC)).fieldOf("tracks").forGetter(o->o.tracks))
		.apply(t, MidiSheet::new));
	
	public MidiSheet(List<List<NoteInfo>> tracks) {
		super();
		this.tracks = tracks;
	}
	public static MidiSheet readFromJsonFile(Reader input) {
		return CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(input)).map(t->t.getFirst()).getOrThrow();
	}
	public static MidiSheet readFromBinaryFile(InputStream input) {
		ByteBuf byteBuf=Unpooled.buffer();
	    try (ByteBufOutputStream out = new ByteBufOutputStream(byteBuf);
	    	InflaterInputStream iis=new InflaterInputStream(input)) {
	    	iis.transferTo(out);
	    } catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return STREAM_CODEC.decode(byteBuf);
	}
	public MidiSheet(InputStream f) throws InvalidMidiDataException, IOException {
		Sequence sequence;
		sequence = MidiSystem.getSequence(f);
		float framesPerSecond;
		if (sequence.getDivisionType() == Sequence.PPQ) {
			framesPerSecond = 0F;
		} else {
			framesPerSecond = sequence.getDivisionType();
		}
		int resolution = sequence.getResolution();
		Int2ObjectOpenHashMap<NoteOn> keys=new Int2ObjectOpenHashMap<>(256*16);
		boolean[] channels=new boolean[16];
		for (Track track : sequence.getTracks()) {
			List<NoteInfo> currentTrack = new ArrayList<>();
			
			Arrays.fill(channels, true);
			keys.clear();
			channels[9]=false;
			double beatsPerMinute = 120;
			double millisPerMidiTick;
			if (framesPerSecond == 0F) {// PPQ mode
				millisPerMidiTick = 60000 / beatsPerMinute / resolution;
			} else {
				millisPerMidiTick = 1000 / resolution / framesPerSecond;
			}
			long currentOffset=0;
			long lastTick=0;
			if (track.size() > 0) {
				for (int i = 0; i < track.size(); i++) {
					MidiEvent event = track.get(i);
					MidiMessage message = event.getMessage();
					long delta=event.getTick()-lastTick;
					lastTick=event.getTick();
					currentOffset+=Math.round(delta * millisPerMidiTick);
					if (message instanceof ShortMessage) {
						ShortMessage sm = (ShortMessage) message;
						if (sm.getCommand()== ShortMessage.NOTE_ON&&sm.getData2()!=0) {// Detect KEY_ON message
							if(channels[sm.getChannel()]) {
								int pitch=sm.getData1();
								NoteOn cur=new NoteOn(pitch, currentOffset, sm.getData2());
								NoteOn old=keys.put(sm.getChannel()<<8+pitch,cur);
								if(old!=null&&!Objects.equal(old, cur))
									currentTrack.add(old.off(currentOffset));
							}
							continue;
						}
						if (sm.getCommand()== ShortMessage.NOTE_ON||sm.getCommand()==ShortMessage.NOTE_OFF) {
							
							int pitch=sm.getData1();
							NoteOn old=keys.remove(sm.getChannel()<<8+pitch);
							if(old!=null)
								currentTrack.add(old.off(currentOffset));
						}else if(sm.getCommand()==ShortMessage.CONTROL_CHANGE) {
	                        int controller = sm.getData1();
	                        int value = sm.getData2();
	                        if (controller == 0) {          // Bank Select MSB
	                            if(value==120||value==127) {
	                            	channels[sm.getChannel()]=false;
	                            }else {
	                            	channels[sm.getChannel()]=true;
	                            }
	                        }
		                    
						}
					} else if (message instanceof MetaMessage) {
						MetaMessage metaMessage = (MetaMessage) message;
						if (metaMessage.getStatus() == 0xff) {
							if (metaMessage.getType() == 0x51) {// Detect tempo change
								long microsPerBeat = 0;
								byte[] byteData = metaMessage.getData();
								for (int j = 0; j < byteData.length; j++) {
									microsPerBeat <<= 8;
									microsPerBeat |= Byte.toUnsignedInt(byteData[j]);
								}
								if (microsPerBeat != 0) {
									beatsPerMinute = 60000000 / microsPerBeat;
								}
								if (framesPerSecond == 0F) {// PPQ mode
									millisPerMidiTick = 60000 / beatsPerMinute / resolution;
								} else {
									millisPerMidiTick = 1000 / resolution / framesPerSecond ;
								}
							}
						}
					}
				}
				for(NoteOn no:keys.values()) {
					currentTrack.add(no.off(currentOffset));
				}
				
			}
			if (currentTrack.size() != 0) {
				tracks.add(currentTrack);
			}
		}
	}
	public boolean bake() {
		if(tracks.isEmpty())
			return false;
		if (tracks.size() > 1) {
			List<NoteInfo> combined = new ArrayList<>();
			for (List<NoteInfo> t : tracks) {
				combined.addAll(t);
			}
			tracks.clear();
			tracks.add(combined);
		}
		tracks.get(0).sort(Comparator.comparingLong(NoteInfo::begin));
		return true;
	}
	public TrackPlayer createPlayerBaked(IntFunction<SoundEvent> se,float speed,int offset,float noteLen) {
		return new TrackPlayer(tracks.get(0).iterator(),speed,offset,noteLen, se);
	}
}
