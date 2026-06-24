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

package com.khjxiaogu.beecrasy.blocks.bee.beecity;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.beehive.BeeCityComponent;
import com.khjxiaogu.beecrasy.beehive.WorkBehaviour;
import com.khjxiaogu.beecrasy.blocks.bee.BeeHiveBaseBlockEntity;
import com.khjxiaogu.beecrasy.menu.BeeCityCoreMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BeeCityCoreBlockEntity extends BeeHiveBaseBlockEntity implements MenuProvider{
	public final BeeCityComponent component;
	public BeeCityCoreBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(Blocks.BEE_CITY_CORE_BLOCKENTITY.get(), pWorldPosition, pBlockState,component=new BeeCityComponent(1,2,2,1));
	}


	@Override
	public void tick() {
		if(level instanceof ServerLevel serverLevel) {
			component.work=WorkBehaviour.AUTO;
			component.level=serverLevel;
			component.tick(serverLevel, worldPosition, 1, true);
			if(component.isChanged()) {
				setChanged();
				component.setChanged(false);
			}
			boolean oldstate=this.getBlockState().getValue(BlockStateProperties.LIT);
			boolean newstate=component.hiveInfo.isWorking();
			if(oldstate!=newstate) {
				BlockState nextstate=getBlockState().setValue(BlockStateProperties.LIT, newstate);
				this.level.setBlockAndUpdate(worldPosition, nextstate);
			}
		}
	}
	@Override
	public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
		return new BeeCityCoreMenu(containerId,inventory,this);
	}

	@Override
	public Component getDisplayName() {
		return Blocks.BEE_CITY_COMB.get().getName();
	}
}
