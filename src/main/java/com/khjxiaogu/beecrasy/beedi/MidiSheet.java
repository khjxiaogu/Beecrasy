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

/**
 * Minecraft midi player
 * Copyright (C) 2021  khjxiaogu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.beecrasy.beedi;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;


// TODO: Auto-generated Javadoc
/**
 * Class MidiSheet.
 *
 * @author khjxiaogu
 * file: MidiSheet.java
 * time: 2020年8月9日
 */
public class MidiSheet {
	
	/** The tracks.<br> 成员 tracks. */
	public List<NoteTrack> tracks = new ArrayList<>();
	private final static int MsPerGameTick = 50;

	/**
	 * Instantiates a new MidiSheet.<br>
	 * 新建一个MidiSheet类<br>
	 *
	 * @param f the f<br>
	 * @param offset the offset<br>
	 * @param speed the speed<br>
	 * @throws InvalidMidiDataException if an invalid midi data exception occurred.<br>如果invalid midi data exception发生了
	 * @throws IOException Signals that an I/O exception has occurred.<br>发生IO错误
	 */
	public MidiSheet(InputStream f, int offset, float speed) throws InvalidMidiDataException, IOException {
		Sequence sequence;
		sequence = MidiSystem.getSequence(f);
		float framesPerSecond;
		if (sequence.getDivisionType() == Sequence.PPQ) {
			framesPerSecond = 0F;
		} else {
			framesPerSecond = sequence.getDivisionType();
		}
		int resolution = sequence.getResolution();
		for (Track track : sequence.getTracks()) {
			NoteTrack currentTrack = new NoteTrack();
			boolean[] channels=new boolean[16];
			Arrays.fill(channels, true);
			channels[9]=false;
			double beatsPerMinute = 120;
			double millisPerMidiTick;
			if (framesPerSecond == 0F) {// PPQ mode
				millisPerMidiTick = 60000 / beatsPerMinute / resolution / speed;
			} else {
				millisPerMidiTick = 1000 / resolution / framesPerSecond / speed;
			}
			long lastOffset=0;
			long lastTick=0;
			if (track.size() > 0) {
				for (int i = 0; i < track.size(); i++) {
					MidiEvent event = track.get(i);
					MidiMessage message = event.getMessage();
					if (message instanceof ShortMessage) {
						ShortMessage sm = (ShortMessage) message;
						if (sm.getCommand()== ShortMessage.NOTE_ON) {// Detect KEY_ON message
							if(channels[sm.getChannel()]) {
								long delta=event.getTick()-lastTick;
								lastTick=event.getTick();
								lastOffset+=Math.round(delta * millisPerMidiTick / MsPerGameTick);
								currentTrack.add(sm.getData1() + offset * 12,lastOffset, sm.getData2());
							}
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
									microsPerBeat *= 0x100;
									microsPerBeat += Byte.toUnsignedInt(byteData[j]);
								}
								if (microsPerBeat != 0) {
									beatsPerMinute = 60000000 / microsPerBeat;
								}
								if (framesPerSecond == 0F) {// PPQ mode
									millisPerMidiTick = 60000 / beatsPerMinute / resolution / speed;
								} else {
									millisPerMidiTick = 1000 / resolution / framesPerSecond / speed;
								}
							}
						}
					}
				}
			}
			if (currentTrack.getSize() != 0) {
				tracks.add(currentTrack);
			}
		}
	}
	public boolean bake() {
		if(tracks.isEmpty())
			return false;
		if (tracks.size() == 1) {
			tracks.get(0).bake();
			return true;
		}
		NoteTrack Combined = new NoteTrack();
		;
		for (NoteTrack t : tracks) {
			Combined.addAll(t);
		}
		Combined.bake();
		tracks.clear();
		tracks.add(Combined);
		return true;
	}
	public TrackPlayer createPlayerBaked() {
		return new TrackPlayer(tracks.get(0));
	}
}
