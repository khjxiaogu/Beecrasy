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

package com.khjxiaogu.beecrasy.client.model;

import java.util.function.Consumer;

import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.BeecrasyRegistries.Components;
import com.khjxiaogu.beecrasy.mail.MailComponent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public record MailModel(float dx,float dy,float scale) implements SpecialModelRenderer<ItemStack> {

    public record Unbaked(float dx,float dy,float scale) implements SpecialModelRenderer.Unbaked<ItemStack> {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(t->t.group(
        	Codec.FLOAT.optionalFieldOf("x",0f).forGetter(Unbaked::dx),
        	Codec.FLOAT.optionalFieldOf("y",0f).forGetter(Unbaked::dy),
        	Codec.FLOAT.optionalFieldOf("scale",1f).forGetter(Unbaked::scale)
        	).apply(t, Unbaked::new));
        		
        
        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

		@Override
		public @Nullable SpecialModelRenderer<ItemStack> bake(BakingContext context) {
			return new MailModel(dx,dy,scale);
		}
    }
	@Override
	public void submit(@Nullable ItemStack argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
		if(argument!=null) {
			poseStack.pushPose();
			ItemStackRenderState state=new ItemStackRenderState();
			Minecraft.getInstance().getItemModelResolver().appendItemLayers(state, argument, ItemDisplayContext.GUI, Minecraft.getInstance().level, null, outlineColor);
			poseStack.translate(dx, dy, 1.5f);
			poseStack.scale(scale,scale,1f);
			state.submit(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor);
			poseStack.popPose();
		}
		
	}
	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		
		
	}
	@Override
	public @Nullable ItemStack extractArgument(ItemStack stack) {
		MailComponent mail = stack.get(Components.MAIL);
		if(mail!=null) {
			return mail.icon().map(ItemStackTemplate::create).orElse(null);
		}
		return null;
	}
}
