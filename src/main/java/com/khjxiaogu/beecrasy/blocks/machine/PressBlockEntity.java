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

package com.khjxiaogu.beecrasy.blocks.machine;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Blocks;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.BeecrasyRegistries.Recipes;
import com.khjxiaogu.beecrasy.blocks.BeecrasyBlockEntity;
import com.khjxiaogu.beecrasy.data.recipe.PressRecipe;
import com.khjxiaogu.beecrasy.data.recipe.RandomizableRecipeInput;
import com.khjxiaogu.beecrasy.menu.PressMenu;
import com.khjxiaogu.beecrasy.utils.RecipeHandleStatus;
import com.khjxiaogu.beecrasy.utils.RecipeHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class PressBlockEntity extends BeecrasyBlockEntity implements MenuProvider{
	public int currentTicks;
	public int maxTicks;
	public int currentMaxTicks;
	public int powerRemain;
	public static final int ACCESSIBLE_SLOTS=10;
	private ItemStacksResourceHandler internInv = new ItemStacksResourceHandler(ACCESSIBLE_SLOTS) {
		@Override
		protected void onContentsChanged(int slot, ItemStack stack) {
			if(slot==0)
				getRecipeHandler().onContainerChanged();
			setChanged();
			super.onContentsChanged(slot, stack);
		}
		
	};
	private ResourceHandler<ItemResource> externInv=new DelegatingResourceHandler<>(internInv) {

		@Override
		public boolean isValid(int index, ItemResource resource) {
			if(index==0)
				return super.isValid(index, resource)&&getRecipe(getInput(level.getRandom(),resource.toStack()))!=null;
			return false;
		}

		@Override
		public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
			if(index==0)
				return 0;
			return super.extract(index, resource, amount, transaction);
		}
		
	};
	
	public FluidStacksResourceHandler tank = new FluidStacksResourceHandler(1,1000) {
		@Override
		protected void onContentsChanged(int slot,FluidStack stackBefore) {
			setChanged();
			super.onContentsChanged(slot,stackBefore);
		}
	};
	public DelegatingResourceHandler<FluidResource> externTank = new DelegatingResourceHandler<>(tank) {
		@Override
		public boolean isValid(int index, FluidResource resource) {
			return false;
		}
		@Override
		public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
			return 0;
		}

		@Override
		public int insert(FluidResource resource, int amount, TransactionContext transaction) {
			return 0;
		}

	
	};
	protected final RecipeHandler<PressRecipe> recipeHandler=new RecipeHandler<>(id->{
		RandomizableRecipeInput input=getInput();
		RecipeHolder<PressRecipe> recipe =getRecipe(input);
		if(recipe!=null&&recipe.id().identifier().equals(id)) {
			try(Transaction trans=Transaction.openRoot()){
				int inputCount=recipe.value().input().count();
				ItemResource inrs=getInternInv().getResource(0);
				if(getInternInv().extract(inrs, inputCount, trans)!=inputCount) {
					return RecipeHandleStatus.FAILED;
				}
				if(recipe.value().fluid().isPresent()) {
					FluidStack stack=recipe.value().fluid().get().create();
					if(tank.insert(FluidResource.of(stack), stack.amount(), trans)!=stack.amount())
						return RecipeHandleStatus.BLOCKED;
				}
				ItemStackTemplate ist=inrs.get(Components.COMB_PRODUCT);
				if(ist!=null&&level.getRandom().nextFloat()<=0.33) {
					if(getInternInv().insert(ItemResource.of(ist), 1, trans)!=1)
						return RecipeHandleStatus.BLOCKED;
				}
				
				for(ItemStack is:recipe.value().getOutputs(input)) {
					ItemResource resource=ItemResource.of(is);
					int reminder=is.count();
			        int size = getInternInv().size();
			        for (int index = 1; index < size; index++) {
			        	reminder -= getInternInv().insert(index, resource, reminder, trans);
			            if (reminder<=0) break;
			        }
			        if (reminder>0) 
			        	return RecipeHandleStatus.BLOCKED;
				}
				trans.commit();
				powerRemain=0;
				return RecipeHandleStatus.SUCCEED;
			}
		}
		return RecipeHandleStatus.FAILED;
	}) ;
	public PressBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(Blocks.PRESS_BLOCKENTITY.get(), pWorldPosition, pBlockState);
	}
	public RandomizableRecipeInput getInput() {
		return getInput(level.getRandom(),getInternInv().getResource(0).toStack(getInternInv().getAmountAsInt(0)));
	}
	public static RandomizableRecipeInput getInput(RandomSource rs,ItemStack stack) {
		return new RandomizableRecipeInput(new SingleRecipeInput(stack),rs);
	}
	public RecipeHolder<PressRecipe> getRecipe(RandomizableRecipeInput input){
		if(this.level.recipeAccess() instanceof RecipeManager manager)
			return manager.getRecipeFor(Recipes.PRESS_TYPE.get(),input,level).orElse(null);
		return null;
	}
	@Override
	public void readCustomNBT(ValueInput nbt, boolean isClient) {

		if(!isClient) {
			getInternInv().deserialize(nbt.childOrEmpty("inventory"));
			tank.deserialize(nbt.childOrEmpty("tank"));
			getRecipeHandler().readCustomNBT(nbt, isClient);
		}else {
			currentTicks=nbt.getIntOr("process", 0);
			maxTicks=nbt.getIntOr("processMax", 0);
			currentMaxTicks=nbt.getIntOr("currentMax", 0);
		}
		powerRemain=nbt.getIntOr("power", powerRemain);
	}

	@Override
	public void writeCustomNBT(ValueOutput nbt, boolean isClient) {
		if(!isClient) {
			getInternInv().serialize(nbt.child("inventory"));
			tank.serialize(nbt.child("tank"));
			getRecipeHandler().writeCustomNBT(nbt, isClient);
			
		}else {
			nbt.putInt("process", getRecipeHandler().getFinishedProgress());
			nbt.putInt("processMax", getRecipeHandler().getProcessMax());
			if(getRecipeHandler().getFinishedProgress()>=getRecipeHandler().getProcessMax()/2)
				nbt.putInt("currentMax", getRecipeHandler().getProcessMax());
			else
				nbt.putInt("currentMax", getRecipeHandler().getProcessMax()/2);
		}
		nbt.putInt("power", powerRemain);
	}
	public void refillPower() {
		if(powerRemain<20) {
			powerRemain=40;
			this.sendUpdated();
		}
	}
	@Override
	public void tick() {
		if (this.level.isClientSide()) {
			if(powerRemain>0) {
				if(currentTicks<currentMaxTicks) {
					currentTicks++;
					powerRemain--;
				}
			}
			return;
		}
		
		if(getRecipeHandler().shouldTestRecipe()){
			RecipeHolder<PressRecipe> recipe=getRecipe(getInput());
			if(getRecipeHandler().setRecipe(recipe,recipe==null?0:recipe.value().time())) {

				powerRemain=0;
				this.sendUpdated();
			}
			
		}
		if(powerRemain>0) {
			if (getRecipeHandler().tickProcess(1)) {
				powerRemain--;
				int process=getRecipeHandler().getProcess();
				int halfMax=getRecipeHandler().getProcessMax()/2;
				if(process==halfMax)
					this.sendUpdated();
				this.setChanged();
			}
		}
	}
	public RecipeHandler<PressRecipe> getRecipeHandler() {
		return recipeHandler;
	}
	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
		return new PressMenu(containerId,inventory,this);
	}
	@Override
	public Component getDisplayName() {
		return Component.translatable("block.beecrasy.honey_press");
	}
	public ItemStacksResourceHandler getInternInv() {
		return internInv;
	}
	public ResourceHandler<ItemResource> getExternInv() {
		return externInv;
	}
	public DelegatingResourceHandler<FluidResource> getExternTank() {
		return externTank;
	}

}
