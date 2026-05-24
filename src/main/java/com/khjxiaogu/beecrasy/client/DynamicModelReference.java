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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

public record DynamicModelReference(StandaloneModelKey<QuadCollection> name) implements Supplier<QuadCollection>
{
	public static final Map<Identifier,DynamicModelReference> models=new HashMap<>();
	private DynamicModelReference(Identifier name)
	{
		this(new StandaloneModelKey<>(name::toString));
	}
	public synchronized static DynamicModelReference createKey(Identifier name) {
		return models.computeIfAbsent(name, DynamicModelReference::new);
		
	}
	public static DynamicModelReference getModel(Identifier rl)
	{
		if(rl==null)
			return null;
		return models.get(rl);
	}
	public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector,QuadInstance quadInstance) {
		submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.cutoutMovingBlock(), (pose,buffer)->{
			for(BakedQuad quad:get().getAll()) {
				buffer.putBakedQuad(pose, quad, quadInstance);
			}
		});
		
	}
	@Override
	public QuadCollection get()
	{
		return Minecraft.getInstance().getModelManager().getStandaloneModel(name);
	}

}