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

package com.khjxiaogu.beecrasy.client;

import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.components.TintColorComponent;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public record BeeTint(int defaultColor) implements ItemTintSource {
    public static final MapCodec<BeeTint> MAP_CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(ExtraCodecs.ARGB_COLOR_CODEC.fieldOf("default").forGetter(BeeTint::defaultColor)).apply(i, BeeTint::new)
    );

    public BeeTint() {
        this(0xffffffff);
    }
    private static final ThreadLocal<Boolean> calculating=ThreadLocal.withInitial(()->false);
    @SuppressWarnings("resource")
	@Override
    public int calculate(ItemStack itemStack,ClientLevel level,LivingEntity owner) {
    	
    	TintColorComponent color=itemStack.get(Components.TINT_COLOR);
    	if(color!=null)
    		return color.color();
    	//避免死循环
    	if(!calculating.get()) {
    		try {
    			calculating.set(true);
		        @Nullable ItemStackTemplate prod=itemStack.get(Components.TINT_STACK);
		        ItemStack product;
		        if(prod==null)
		        	product=itemStack;
		        else
		        	product=prod.create();
		        ItemStackRenderState irs=new ItemStackRenderState();
		        Minecraft.getInstance().getItemModelResolver().appendItemLayers(irs, product, ItemDisplayContext.GUI, level, owner, 0);
		        TextureAtlasSprite sprite=irs.pickParticleMaterial(level.getRandom()).sprite();
		        return TintColorCache.getTintColor(sprite);
    		}finally {
    			calculating.set(false);
    		}
    	}
		return defaultColor;
    }

    @Override
    public MapCodec<BeeTint> type() {
        return MAP_CODEC;
    }
}
