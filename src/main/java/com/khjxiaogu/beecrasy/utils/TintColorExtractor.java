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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntUnaryOperator;

import javax.annotation.Nullable;

import net.minecraft.util.ARGB;



public class TintColorExtractor {
	// 饱和度容忍区间，高于SAT_HIGH的视为最佳饱和度，低于SAT_LOW的视为最差饱和度。
	private static final double SAT_LOW = 0.08;
	private static final double SAT_HIGH = 0.25;
	// 最佳视觉效果明度区间，不在这个区间内会视为最差明度
	private static final double LIGHTNESS_MIN = 0.12;
	private static final double LIGHTNESS_MAX = 0.92;
	// 最大图片大小，大于这个尺寸会进行采样
	private static final int MAX_IMAGE_SIZE = 32;

	public static record Color(int r, int g, int b) {
		public int toARGB32() {
			return ARGB.color(0xFF, r, g, b);
		}
	}

	public static class WeightedColor {
		Color color;
		int weight;

		WeightedColor(Color color, int totalWeight) {
			super();
			this.color = color;
			this.weight = totalWeight;
		}
	}

	/**
	 * 颜色簇类。
	 * <p>
	 * 包含一组带权重的颜色及其总权重。
	 */
	public static class Bucket {
		List<WeightedColor> colors;
		int totalWeight;

		Bucket(List<WeightedColor> colors, int totalWeight) {
			this.colors = colors;
			this.totalWeight = totalWeight;
		}
	}

	private static final Comparator<WeightedColor> COMPARING_R = Comparator.comparingInt(c -> c.color.r);
	private static final Comparator<WeightedColor> COMPARING_G = Comparator.comparingInt(c -> c.color.g);
	private static final Comparator<WeightedColor> COMPARING_B = Comparator.comparingInt(c -> c.color.b);

	/**
	 * 从图片像素数组中采样指定矩形区域的像素，并进行尺寸自适应缩放。
	 * <p>
	 * 如果目标采样区域（rectW × rectH）的尺寸小于等于{@link #MAX_IMAGE_SIZE}，
	 * 则直接复制该区域像素并转换为ARGB格式；否则对区域进行降采样，使结果像素总数
	 * 不超过{@code MAX_IMAGE_SIZE × MAX_IMAGE_SIZE}，并同时完成ABGR到ARGB的转换。
	 *
	 * @param pixels 原始像素数组（ABGR格式）
	 * @param width  原图宽度
	 * @param height 原图高度
	 * @param x      采样区域左上角X坐标
	 * @param y      采样区域左上角Y坐标
	 * @param rectW  采样区域宽度
	 * @param rectH  采样区域高度
	 * @return ARGB格式的采样像素数组
	 */
	public static int[] samplePixelsABGR(int[] pixels, int width, int height, int x, int y,
		int rectW, int rectH) {

		return samplePixels(pixels, width, height, x, y, rectW, rectH, ARGB::fromABGR);
	}
	/**
	 * 从图片像素数组中采样指定矩形区域的像素，并进行尺寸自适应缩放。
	 * <p>
	 * 如果目标采样区域（rectW × rectH）的尺寸小于等于{@link #MAX_IMAGE_SIZE}，
	 * 则直接复制该区域像素并转换为ARGB格式；否则对区域进行降采样，使结果像素总数
	 * 不超过{@code MAX_IMAGE_SIZE × MAX_IMAGE_SIZE}
	 *
	 * @param pixels 原始像素数组（ARGB格式）
	 * @param width  原图宽度
	 * @param height 原图高度
	 * @param x      采样区域左上角X坐标
	 * @param y      采样区域左上角Y坐标
	 * @param rectW  采样区域宽度
	 * @param rectH  采样区域高度
	 * @return ARGB格式的采样像素数组
	 */
	public static int[] samplePixelsARGB(int[] pixels, int width, int height, int x, int y,
		int rectW, int rectH) {

		return samplePixels(pixels, width, height, x, y, rectW, rectH, IntUnaryOperator.identity());
	}
	/**
	 * 从图片像素数组中采样指定矩形区域的像素，并进行尺寸自适应缩放。
	 * <p>
	 * 如果目标采样区域（rectW × rectH）的尺寸小于等于{@link #MAX_IMAGE_SIZE}，
	 * 则直接复制该区域像素并转换为ARGB格式；否则对区域进行降采样，使结果像素总数
	 * 不超过{@code MAX_IMAGE_SIZE × MAX_IMAGE_SIZE}，并同时完成指定的像素格式转换。
	 *
	 * @param pixels   原始像素数组
	 * @param width    原图宽度
	 * @param height   原图高度
	 * @param x        采样区域左上角X坐标
	 * @param y        采样区域左上角Y坐标
	 * @param rectW    采样区域宽度
	 * @param rectH    采样区域高度
	 * @param colorMap 像素格式转换函数
	 * @return 采样像素数组
	 */
	public static int[] samplePixels(int[] pixels, int width, int height, int x, int y,
		int rectW, int rectH, IntUnaryOperator colorMap) {
		if (rectW <= MAX_IMAGE_SIZE && rectH <= MAX_IMAGE_SIZE) {
			int[] result = new int[rectW * rectH];
			for (int r = 0; r < rectH; r++) {
				int srcRow = y + r;
				int srcIndexStart = srcRow * width + x;
				int destIndexStart = r * rectW;
				for (int w = 0; w < rectW; w++) {
					result[destIndexStart + w] = colorMap.applyAsInt(pixels[srcIndexStart + w]);
				}
			}
			return result;
		}
		int[] result = new int[MAX_IMAGE_SIZE * MAX_IMAGE_SIZE];
		for (int r = 0; r < MAX_IMAGE_SIZE; r++) {
			int srcAbsY = y + r * rectH / MAX_IMAGE_SIZE;
			int destIndexStart = r * MAX_IMAGE_SIZE;
			int srcIndexStart = srcAbsY * width + x;
			for (int c = 0; c < MAX_IMAGE_SIZE; c++) {
				result[destIndexStart + c] = colorMap.applyAsInt(pixels[srcIndexStart + c * rectW / MAX_IMAGE_SIZE]);
			}
		}
		return result;
	}

	/**
	 * 对像素数组进行颜色量化与分布统计。
	 * <p>
	 * 将每个像素的RGB分量进行9bit量化，每个分量3bit，合并成一个索引，
	 * 统计每个量化颜色出现的次数。然后根据索引还原近似的RGB值，构建带权重的颜色列表， 封装为{@link Bucket}对象返回。
	 *
	 * @param pixels ARGB32格式的像素数组（Alpha为0的像素将被忽略）
	 * @return 包含颜色分布统计的{@code Bucket}对象；如果没有有效像素则返回{@code null}
	 */
	@Nullable
	public static Bucket processImage(int[] pixels) {
		int[] colorCounts = new int[512];
		int totalPixels = 0;

		for (int i = 0; i < pixels.length; i++) {
			int pixel = pixels[i];
			if (((pixel >> 24) & 0xFF) == 0)
				continue;
			int r4 = ((pixel >> 16) & 0xFF) >> 5;
			int g4 = ((pixel >> 8) & 0xFF) >> 5;
			int b4 = (pixel & 0xFF) >> 5;
			colorCounts[(r4 << 6) | (g4 << 3) | b4]++;
			totalPixels++;
		}
		if (totalPixels == 0)
			return null;
		List<WeightedColor> weightedList = new ArrayList<>(512);
		for (int idx = 0; idx < colorCounts.length; idx++) {
			int count = colorCounts[idx];
			if (count == 0)
				continue;
			int r4 = (idx >> 6) & 0x7;
			int g4 = (idx >> 3) & 0x7;
			int b4 = idx & 0x7;
			int r = (r4 << 5) | (r4 << 2);
			int g = (g4 << 5) | (g4 << 2);
			int b = (b4 << 5) | (b4 << 2);
			weightedList.add(new WeightedColor(new Color(r, g, b), count));
		}

		return new Bucket(weightedList, totalPixels);
	}

	/**
	 * 从颜色分布桶中提取图片的主色调。
	 * <p>
	 * 通过中位切分算法将颜色空间划分为{@code quantColors}个桶，计算每个桶的加权平均色，
	 * 然后利用{@link #evaluateColor}函数对每个候选色进行评分，选择得分最高的颜色作为主色调。
	 * <p>
	 * 若评分后没有候选颜色（理论上不会发生），则按权重降序选择第一个颜色作为兜底。
	 *
	 * @param result      由{@link #processImage}得到的颜色分布桶
	 * @param quantColors 期望的量化颜色数（即切分后的桶数量）
	 * @return 主色调的ARGB32格式整数值（Alpha为255）
	 */
	public static int extractTintColor(Bucket result, int quantColors) {
		if (result.totalWeight == 0) {
			return 0xFFFFFFFF;
		}
		Set<Bucket> buckets = medianCut(result, quantColors);
		List<WeightedColor> candidates = new ArrayList<>();
		for (Bucket bucket : buckets) {
			if (bucket.totalWeight == 0)
				continue;

			long sumR = 0, sumG = 0, sumB = 0;
			for (WeightedColor cw : bucket.colors) {
				sumR += cw.color.r * cw.weight;
				sumG += cw.color.g * cw.weight;
				sumB += cw.color.b * cw.weight;
			}
			int avgR = (int) (sumR / bucket.totalWeight);
			int avgG = (int) (sumG / bucket.totalWeight);
			int avgB = (int) (sumB / bucket.totalWeight);
			candidates.add(new WeightedColor(new Color(avgR, avgG, avgB), bucket.totalWeight));
		}
		double bestScore = -Double.MAX_VALUE;
		Color bestColor = null;

		for (WeightedColor cand : candidates) {
			double score = evaluateColor(cand.color, cand.weight, result.totalWeight);
			if (score > bestScore) {
				bestScore = score;
				bestColor = cand.color;
			}
		}
		if (bestColor == null && !candidates.isEmpty()) {
			candidates.sort((a, b) -> Double.compare(b.weight, a.weight));
			bestColor = candidates.get(0).color;
		}

		return bestColor.toARGB32();
	}

	/**
	 * 中位切分算法实现。
	 * <p>
	 * 递归地将给定的颜色桶按颜色范围最大的一维进行切分，直到桶数量达到{@code targetBuckets}。
	 * 切分点的选择依据所有颜色的权重和的中位数，将桶分裂为两个子桶。
	 *
	 * @param initalbucket  初始颜色桶（通常为整个图片的颜色分布）
	 * @param targetBuckets 目标桶数量
	 * @return 切分后的桶集合
	 */
	private static Set<Bucket> medianCut(Bucket initalbucket, int targetBuckets) {
		Set<Bucket> buckets = new HashSet<>(targetBuckets);
		buckets.add(initalbucket);

		while (buckets.size() < targetBuckets) {
			double maxRange = -1;
			Bucket bestBucket = null;
			Comparator<WeightedColor> splitDim = COMPARING_R;

			for (Bucket bucket : buckets) {
				if (bucket.colors.size() < 2)
					continue;
				int minR = Integer.MAX_VALUE, maxR = Integer.MIN_VALUE;
				int minG = Integer.MAX_VALUE, maxG = Integer.MIN_VALUE;
				int minB = Integer.MAX_VALUE, maxB = Integer.MIN_VALUE;
				for (WeightedColor c : bucket.colors) {
					Color clr = c.color;
					if (clr.r < minR)
						minR = clr.r;
					if (clr.r > maxR)
						maxR = clr.r;
					if (clr.g < minG)
						minG = clr.g;
					if (clr.g > maxG)
						maxG = clr.g;
					if (clr.b < minB)
						minB = clr.b;
					if (clr.b > maxB)
						maxB = clr.b;
				}
				int rangeR = maxR - minR;
				int rangeG = maxG - minG;
				int rangeB = maxB - minB;
				Comparator<WeightedColor> currDim;
				int currentMax;
				if (rangeR > rangeG && rangeR > rangeB) {
					currentMax = rangeR;
					currDim = COMPARING_R;
				} else if (rangeG >= rangeB) {
					currentMax = rangeG;
					currDim = COMPARING_G;
				} else {
					currentMax = rangeB;
					currDim = COMPARING_B;
				}
				if (currentMax > maxRange) {
					maxRange = currentMax;
					bestBucket = bucket;
					splitDim = currDim;
				}
			}

			if (bestBucket == null)
				break;
			bestBucket.colors.sort(splitDim);

			double halfWeight = bestBucket.totalWeight / 2.0;
			int acc = 0;
			List<WeightedColor> part1 = new ArrayList<>(bestBucket.colors.size());
			List<WeightedColor> part2 = new ArrayList<>(bestBucket.colors.size());
			int weight1 = 0;
			int weight2 = 0;
			for (WeightedColor clr : bestBucket.colors) {
				acc += clr.weight;
				if (acc >= halfWeight) {
					part2.add(clr);
					weight2 += clr.weight;
				} else {
					part1.add(clr);
					weight1 += clr.weight;
				}
			}
			buckets.remove(bestBucket);
			buckets.add(new Bucket(part1, weight1));
			buckets.add(new Bucket(part2, weight2));
		}

		return buckets;
	}

	/**
	 * 评估一个候选颜色作为主色调的得分。
	 * <p>
	 * 综合颜色的饱和度、明度以及在整体颜色中的权重占比进行评分。
	 * <ul>
	 * <li>饱和度分数：低于{@link #SAT_LOW}得0，高于{@link #SAT_HIGH}得1，中间线性插值；</li>
	 * <li>明度分数：不在{@link #LIGHTNESS_MIN}～{@link #LIGHTNESS_MAX}区间内得0，
	 * 否则按抛物线函数计算（中心0.5处得分最高为1）；</li>
	 * <li>权重占比：当前桶权重与总权重的比值。</li>
	 * </ul>
	 * 最终得分为三者乘积。
	 *
	 * @param color        待评估的颜色
	 * @param bucketWeight 该颜色所在桶的权重（如像素总数）
	 * @param totalWeight  整图所有颜色的总权重
	 * @return 评估得分（0～1之间）
	 */
	private static double evaluateColor(Color color, double bucketWeight, double totalWeight) {
		// 取得饱和度和亮度
		double rn = color.r / 255.0;
		double gn = color.g / 255.0;
		double bn = color.b / 255.0;
		double maxc = Math.max(rn, Math.max(gn, bn));
		double minc = Math.min(rn, Math.min(gn, bn));
		double delta = maxc - minc;
		double l = (maxc + minc) * 0.5;
		double s = 0;
		if (delta != 0) {
			s = delta / (1 - Math.abs(2 * l - 1));
		}
		// 饱和度平滑因子
		double fSat;
		if (s <= SAT_LOW) {
			fSat = 0.0;
		} else if (s >= SAT_HIGH) {
			fSat = 1.0;
		} else {
			fSat = (s - SAT_LOW) / (SAT_HIGH - SAT_LOW);
		}

		// 明度因子
		double fLight;
		if (l < LIGHTNESS_MIN || l > LIGHTNESS_MAX) {
			fLight = 0.0;
		} else {
			fLight = 1.0 - 4.0 * Math.pow(l - 0.5, 2);
		}

		double weightRatio = bucketWeight / totalWeight;
		return weightRatio * fSat * fLight;
	}

}