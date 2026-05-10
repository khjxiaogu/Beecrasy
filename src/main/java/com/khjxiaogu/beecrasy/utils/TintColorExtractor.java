package com.khjxiaogu.beecrasy.utils;

import javax.imageio.ImageIO;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.util.ARGB;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 为贴图产生一个具有代表性的颜色tint
 * 
 */

public class TintColorExtractor {
	// 最佳视觉效果饱和度区间
	private static final double SAT_LOW = 0.08;
	private static final double SAT_HIGH = 0.25;
	// 最佳视觉效果明度区间
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

	public static int[] samplePixels(int[] pixels, int width, int height, int x, int y,
			int rectW, int rectH) {
		if (rectW <= MAX_IMAGE_SIZE && rectH <= MAX_IMAGE_SIZE) {
			int[] result = new int[rectW * rectH];
			for (int r = 0; r < rectH; r++) {
				int srcRow = y + r;
				int srcIndexStart = srcRow * width + x;
				int destIndexStart=r * rectW;
				for(int w=0;w<rectW;w++) {
					result[destIndexStart+w]=ARGB.fromABGR(pixels[srcIndexStart+w]);
				}
			}
			return result;
		}
		int[] result = new int[MAX_IMAGE_SIZE * MAX_IMAGE_SIZE];
		for (int r = 0; r < MAX_IMAGE_SIZE; r++) {
			int srcAbsY = y + r * rectH / MAX_IMAGE_SIZE;
			int destIndexStart=r * MAX_IMAGE_SIZE;
			int srcIndexStart= srcAbsY * width+x;
			for (int c = 0; c < MAX_IMAGE_SIZE; c++) {
				result[destIndexStart+c] = ARGB.fromABGR(pixels[srcIndexStart + c * rectW / MAX_IMAGE_SIZE]);
			}
		}
		return result;
	}
	/**
	 * 图像预处理：把颜色进行9bit量化并输出图片颜色分布
	 * 
	 * @param width  像素宽度
	 * @param height 像素高度
	 * @param pixels ARGB32格式的像素数组，扫描方式先行再列
	 * @return {@link Bucket}图片颜色分布
	 */
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
	 * 提取图片的代表色
	 * 
	 * @param result      图片颜色分布
	 * @param quantColors 量化颜色数，决定了从图片选中多少种颜色进行最终选择
	 * @return ARGB32颜色
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

	private static Set<Bucket> medianCut(Bucket initalbucket, int targetBuckets) {
		Set<Bucket> buckets = new HashSet<>(8);
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

	private static class ImageResult {
		BufferedImage bi;
		int width;
		int height;
		int[] pixels;
		int color;

		ImageResult(BufferedImage bi, int color) {
			super();
			this.bi = bi;
			this.color = color;
			width = bi.getWidth();
			height = bi.getHeight();
			pixels = bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), null, 0, bi.getWidth());
		}

	}

	public static void main(String[] args) throws IOException {
		File input = new File("item");
		File[] files = input.listFiles();

		BufferedImage image = new BufferedImage(34, files.length * 18, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graph = image.createGraphics();
		int i = 0;
		List<ImageResult> ir = new ArrayList<>();
		for (File f : files) {
			BufferedImage bi = ImageIO.read(f);
			ir.add(new ImageResult(bi, 0));
		}
		int countProc = 0;
		ImageResult fc = ir.get(0);
		Bucket procx = processImage(fc.pixels);
		extractTintColor(procx, 12);
		long start = System.nanoTime();
		for (int o = 0; o < 100; o++) {
			for (ImageResult f : ir) {

				Bucket proc = processImage(f.pixels);
				f.color = 0;
				f.color = extractTintColor(proc, 8);

				countProc++;
			}
		}
		long end = System.nanoTime();
		System.out.println("total cost=" + (end - start) + ",count=" + countProc + ",avg=" + (end - start) / countProc);
		for (ImageResult f : ir) {
			BufferedImage bi = f.bi;
			graph.drawImage(bi, 0, i * 18, 16, 16, null);
			graph.setColor(new java.awt.Color(f.color));
			graph.fillRect(18, i * 18, 16, 16);
			i++;

		}
		ImageIO.write(image, "png", new File("testImg.png"));
	}
}