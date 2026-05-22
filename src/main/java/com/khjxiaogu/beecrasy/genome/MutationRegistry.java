/** 
* Copyright (c) 2026 khjxiaogu
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
package com.khjxiaogu.beecrasy.genome;

import java.util.Comparator;
import java.util.PriorityQueue;

import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

public class MutationRegistry {
	private static record MutationRecord(Identifier id,Mutation mutation) {
		
	}
	private static PriorityQueue<MutationRecord> mutations=new PriorityQueue<>(Comparator.<MutationRecord>comparingInt(r->r.mutation.priority()).reversed());
	private MutationRegistry() {
	}
	public static synchronized void register(Identifier id,Mutation type) {
		mutations.removeIf(t->t.id.equals(id));
		if(type!=null)
			mutations.add(new MutationRecord(id,type));
	}
	public static void handleMutation(BeeHiveParameters params,DiploidGenome genome,RandomSource random) {
		for(MutationRecord mr:mutations) {
			if(mr.mutation.mutate(params,genome,random))
				break;
		}
	}
}
