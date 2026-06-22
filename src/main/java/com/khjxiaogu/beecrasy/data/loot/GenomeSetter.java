package com.khjxiaogu.beecrasy.data.loot;

import java.util.List;
import java.util.Optional;

import com.khjxiaogu.beecrasy.genome.PartialGenome;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

public record GenomeSetter(Optional<PartialGenome> template,List<Identifier> pools,IntList applies,boolean applyNatural) {
	public static final MapCodec<GenomeSetter> MAP_CODEC = RecordCodecBuilder.mapCodec(
		i->i.group(
		PartialGenome.CODEC.optionalFieldOf("template").forGetter(GenomeSetter::template),
    	ExtraCodecs.compactListCodec(Identifier.CODEC).optionalFieldOf("genetic_pool",List.of()).forGetter(GenomeSetter::pools),
    	ExtraCodecs.compactListCodec(Codec.INT).optionalFieldOf("position",IntList.of(0)).forGetter(GenomeSetter::applies),
    	Codec.BOOL.optionalFieldOf("natural",false).forGetter(GenomeSetter::applyNatural)
    	).apply(i, GenomeSetter::new));
	public GenomeSetter(Optional<PartialGenome> template,List<Identifier> pools,List<Integer> applies,boolean applyNatural) {
		this(template,pools,new IntArrayList(applies),applyNatural);
	}
}