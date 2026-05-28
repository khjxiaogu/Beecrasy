package com.khjxiaogu.beecrasy.item;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
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

public class LarvaItem extends Item {

	public LarvaItem(Properties properties) {
		super(properties);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {

		super.inventoryTick(itemStack, level, owner, slot);
		Long lo=itemStack.get(Components.LARVA_EXPIRES);
		long secs=level.getServer().getDataStorage().computeIfAbsent(WorldCalendar.TYPE).getSeconds();
		if(lo==null) {
			lo=secs;
			itemStack.set(Components.LARVA_EXPIRES,lo);
		}
		if(secs>lo+600) {
			int count=itemStack.count();
			
			Genome pheno=GenomeDataHelper.getPhenoType(itemStack);

			LarvaProductivity lp=itemStack.get(Components.LARVA_PRODUCT);
			if(lp!=null) {
				ItemStack ret=lp.getProduction(pheno, level.getRandom());
				ret.setCount(count*ret.count());
				if(owner instanceof Player p) {
					p.getInventory().placeItemBackInInventory(ret);
				}else {
					ItemEntity entityitem = new ItemEntity(level, owner.getX(), owner.getY() , owner.getZ(),ret);
		            entityitem.setPickUpDelay(40);
		            entityitem.setDeltaMovement(entityitem.getDeltaMovement().multiply(0, 1, 0));

		            level.addFreshEntity(entityitem);
				}
			}
			itemStack.shrink(count);
		}
	}

}
