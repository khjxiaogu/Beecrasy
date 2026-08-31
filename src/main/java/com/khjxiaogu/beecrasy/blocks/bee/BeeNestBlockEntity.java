package com.khjxiaogu.beecrasy.blocks.bee;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyRegistries;
import com.khjxiaogu.beecrasy.blocks.BeecrasyBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;

public class BeeNestBlockEntity extends BeecrasyBlockEntity{
	protected @Nullable ResourceKey<LootTable> lootTable;
    protected long lootTableSeed = 0L;
	public BeeNestBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(BeecrasyRegistries.Blocks.BEE_NEST_BLOCKENTITY.get(), pWorldPosition, pBlockState);
	}

	@Override
	public void readCustomNBT(ValueInput nbt, boolean isClient) {
		lootTable= nbt.read("LootTable", LootTable.KEY_CODEC).orElse(null);
		lootTableSeed = nbt.getLongOr("LootTableSeed", 0L);
	}

	@Override
	public void writeCustomNBT(ValueOutput nbt, boolean isClient) {
		nbt.storeNullable("LootTable", LootTable.KEY_CODEC, lootTable);
		if (lootTableSeed != 0L) {
			nbt.putLong("LootTableSeed", lootTableSeed);
        }
	}

	@Override
	public void tick() {
		
	}
    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        SeededContainerLoot loot = components.get(DataComponents.CONTAINER_LOOT);
        if (loot != null) {
            this.lootTable = loot.lootTable();
            this.lootTableSeed = loot.seed();
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (this.lootTable != null) {
            components.set(DataComponents.CONTAINER_LOOT, new SeededContainerLoot(this.lootTable, this.lootTableSeed));
        }
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard("LootTable");
        output.discard("LootTableSeed");
    }
}
