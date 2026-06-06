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

package com.khjxiaogu.beecrasy.item;

import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.beehive.BeeHiveBaseComponent;
import com.khjxiaogu.beecrasy.beehive.BeeHiveBaseComponent.BeeHiveBaseData;
import com.khjxiaogu.beecrasy.client.BeecrasyParticles;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class BeehiveBlockItem extends BlockItem {
	protected final Supplier<BeeHiveBaseComponent> factory;
	
	public BeehiveBlockItem(Block block, Properties properties, Supplier<BeeHiveBaseComponent> factory) {
		super(block, properties);
		this.factory = factory;
	}
	
	@Override
	public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {

		BeeHiveBaseData data=itemStack.get(Components.BEE_HIVE);
		if(data!=null) {
			if(owner.tickCount%20==0) {
				
					BeeHiveBaseComponent beehive=factory.get();
					beehive.load(data);
					beehive.tick(level,BlockPos.containing(owner.position()), 20, false);
					if(beehive.isChanged()) {
						itemStack.set(Components.BEE_HIVE, beehive.save());
					}
				
			}
			if(data.hiveInfo().processMax()>0) {
					RandomSource rnd=owner.getRandom();
					if(rnd.nextFloat()<0.25)
						level.sendParticles(BeecrasyParticles.BEE.get().random(),owner.getX()+rnd.nextGaussian()*0.5d,owner.getY()+rnd.nextGaussian()*0.5d,owner.getZ()+rnd.nextGaussian()*0.5d,0, 0,0, 0,0);

			}
		}
	}




}
