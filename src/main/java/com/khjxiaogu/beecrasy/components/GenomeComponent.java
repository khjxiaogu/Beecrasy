package com.khjxiaogu.beecrasy.components;

import java.util.List;
import java.util.Optional;

import com.khjxiaogu.beecrasy.genome.AllelesHolder;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class GenomeComponent {

	public static final GenomeComponent EMPTY=new GenomeComponent();
	public static final Codec<GenomeComponent> CODEC=RecordCodecBuilder.create(t->t.group(
		Codec.BOOL.fieldOf("inspected").forGetter(o->o.inspected),
		Genome.CODEC.fieldOf("genome_1").forGetter(o->o.genomes[0]),
		Genome.CODEC.optionalFieldOf("genome_2").forGetter(o->o.genomes.length>1?Optional.of(o.genomes[1]):Optional.empty())
		).apply(t, GenomeComponent::new)
		);
	public static final StreamCodec<RegistryFriendlyByteBuf,GenomeComponent> EMPTY_CODEC=StreamCodec.unit(EMPTY);
	public static final StreamCodec<RegistryFriendlyByteBuf,GenomeComponent> FULL_CODEC=Genome.STREAM_CODEC.apply(ByteBufCodecs.list())
		.map(t->new GenomeComponent(true,t), t->List.of(t.genomes));
	public static final StreamCodec<RegistryFriendlyByteBuf,GenomeComponent> NETWORK_CODEC=ByteBufCodecs.BOOL
		.<RegistryFriendlyByteBuf>cast()
		.dispatch(t->t.inspected,
		n->(n?FULL_CODEC:EMPTY_CODEC));
	private final boolean inspected;
	private final Genome[] genomes;
	private GenomeComponent() {
		inspected=false;
		this.genomes = new Genome[] {Genome.DEFAULT};
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
		return new GenomeComponent(true,genomes);
	}
	public int size() {
		return genomes.length;
	}
	public AllelesHolder getGenome(int index) {
		return genomes[index];
	}

}
