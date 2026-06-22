package com.khjxiaogu.beecrasy.data.loot;

import java.util.List;
import java.util.function.Consumer;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.beehive.BeeHiveParameterSet;
import com.khjxiaogu.beecrasy.genome.Genes;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.GenomeDataHelper;
import com.khjxiaogu.beecrasy.genome.GenomeWorkHelper;
import com.khjxiaogu.beecrasy.genome.GenomeWorkHelper.ProductWithCount;
import com.khjxiaogu.beecrasy.genome.gene.ProductItem;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class BeeFamilyPool extends LootPoolSingletonContainer {
	   public static final MapCodec<BeeFamilyPool> MAP_CODEC = RecordCodecBuilder.mapCodec(
	        i -> i.group(GenerateGenomesFunction.MAP_CODEC.forGetter(o->o.func),
	        	NumberProviders.CODEC.fieldOf("drone").forGetter(o->o.droneCount),
	        	NumberProviders.CODEC.fieldOf("queen").forGetter(o->o.queenCount),
	        	NumberProviders.CODEC.fieldOf("comb").forGetter(o->o.combCount)
	        	).and(singletonFields(i)).apply(i, BeeFamilyPool::new)
	    );
	public final GenerateGenomesFunction func;
	public final NumberProvider droneCount;
	public final NumberProvider queenCount;
	public final NumberProvider combCount;
	protected BeeFamilyPool(GenerateGenomesFunction func,NumberProvider drone,NumberProvider queen,NumberProvider comb,int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
		super(weight, quality, conditions, functions);
		this.func=func;
		this.droneCount=drone;
		this.queenCount=queen;
		this.combCount=comb;
	}

	@Override
	public MapCodec<? extends LootPoolSingletonContainer> codec() {
		return MAP_CODEC;
	}

	@Override
	protected void createItemStack(Consumer<ItemStack> output, LootContext context) {
        BeeHiveParameterSet params=new BeeHiveParameterSet.Builder(context.getLevel(), 
        	BlockPos.containing(context.getParameter(LootContextParams.ORIGIN)))
        	.build();
        Genome data=func.applySingle(params, null,0);
		ItemStack drone = Items.DRONE.toStack(droneCount.getInt(context));
		GenomeDataHelper.setHaploidGenome(drone, data);
		output.accept(drone);
		ItemStack queen = Items.QUEEN_BEE.toStack(queenCount.getInt(context));
		GenomeDataHelper.setDiploidGenome(queen, data, data);
		output.accept(queen);
		List<ProductItem> product = data.getAllele(Genes.PRODUCTS);
		if (!product.isEmpty()) {
			for (ProductWithCount i : GenomeWorkHelper.pickProduct(data.getAllele(Genes.BIOTOPE), product, context.getRandom(), combCount.getInt(context))) {
				output.accept(i.createProductComb());
			}
		}
	}
    public static BeeFamilyPool.Builder<?> builder(GenerateGenomesFunction func,NumberProvider drone,NumberProvider queen,NumberProvider comb) {
        return simpleBuilder(
            (weight, quality, conditions, functions) -> new BeeFamilyPool(func,drone,queen,comb, weight, quality, conditions, functions)
        );
    }
}
