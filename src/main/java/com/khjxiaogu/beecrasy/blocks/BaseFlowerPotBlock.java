package com.khjxiaogu.beecrasy.blocks;



import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.neoforge.registries.DeferredBlock;

public class BaseFlowerPotBlock extends FlowerPotBlock {

	public BaseFlowerPotBlock(DeferredBlock<? extends Block> potted,
			Properties properties) {
		super(()->(FlowerPotBlock)Blocks.FLOWER_POT, potted, properties);
		((FlowerPotBlock)Blocks.FLOWER_POT).addPlant(potted.getId(), () -> this);
	}

}
