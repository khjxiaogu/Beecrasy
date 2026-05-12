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
	public static void handleMutation(DiploidGenome genome,RandomSource random) {
		for(MutationRecord mr:mutations) {
			if(mr.mutation.mutate(genome,random))
				break;
		}
	}
}
