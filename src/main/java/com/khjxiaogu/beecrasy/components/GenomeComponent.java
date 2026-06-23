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

package com.khjxiaogu.beecrasy.components;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.khjxiaogu.beecrasy.genome.Genome;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public class GenomeComponent implements Iterable<Genome>{

	public static final GenomeComponent HAPLOID_EMPTY=new GenomeComponent(true);
	public static final GenomeComponent DIPLOID_EMPTY=new GenomeComponent(false);
	public static final Codec<GenomeComponent> CODEC=RecordCodecBuilder.create(t->t.group(
		Codec.BOOL.fieldOf("inspected").forGetter(o->o.inspected),
		ExtraCodecs.nonEmptyList(Codec.list(Genome.CODEC)).optionalFieldOf("genomes",List.of(Genome.DEFAULT)).forGetter(o->List.of(o.genomes))
		).apply(t, GenomeComponent::new)
		);
	public static final StreamCodec<RegistryFriendlyByteBuf,GenomeComponent> EMPTY_CODEC= new StreamCodec<>() {
        @Override
        public GenomeComponent decode(RegistryFriendlyByteBuf input) {
            return HAPLOID_EMPTY;
        }

        @Override
        public void encode(RegistryFriendlyByteBuf output, GenomeComponent value) {
            
        }
    };
	public static final StreamCodec<RegistryFriendlyByteBuf,GenomeComponent> FULL_CODEC=Genome.STREAM_CODEC.apply(ByteBufCodecs.list())
		.map(t->new GenomeComponent(true,t), t->List.of(t.genomes));
	public static final StreamCodec<RegistryFriendlyByteBuf,GenomeComponent> NETWORK_CODEC=ByteBufCodecs.BOOL
		.<RegistryFriendlyByteBuf>cast()
		.dispatch(t->t.inspected,
		n->(n?FULL_CODEC:EMPTY_CODEC));
	private final boolean inspected;
	private final Genome[] genomes;
	private GenomeComponent(boolean haploid) {
		inspected=false;
		if(haploid)
			this.genomes = new Genome[] {Genome.DEFAULT};
		else
			this.genomes = new Genome[] {Genome.DEFAULT,Genome.DEFAULT};
	}
	
	public GenomeComponent(boolean inspected,List<Genome> genomes) {
		super();
		this.inspected=inspected;
		this.genomes = genomes.toArray(Genome[]::new);
	}
	public GenomeComponent(boolean inspected,Genome... genome) {
		super();
		this.inspected=inspected;
		this.genomes =genome;
	}
	public GenomeComponent(boolean inspected, Genome genome1, Optional<Genome> genome2) {
		super();
		this.inspected = inspected;
		if(genome2.isPresent())
			this.genomes = new Genome[] {genome1,genome2.get()};
		else
			this.genomes = new Genome[] {genome1};
	}
	public GenomeComponent asInspected() {
		if(inspected)
			return this;
		return new GenomeComponent(true,genomes);
	}
	public GenomeComponent reduceHaploid() {
		if(genomes.length==0)
			return HAPLOID_EMPTY;
		if(genomes.length==1)
			return this;
		return new GenomeComponent(this.isInspected(),genomes[0]);
	}
	public GenomeComponent reduceDiploid() {
		if(genomes.length==0)
			return DIPLOID_EMPTY;
		if(genomes.length==1)
			return new GenomeComponent(this.isInspected(),genomes[0],genomes[0]);
		if(genomes.length==2)
			return this;
		return new GenomeComponent(this.isInspected(),genomes[0],genomes[1]);
	}
	public int size() {
		return genomes.length;
	}
	public Genome getGenome(int index) {
		return genomes[index];
	}
	public Genome[] toArray() {
		return Arrays.copyOf(genomes, genomes.length);
	}
	public boolean isInspected() {
		return inspected;
	}

	@Override
	public Iterator<Genome> iterator() {
		return List.of(genomes).iterator();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(genomes);
		result = prime * result + Objects.hash(inspected);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GenomeComponent other = (GenomeComponent) obj;
		return Arrays.equals(genomes, other.genomes) && inspected == other.inspected;
	}

}
