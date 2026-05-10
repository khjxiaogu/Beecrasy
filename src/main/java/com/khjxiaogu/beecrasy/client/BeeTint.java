package com.khjxiaogu.beecrasy.client;

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

public record BeeTint(int defaultColor) implements ItemTintSource {
    public static final MapCodec<BeeTint> MAP_CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(BeeTint::defaultColor)).apply(i, BeeTint::new)
    );

    public BeeTint() {
        this(0xffffffff);
    }
    private static ThreadLocal<Boolean> calculating=ThreadLocal.withInitial(()->false);
    @Override
    public int calculate(ItemStack itemStack,ClientLevel level,LivingEntity owner) {
    	//避免死循环
    	if(!calculating.get()) {
    		try {
    			calculating.set(true);
		        ItemStack product=itemStack;
		        ItemStackRenderState irs=new ItemStackRenderState();
		        Minecraft.getInstance().getItemModelResolver().appendItemLayers(irs, product, ItemDisplayContext.GUI, level, owner, 0);
		        TextureAtlasSprite sprite=irs.pickParticleMaterial(level.getRandom()).sprite();
		        System.out.println(sprite);
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
