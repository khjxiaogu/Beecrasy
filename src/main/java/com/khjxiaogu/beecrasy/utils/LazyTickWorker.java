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

import java.util.function.Supplier;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 惰性Tick工作器，每隔指定tick数执行一次任务。
 * <p>
 * 支持序列化读写与动态调整执行间隔。当tick计数达到阈值时触发工作函数，
 * 若工作函数返回 {@code true} 则可表示工作已完成。
 */
public class LazyTickWorker {
	/** 最大tick间隔（0表示停止）。 */
	public int tMax;
	/** 当前已计数的tick数。 */
	public int tCur=0;
	/** 是否为固定最大间隔（若为固定值则序列化时不保存tMax）。 */
	private boolean isStaticMax;
	/** 工作函数，返回 {@code true} 表示工作完成。 */
	public Supplier<Boolean> work;
	/**
	 * 构造一个固定间隔的惰性Tick工作器。
	 *
	 * @param tMax 固定tick间隔
	 * @param work 工作函数
	 */
	public LazyTickWorker(int tMax, Supplier<Boolean> work) {
		super();
		this.tMax = tMax;
		this.work = work;
		isStaticMax=true;
	}
	/**
	 * 构造一个动态间隔的惰性Tick工作器（间隔可由序列化实时调整）。
	 *
	 * @param work 工作函数
	 */
	public LazyTickWorker(Supplier<Boolean> work) {
		super();
		this.work = work;
		isStaticMax=false;
	}
	/**
	 * 执行一次tick计数，当达到最大间隔时触发工作函数。
	 *
	 * @return 如果触发了工作函数则返回其执行结果，否则返回 {@code false}
	 */
	public boolean tick() {
		if(tMax!=0) {
			tCur++;
			if(tCur>=tMax) {
				tCur=0;
				return work.get();
			}
		}
		return false;
	}
	/**
	 * 判断工作器是否正在运行（tMax不为0）。
	 *
	 * @return 如果正在运行则返回 {@code true}
	 */
	public boolean isRunning() {
		return tMax!=0;
	}
	/**
	 * 重置当前tick计数为0。
	 */
	public void rewind() {
		tCur=0;
	}
	/**
	 * 将当前tick计数倒回至（tMax - num）。
	 *
	 * @param num 从间隔终点回退的tick数
	 */
	public void rewind(int num) {
		tCur=tMax-num;
	}
	/**
	 * 将当前tick计数设置到tMax，使得下一次tick就能触发工作。
	 */
	public void enqueue() {
		tCur=tMax;
	}
	/**
	 * 启动工作器并设置新的间隔。
	 *
	 * @param time 新的tick间隔
	 */
	public void start(int time) {
		tCur=0;
		tMax=time;
	}
	/**
	 * 停止工作器（将tMax设为0）。
	 */
	public void stop() {
		tMax=0;
	}
	/**
	 * 从NBT数据中读取工作器状态（默认键名）。
	 *
	 * @param cnbt NBT输入
	 */
	public void read(ValueInput cnbt) {
		if(!isStaticMax)
			tMax=cnbt.getIntOr("max",0);
		tCur=cnbt.getIntOr("cur",0);
	}
	/**
	 * 从NBT数据中读取工作器状态（自定义前缀键名）。
	 *
	 * @param cnbt NBT输入
	 * @param key  键名前缀
	 */
	public void read(ValueInput cnbt,String key) {
		if(!isStaticMax)
			tMax=cnbt.getIntOr(key+"Max",0);
		tCur=cnbt.getIntOr(key,0);
	}
	/**
	 * 将工作器状态写入NBT数据（默认键名）。
	 *
	 * @param cnbt NBT输出
	 */
	public void write(ValueOutput cnbt) {
		if(!isStaticMax)
			cnbt.putInt("max", tMax);
		cnbt.putInt("cur",tCur);
	}
	/**
	 * 将工作器状态写入NBT数据（自定义前缀键名）。
	 *
	 * @param cnbt NBT输出
	 * @param key  键名前缀
	 */
	public void write(ValueOutput cnbt,String key) {
		if(!isStaticMax)
			cnbt.putInt(key+"Max", tMax);
		cnbt.putInt(key,tCur);
	}
}
