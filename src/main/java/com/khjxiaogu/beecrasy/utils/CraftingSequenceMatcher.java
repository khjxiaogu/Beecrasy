package com.khjxiaogu.beecrasy.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.utils.CraftingRecipeSequence.SequencedRecipe;
import com.khjxiaogu.beecrasy.utils.CraftingRecipeSequence.UnordererRecipeSequence;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class CraftingSequenceMatcher {
	public static CraftingRecipeSequence ordered;
	public static UnordererRecipeSequence unordered;
	public static void bake(Collection<RecipeHolder<CraftingRecipe>> recipes) {
		ordered=new CraftingRecipeSequence();
		unordered=new UnordererRecipeSequence();
		Beecrasy.LOGGER.info("Baking Recipe Ingredients");
		List<SequencedRecipe> toadd=new ArrayList<>(recipes.size());
		outer:for(RecipeHolder<CraftingRecipe> recipe:recipes) {
			CraftingRecipe rcp=recipe.value();
			if(rcp instanceof ShapedRecipe sr&&rcp.getClass()==ShapedRecipe.class) {
				ItemStackTemplate result=sr.result;
				
				for(Optional<Ingredient> ig:sr.pattern.ingredients()) {
					if(ig.isPresent()) {
						Ingredient igd=ig.get();
						//仅支持原版无NBT原料，不支持自定义配方
						if(!isValidIngredient(igd))
							continue outer;
						//避免循环配方，粗略判断包含相同物品即可排除
						if(igd.acceptsItem(result.item()))
							continue outer;
					}
				}
				SequencedRecipe seqr=new SequencedRecipe(recipe);
				toadd.add(seqr);
			}else if(rcp instanceof ShapelessRecipe sr&&rcp.getClass()==ShapelessRecipe.class) {
				ItemStackTemplate result=sr.result;
				
				for(Ingredient igd:sr.ingredients) {
					//仅支持原版无NBT原料，不支持自定义配方
					if(!isValidIngredient(igd))
						continue outer;
					//避免循环配方，粗略判断包含相同物品即可排除
					if(igd.acceptsItem(result.item()))
						continue outer;
				
				}
				SequencedRecipe seqr=new SequencedRecipe(recipe);
				toadd.add(seqr);
			}
		}
		Beecrasy.LOGGER.info("Baking Ordered Recipe Index");
		ordered.insertAll(toadd);
		Beecrasy.LOGGER.info("Baking Unordered Recipe Index");
	
		unordered.insertAll(toadd);
		
		Beecrasy.LOGGER.info("Recipe Baking Complete");
	}
	public static boolean isValidIngredient(Ingredient igd) {
		return igd.isSimple()&&!igd.isCustom();
	}
	public static Collection<RecipeHolder<CraftingRecipe>> matchOrdered(List<ItemStack> matcher) {
		if(ordered==null)
			return Set.of();
		return ordered.match(matcher);
	};
	public static Collection<RecipeHolder<CraftingRecipe>> matchUnordered(List<ItemStack> matcher) {
		if(unordered==null)
			return Set.of();
		List<ItemStack> sorted=new ArrayList<>(matcher);
		sorted.sort(Comparator.comparingInt(t->t.getItem().hashCode()));
		return unordered.match(sorted);
	};
}
