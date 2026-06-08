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

package com.khjxiaogu.beecrasy.utils;

import java.util.Objects;

import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 通用配方处理器，管理配方进度、状态机流转与客户端/服务端数据同步。
 * <p>
 * 实现 {@link ContainerData} 接口，提供双数据槽供容器同步使用。
 * 支持配方切换、进度Tick、序列化读写等功能。
 *
 * @param <T> 配方类型
 */
public class RecipeHandler<T extends Recipe<?>> implements ContainerData{
	/** 当前处理进度。 */
	private int process;
	/** 最大处理进度（总处理时间）。 */
	private int processMax;
	/** 上次执行的配方ID。 */
	private Identifier lastRecipe;
	/** 是否已验证过配方。 */
	private boolean recipeTested=false;
	/** 配方执行回调。 */
	private RecipeProcessor doRecipe;
	/** 配方是否已完成。 */
	private boolean recipeFinished=false;
	/**
	 * 获取上次执行的配方ID。
	 *
	 * @return 配方ID
	 */
	public Identifier getLastRecipe() {
		return lastRecipe;
	}
	/**
	 * 构造一个配方处理器。
	 *
	 * @param doRecipe 配方执行回调
	 */
	public RecipeHandler(RecipeProcessor doRecipe) {
		this.doRecipe=doRecipe;
	}
	/**
	 * 当容器内容发生变化时调用，标记配方的测试状态为无效，
	 * 下次需要重新测试配方是否仍然有效。
	 */
	public void onContainerChanged() {
		//System.out.println("revalidate needed");
		recipeTested=false;
	}
	/**
	 * 判断是否需要重新测试配方。
	 *
	 * @return 如果需要重新测试则返回 {@code true}
	 */
	public boolean shouldTestRecipe() {
		return !recipeTested;
	}
	/**
	 * 判断配方是否已经完成。
	 *
	 * @return 如果配方已完成则返回 {@code true}
	 */
	public boolean isRecipeFinished() {
		return recipeFinished;
	}
	/**
	 * 设置要处理的配方。
	 * <p>
	 * 如果配方与上次不同则重置进度；如果配方为 {@code null} 则清空所有状态。
	 *
	 * @param recipe               新的配方持有人
	 * @param calculatedProcessTime 计算出的处理时间
	 * @return 如果配方状态发生实际变化则返回 {@code true}
	 */
	public boolean setRecipe(RecipeHolder<T> recipe,int calculatedProcessTime) {
		//System.out.println("revalidate return "+recipe);
		if (recipe!= null) {
			if(!recipe.id().identifier().equals(lastRecipe)) {
				process=processMax=calculatedProcessTime;
				lastRecipe=recipe.id().identifier();
				recipeFinished=false;
				return true;
			}
		}else {
			boolean ret=processMax>0;
			process=processMax=0;
			lastRecipe=null;
			recipeFinished=false;
			return ret;
		}
		recipeTested=true;
		return false;
	}
	/**
	 * 判断是否应执行tick处理（进度大于0）。
	 *
	 * @return 如果应执行tick则返回 {@code true}
	 */
	public boolean shouldTick() {
		return process>0;
	}
	/**
	 * 减少指定数量的处理进度。
	 * <p>
	 * 当进度归零时标记配方为完成状态，调用 {@link RecipeProcessor#run} 执行配方，
	 * 并根据返回的状态决定继续或重置。
	 *
	 * @param num 本次减少的进度值
	 * @return 如果配方仍在处理中（未完成）则返回 {@code true}
	 */
	public boolean tickProcess(int num) {
		if (process > 0) {
			process-=num;
			if(process<=0) {
				recipeFinished=true;
				RecipeHandleStatus status=doRecipe.run(lastRecipe);
				if(status.resetsProcess()) {
					process=processMax=0;
					lastRecipe=null;
					recipeTested=false;
					recipeFinished=false;
					return true;
				}else {
					process=10;
				}
			}
			return !recipeFinished;
		}
		return false;
	}
	/**
	 * 重置处理进度到最大值，取消完成状态。
	 */
	public void resetProgress() {
		process=processMax;
		recipeFinished=false;
	}
	/**
	 * 从NBT数据读取配方处理器的状态。
	 * <p>
	 * 在服务端还会额外读取上次配方ID。
	 *
	 * @param nbt      NBT输入
	 * @param isClient 是否为客户端
	 */
	public void readCustomNBT(ValueInput nbt, boolean isClient) {
		process=nbt.getIntOr("process",0);
		processMax=nbt.getIntOr("processMax",0);
		recipeFinished=nbt.getBooleanOr("recipeFinished", false);
		if (!isClient) {
			lastRecipe=nbt.getString("lastRecipe").map(Identifier::parse).orElse(null);
		}

	}
	/**
	 * 将配方处理器的状态写入NBT数据。
	 * <p>
	 * 在服务端还会额外写入上次配方ID。
	 *
	 * @param nbt      NBT输出
	 * @param isClient 是否为客户端
	 */
	public void writeCustomNBT(ValueOutput nbt, boolean isClient) {
		nbt.putInt("process",process);
		nbt.putInt("processMax",processMax);
		nbt.putBoolean("recipeFinished", recipeFinished);
		if (!isClient) {
			if(lastRecipe!=null)
				nbt.putString("lastRecipe", lastRecipe.toString());
		}

	}
	/**
	 * 获取当前处理进度。
	 *
	 * @return 当前进度
	 */
	public int getProcess() {
		return process;
	}
	/**
	 * 获取最大处理进度。
	 *
	 * @return 最大进度
	 */
	public int getProcessMax() {
		return processMax;
	}
	/**
	 * 获取已完成部分的进度。
	 * <p>
	 * 如果配方已完成后返回最大进度，否则返回（最大进度 - 当前进度）。
	 *
	 * @return 已完成进度
	 */
	public int getFinishedProgress() {
		if(recipeFinished)
			return processMax;
		return processMax-process;
	}
	/**
	 * 从指定索引获取 {@link ContainerData} 槽位的数据。
	 * <p>
	 * 索引0返回当前进度（完成后返回0），索引1返回最大进度。
	 *
	 * @param index 索引（0或1）
	 * @return 对应槽位的值，无效索引返回-1
	 */
	@Override
	public int get(int index) {
		Objects.checkIndex(index, 2);
		switch(index) {
		case 0:return recipeFinished?0:process;
		case 1:return processMax;
		}
		return -1;
	}
	/**
	 * 设置指定索引的 {@link ContainerData} 槽位数据。
	 * <p>
	 * 索引0设置当前进度，索引1设置最大进度。
	 *
	 * @param index 索引（0或1）
	 * @param value 要设置的值
	 */
	@Override
	public void set(int index, int value) {
		Objects.checkIndex(index, 2);
		switch(index) {
		case 0:process=value;return;
		case 1:processMax=value;return;
		}
	}
	/**
	 * 获取 {@link ContainerData} 的槽位数（始终为2）。
	 *
	 * @return 槽位数
	 */
	@Override
	public int getCount() {
		return 2;
	}

}
