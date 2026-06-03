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

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.beehive.BeeHiveBaseComponent;
import com.khjxiaogu.beecrasy.components.BeeHiveArgumentation.Builder;
import com.khjxiaogu.beecrasy.menu.SkepMenu;
import com.khjxiaogu.beecrasy.utils.ItemValidateHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class SkepBlockEntity extends BeeHiveBaseBlockEntity implements MenuProvider{
	public static class SkepComponent extends BeeHiveBaseComponent{

		public SkepComponent(int queen, int drone, int comb, int extra) {
			super(queen, drone, comb, extra);
		}

		@Override
		public boolean isValidForExtra(int index, ItemResource resource) {
			return ItemValidateHelper.isArgument(resource.toStack());
		}
		@Override
		public Builder buildArgumentation(ServerLevel level,BlockPos worldPosition,TransactionContext root) {
			
			return super.buildArgumentation(level,worldPosition,root)
				.addParams(super.extractArgumentation(level, 9, root))
				.addParams(super.extractArgumentation(level, 10, root));
		}
	}
	public SkepBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(Blocks.SKEP_BLOCKENTITY.get(), pWorldPosition, pBlockState, new SkepComponent(1, 4, 4, 2));
	}

	@Override
	public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
		return new SkepMenu(containerId,inventory,this);
	}

	@Override
	public Component getDisplayName() {
		return Blocks.SKEP.get().getName();
	}






}
