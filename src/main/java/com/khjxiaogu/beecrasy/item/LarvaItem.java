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

import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyConfig;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Items;
import com.khjxiaogu.beecrasy.components.LarvaProductivity;
import com.khjxiaogu.beecrasy.components.WorldCalendar;
import com.khjxiaogu.beecrasy.genome.Genome;
import com.khjxiaogu.beecrasy.genome.GenomeDataHelper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;

public class LarvaItem extends Item {

	public LarvaItem(Properties properties) {
		super(properties);
	}
	public static ItemStack getProduct(ItemStack itemStack, ServerLevel level) {
		int count=itemStack.count();
	
		Genome pheno=GenomeDataHelper.getPhenoType(itemStack);

		LarvaProductivity lp=itemStack.get(Components.LARVA_PRODUCT);
		if(lp!=null) {
			ItemStack ret=lp.getProduction(pheno, level.getRandom());
			ret.setCount(count*ret.count());
			return ret;
		}
		return ItemStack.EMPTY;
	}
	public static boolean isExpired(ItemStack itemStack, long secs) {
		int max=BeecrasyConfig.SERVER.LARVA_SURVIVE_SECS.getAsInt();
		if(max==0)
			return false;
		Long lo=itemStack.get(Components.LARVA_EXPIRES);
		if(lo==null) {
			lo=secs;
			itemStack.set(Components.LARVA_EXPIRES,lo);
		}
		return secs>lo+max;
	}
	public static ItemStack convertToQueen(ItemStack itemStack) {
		ItemStack queenStack=itemStack.transmuteCopy(Items.QUEEN_BEE);
		queenStack.remove(Components.LARVA_PRODUCT);
		queenStack.remove(Components.LARVA_EXPIRES);
		return queenStack;
	}
	@Override
	public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
		super.inventoryTick(itemStack, level, owner, slot);
		long secs=WorldCalendar.getCalendar(level).getSeconds();
		
		if(isExpired(itemStack,secs)) {
			ItemStack ret=getProduct(itemStack, level);
			if(!ret.isEmpty()) {
				if(owner instanceof Player p) {
					p.getInventory().placeItemBackInInventory(ret);
				}else {
					ItemEntity entityitem = new ItemEntity(level, owner.getX(), owner.getY() , owner.getZ(),ret);
		            entityitem.setPickUpDelay(0);
		            entityitem.setDeltaMovement(entityitem.getDeltaMovement().multiply(0, 1, 0));
	
		            level.addFreshEntity(entityitem);
				}
			}
			itemStack.setCount(0);
		}
	}
    @SuppressWarnings("resource")
	@Override
	public boolean onEntityItemUpdate(ItemStack itemStack, ItemEntity entity) {
    	if(entity.level() instanceof ServerLevel level){
			long secs=WorldCalendar.getCalendar(level).getSeconds();
			
			if(isExpired(itemStack,secs)) {
				ItemStack ret=getProduct(itemStack, level);
				if(!ret.isEmpty()) {
					ItemUtils.onContainerDestroyed(entity, Stream.of(ret));
				}
				itemStack.setCount(0);
				entity.discard();
				return true;
			}
    	}
    	return super.onEntityItemUpdate(itemStack, entity);
	}
	@SuppressWarnings("resource")
	@Override
    public void onDestroyed(ItemEntity entity) {
		if(entity.level() instanceof ServerLevel level){
			ItemStack ret=getProduct(entity.getItem(), level);
			if(!ret.isEmpty()) {
				ItemUtils.onContainerDestroyed(entity, Stream.of(ret));
			}
		}
    }
}
