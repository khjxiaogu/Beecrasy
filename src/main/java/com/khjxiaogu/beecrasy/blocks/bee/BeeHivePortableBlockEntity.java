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

package com.khjxiaogu.beecrasy.blocks.bee;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.beehive.BeeHiveBaseComponent;
import com.khjxiaogu.beecrasy.beehive.BeeHiveBaseComponent.BeeHiveBaseData;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueOutput;

public class BeeHivePortableBlockEntity extends BeeHiveBaseBlockEntity{
	public BeeHiveBaseComponent component;
	public BeeHivePortableBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState, BeeHiveBaseComponent component) {
		super(pType, pWorldPosition, pBlockState, component);
		this.component=component;
	}
	@SuppressWarnings("deprecation")
	@Override
	public void removeComponentsFromTag(ValueOutput output) {

		output.discard("inv");
		output.discard("hive");
		output.discard("nextWork");
		output.discard("arguments");
		output.discard("work");
		super.removeComponentsFromTag(output);
	}
	@Override
	protected void applyImplicitComponents(DataComponentGetter components) {
		BeeHiveBaseData hive=components.get(Components.BEE_HIVE);
		if(hive!=null)
		this.component.load(hive);
		super.applyImplicitComponents(components);
	}

	@Override
	protected void collectImplicitComponents(net.minecraft.core.component.DataComponentMap.Builder components) {
		components.set(Components.BEE_HIVE, component.save());
	}
}
