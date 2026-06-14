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

package com.khjxiaogu.beecrasy.blocks;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.beehive.BeeHiveBaseComponent;
import com.khjxiaogu.beecrasy.beehive.WorkBehaviour;
import com.khjxiaogu.beecrasy.beehive.BeeHiveBaseComponent.BeeHiveBaseData;
import com.khjxiaogu.beecrasy.beehive.BeeHiveHandler.DataRecord;
import com.khjxiaogu.beecrasy.components.BeeHiveArgumentation;
import com.khjxiaogu.beecrasy.components.BeeHiveArgumentation.Builder;
import com.khjxiaogu.beecrasy.menu.HiveMenu;
import com.khjxiaogu.beecrasy.utils.ItemValidateHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class HiveBlockEntity extends BeeHiveBaseBlockEntity implements MenuProvider{
	public static final BeeHiveBaseData EMPTY=new BeeHiveBaseData(2, 6, 6, DataRecord.EMPTY, Optional.empty(), WorkBehaviour.MAUNAL);
	public static class HiveComponent extends BeeHiveBaseComponent{

		public HiveComponent(int queen, int drone, int comb, int extra) {
			super(queen, drone, comb, extra);
		}

		@Override
		public boolean isValidForExtra(int index, ItemResource resource) {
			return ItemValidateHelper.isArgument(resource.toStack());
		}
		@Override
		public Builder buildArgumentation(ServerLevel level,BlockPos worldPosition,TransactionContext root) {

			Builder builder= super.buildArgumentation(level,worldPosition,root);
			BeeHiveArgumentation arg1=super.extractArgumentation(level, 14, root);
			if(arg1!=null)
				builder.addParams(arg1);
			BeeHiveArgumentation arg2=super.extractArgumentation(level, 15, root);
			if(arg2!=null)
				builder.addParams(arg2);
			BeeHiveArgumentation arg3=super.extractArgumentation(level, 16, root);
			if(arg3!=null)
				builder.addParams(arg3);
			return builder;
		}
	}
	public HiveBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(Blocks.HIVE_BLOCKENTITY.get(), pWorldPosition, pBlockState, new HiveComponent(2, 6, 6, 3));
	}

	@Override
	public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
		return new HiveMenu(containerId,inventory,this);
	}

	@Override
	public Component getDisplayName() {
		return Blocks.HIVE.get().getName();
	}

	public ItemStack getItem() {
		ItemStack stack=new ItemStack(Blocks.HIVE.asItem(),1);
		stack.applyComponents(collectComponents());
		return stack;
	}
}
