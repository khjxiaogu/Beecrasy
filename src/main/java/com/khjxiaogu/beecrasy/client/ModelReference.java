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

import com.khjxiaogu.beecrasy.Beecrasy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

/**
 * 静态独立模型引用的记录类。
 * <p>
 * 通过 {@link StandaloneModelKey} 延迟从模型管理器获取 {@link QuadCollection}。
 * 提供全局缓存机制避免重复创建。
 *
 * @param name 用于在模型管理器中查找 {@link QuadCollection} 的独立模型键
 */
public record ModelReference(StandaloneModelKey<QuadCollection> name) implements Supplier<QuadCollection>
{
	/**
	 * 全局缓存的标识符到模型引用的映射。
	 * 通过 {@link #createKey} 和 {@link #getModel} 方法进行访问。
	 */
	public static final Map<Identifier,ModelReference> models=new HashMap<>();
	/**
	 * 使用指定的标识符构造模型引用。
	 * 内部创建 {@link StandaloneModelKey}，以标识符的字符串表示作为键名。
	 *
	 * @param name 模型标识符
	 */
	private ModelReference(Identifier name)
	{
		this(new StandaloneModelKey<>(name::toString));
	}
	/**
	 * 获取或创建指定路径字符串对应的模型引用。
	 * <p>
	 * 将路径字符串通过 {@link Beecrasy#rl(String)} 转换为模组命名空间下的标识符，
	 * 然后委托给 {@link #createKey(Identifier)}。
	 *
	 * @param path 模型路径字符串（自动添加模组命名空间）
	 * @return 对应路径的 {@code ModelReference} 实例
	 */
	public synchronized static ModelReference createKey(String path) {
		return createKey(Beecrasy.rl(path));
	}

	/**
	 * 获取或创建指定标识符对应的模型引用。
	 * <p>
	 * 线程安全（同步方法）。若缓存中已存在对应引用则直接返回，否则创建新引用并加入缓存。
	 *
	 * @param name 模型标识符
	 * @return 对应标识符的 {@code ModelReference} 实例
	 */
	public synchronized static ModelReference createKey(Identifier name) {
		return models.computeIfAbsent(name, ModelReference::new);
	}
	/**
	 * 从全局缓存中查找指定标识符对应的模型引用。
	 *
	 * @param rl 模型标识符（可为 null）
	 * @return 对应的 {@code ModelReference}，若不存在则返回 {@code null}
	 */
	public static ModelReference getModel(Identifier rl)
	{
		if(rl==null)
			return null;
		return models.get(rl);
	}

	/**
	 * 从全局缓存中查找指定路径字符串对应的模型引用。
	 * <p>
	 * 将路径字符串通过 {@link Beecrasy#rl(String)} 转换为标识符后查找。
	 *
	 * @param path 模型路径字符串（可为 null）
	 * @return 对应的 {@code ModelReference}，若不存在或路径为 null 则返回 {@code null}
	 */
	public static ModelReference getModel(String path)
	{
		if(path==null)
			return null;
		return getModel(Beecrasy.rl(path));
	}
	/**
	 * 向渲染收集器提交此模型引用的几何体。
	 * <p>
	 * 使用 {@link RenderTypes#solidMovingBlock()} 不透明渲染类型遍历所有 {@link BakedQuad}，
	 * 通过 {@link SubmitNodeCollector#submitCustomGeometry} 提交给渲染管线。
	 *
	 * @param poseStack          当前的变换矩阵栈
	 * @param submitNodeCollector 渲染节点收集器，用于提交几何数据
	 * @param quadInstance       四边形实例元数据，传递给每个 {@code BakedQuad}
	 */
	public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector,QuadInstance quadInstance) {
		submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.solidMovingBlock(), (pose,buffer)->{
			for(BakedQuad quad:get().getAll()) {
				buffer.putBakedQuad(pose, quad, quadInstance);
			}
		});
		
	}
	/**
	 * 从 Minecraft 的模型管理器获取此引用对应的模型数据。
	 * <p>
	 * 使用当前 {@code Minecraft} 实例的 {@code ModelManager}，
	 * 通过构造函数中保存的 {@link StandaloneModelKey} 查找并返回 {@link QuadCollection}。
	 *
	 * @return 模型管理器返回的 {@link QuadCollection}，包含所有 {@code BakedQuad}
	 */
	@SuppressWarnings("resource")
	@Override
	public QuadCollection get()
	{
		return Minecraft.getInstance().getModelManager().getStandaloneModel(name);
	}

}