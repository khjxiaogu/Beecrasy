package com.khjxiaogu.beecrasy.utils;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

public class RecipeCache<T extends Recipe<? extends RecipeInput>> {
	private Map<Identifier,RecipeHolder<T>> recipes;
	public final Supplier<RecipeType<T>> type;
	public RecipeCache(Supplier<RecipeType<T>> type) {
		super();
		this.type = type;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void onRecipeReceived(RecipesReceivedEvent event) {
		recipes=(Map<Identifier, RecipeHolder<T>>) event.getRecipeMap().byType((RecipeType)type.get()).stream().collect(Collectors.toMap(t->((RecipeHolder)t).id().identifier(), t->((RecipeHolder)t)));
	}
	public void onRecipeSend(OnDatapackSyncEvent event) {
		event.sendRecipes(type.get());
	}
	public void registerEvents() {
		NeoForge.EVENT_BUS.addListener(this::onRecipeReceived);
		NeoForge.EVENT_BUS.addListener(this::onRecipeSend);
	}
	public Collection<RecipeHolder<T>> getRecipes(){
		return recipes.values(); 
	}
	public RecipeHolder<T> get(Identifier id){
		return recipes.get(id); 
	}
}
