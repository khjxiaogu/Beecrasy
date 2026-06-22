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

package com.khjxiaogu.beecrasy.data.loot;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterSet;
import com.khjxiaogu.beecrasy.components.GenomeComponent;
import com.khjxiaogu.beecrasy.events.NaturalBeeGenomeGenerateEvent;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.PartialGenome;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.NeoForge;

public class GenerateGenomesFunction extends LootItemConditionalFunction {
    public static final MapCodec<GenerateGenomesFunction> MAP_CODEC = 
    	RecordCodecBuilder.mapCodec(
        i -> commonFields(i)
            .and(
            
            	Codec.mapEither(GenomeSetter.MAP_CODEC,Codec.list(GenomeSetter.MAP_CODEC.codec())
                	.fieldOf("generators"))
            	.xmap(t->t.map(l->List.of(l),r->r), e->e.size()==1?Either.left(e.get(0)):Either.right(e)).forGetter(o->o.setters)

            )
            .apply(i, GenerateGenomesFunction::new)
    );
    final List<GenomeSetter> setters;
    final int maxComponents;

	public GenerateGenomesFunction(List<LootItemCondition> predicates, List<GenomeSetter> setters) {
		super(predicates);
		this.setters = setters;
		int maxComp=0;
        for(GenomeSetter gs:setters) {
        	for(int i:gs.applies())
        	maxComp=Math.max(maxComp, i);
        }
        maxComponents=maxComp+1;
	}
	public GenerateGenomesFunction(List<LootItemCondition> predicates, GenomeSetter... setters) {
		this(predicates,List.of(setters));
	}
	@Override
    public MapCodec<GenerateGenomesFunction> codec() {
        return MAP_CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }
    public Genome applySingle(BeeHiveParameterSet params,Genome data,int index) {
    	Genome.Builder builder;
        if (data != null) {
        	builder=data.createBuilder();
        }else {
        	builder=new Genome.Builder();
        }
        for(GenomeSetter setter:setters) {
        	if(!setter.applies().contains(index))
        		continue;
        	NaturalBeeGenomeGenerateEvent nbgge=new NaturalBeeGenomeGenerateEvent(params, setter.template().map(PartialGenome::createBuilder).orElseGet(PartialGenome.Builder::new));
        	if(setter.applyNatural()) {
        		NeoForge.EVENT_BUS.post(nbgge);
        	}
        	for(Identifier id:setter.pools()) {
        		nbgge.applyPools(id);
        	}
        	PartialGenome pg=nbgge.genome.build();
        	builder=pg.apply(builder);
        }
        return builder.build();
    }
    public GenomeComponent apply(BeeHiveParameterSet params,GenomeComponent data) {
    	Genome.Builder[] builders;

        	
        if (data != null) {
        	builders=new Genome.Builder[Math.max(maxComponents, data.size())];
        	for(int i=0;i<data.size();i++)
        		builders[i]=data.getGenome(i).createBuilder();
        }else {
        	builders=new Genome.Builder[maxComponents];
        }
        for(int i=0;i<builders.length;i++) {
        	if(builders[i]==null)
        		builders[i]=new Genome.Builder();
        }
        for(GenomeSetter setter:setters) {
        	
        	NaturalBeeGenomeGenerateEvent nbgge=new NaturalBeeGenomeGenerateEvent(params, setter.template().map(PartialGenome::createBuilder).orElseGet(PartialGenome.Builder::new));
        	if(setter.applyNatural()) {
        		NeoForge.EVENT_BUS.post(nbgge);
        	}
        	for(Identifier id:setter.pools()) {
        		nbgge.applyPools(id);
        	}
        	PartialGenome pg=nbgge.genome.build();
        	for(int pos:setter.applies()) {
        		builders[pos]=pg.apply(builders[pos]);
        	}
        }
        Genome[] genomes=new Genome[builders.length];
        for(int i=0;i<builders.length;i++) {
        	genomes[i]=builders[i].build();
        }
        return new GenomeComponent(false,genomes);
    }
    @Override
    public ItemStack run(ItemStack itemStack, LootContext context) {
        GenomeComponent data=itemStack.get(Components.GENOME);
        BeeHiveParameterSet params=new BeeHiveParameterSet.Builder(context.getLevel(), 
        	BlockPos.containing(context.getParameter(LootContextParams.ORIGIN)))
        	.build();
        itemStack.set(Components.GENOME,apply(params,data));
        return itemStack;
    }
    public static Builder builder() {
    	return new Builder();
    }
    

    public static class Builder extends LootItemConditionalFunction.Builder<GenerateGenomesFunction.Builder> {
    	List<GenomeSetter> setters=new ArrayList<>();
    	public static class GenomeSetterBuilder{
    		private final Builder builderThis;
    		private Optional<PartialGenome> template=Optional.empty();
    		private List<Identifier> pools=new ArrayList<>();
    		private IntList applies=new IntArrayList();
    		private boolean applyNatural=false;
    		
    		public GenomeSetterBuilder(Builder builderThis) {
				super();
				this.builderThis = builderThis;
			}

			public GenomeSetterBuilder setTemplate(UnaryOperator<PartialGenome.Builder> op) {
    			template=Optional.of(op.apply(new PartialGenome.Builder()).build());
    			return this;
    		}
			public GenomeSetterBuilder setNatural(boolean natural) {
				this.applyNatural=natural;
    			return this;
			}
			public GenomeSetterBuilder setNatural() {
    			return setNatural(true);
			}
			public GenomeSetterBuilder addPool(Identifier pool) {
				pools.add(pool);
    			return this;
			}
			public GenomeSetterBuilder addPos(int pos) {
				applies.add(pos);
    			return this;
			}
			public Builder add() {
				builderThis.setters.add(new GenomeSetter(template,pools,applies,applyNatural));
				return builderThis;
			}
    	}
        private Builder() {
        }

        protected GenerateGenomesFunction.Builder getThis() {
            return this;
        }
        public GenomeSetterBuilder begin() {
        	return new GenomeSetterBuilder(this);
        }
        @Override
        public GenerateGenomesFunction build() {
            return new GenerateGenomesFunction(
                this.getConditions(), setters
            );
        }
    }
}
