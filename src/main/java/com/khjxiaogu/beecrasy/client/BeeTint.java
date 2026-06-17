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
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * 自定义物品染色源，用于蜜蜂产品的动态染色。
 * <p>
 * 染色策略采用三层降级机制：
 * <ol>
 *   <li>优先读取物品上的 {@link TintColorComponent} 数据组件指定的颜色；</li>
 *   <li>若组件不存在，则通过纹理分析提取物品的主色调作为染色基准；</li>
 *   <li>若纹理分析因递归保护而跳过，则使用默认颜色。</li>
 * </ol>
 * 使用 {@link ThreadLocal} 标记 {@code calculating} 防止 {@code calculate} 递归调用
 * 导致死循环（纹理分析过程中可能间接请求染色）。
 *
 * @param defaultColor 默认 ARGB 颜色值，当无法从组件或纹理获取颜色时使用
 */
public record BeeTint(int defaultColor) implements ItemTintSource {
    /**
     * 编解码器，包含默认颜色字段 "default"（ARGB 格式）。
     */
    public static final MapCodec<BeeTint> MAP_CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(ExtraCodecs.ARGB_COLOR_CODEC.fieldOf("default").forGetter(BeeTint::defaultColor)).apply(i, BeeTint::new)
    );

    /**
     * 使用纯白 ({@code 0xffffffff}) 作为默认颜色构造染色源。
     */
    public BeeTint() {
        this(0xffffffff);
    }

    /**
     * 线程局部标记，用于防止 {@link #calculate} 方法递归调用导致死循环。
     * 当正在通过纹理分析提取颜色时设为 {@code true}，分析完成后恢复为 {@code false}。
     */
    private static final ThreadLocal<Boolean> calculating=ThreadLocal.withInitial(()->false);
    /**
     * 计算物品栈的染色颜色。
     * <p>
     * 采用三层降级策略：
     * <ol>
     *   <li>检查物品上的 {@link TintColorComponent} 数据组件并返回其颜色；</li>
     *   <li>若组件不存在且未处于递归保护中，通过 {@link TintColorCache} 分析物品
     *       纹理提取主色调；</li>
     *   <li>若纹理分析因递归保护而跳过（即 {@code calculating} 为 {@code true}），
     *       则返回默认颜色。</li>
     * </ol>
     *
     * @param itemStack 需要计算染色颜色的物品栈
     * @param level     客户端所在世界（可为 null）
     * @param owner     持有该物品的实体（可为 null）
     * @return 计算得到的 ARGB 颜色值
     */
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
		        ItemStackTemplate prod=itemStack.get(Components.TINT_STACK);
		        ItemStack product;
		        if(prod==null)
		        	product=itemStack;
		        else
		        	product=prod.create();
		        ItemStackRenderState irs=new ItemStackRenderState();
		        Minecraft.getInstance().getItemModelResolver().appendItemLayers(irs, product, ItemDisplayContext.GUI, level, owner, 0);
		        TextureAtlasSprite sprite=irs.pickParticleMaterial(level==null?RandomSource.create():level.getRandom()).sprite();
		        return TintColorCache.getTintColor(sprite);
    		}finally {
    			calculating.set(false);
    		}
    	}
		return defaultColor;
    }

    /**
     * 返回此染色源的编解码器类型。
     *
     * @return {@link #MAP_CODEC}
     */
    @Override
    public MapCodec<BeeTint> type() {
        return MAP_CODEC;
    }
}
