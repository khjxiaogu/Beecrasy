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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.utils.TintColorExtractor;
import com.khjxiaogu.beecrasy.utils.TintColorExtractor.Bucket;
import com.mojang.blaze3d.platform.NativeImage;

import it.unimi.dsi.fastutil.objects.Reference2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;

/**
 * 纹理染色颜色缓存类。
 * <p>
 * 监听 {@link TextureAtlasStitchedEvent} 事件，在纹理图集拼接完成时对每个纹理分析
 * 并提取主色调，缓存到 {@link Reference2IntOpenHashMap} 中。提供 {@link #getTintColor}
 * 方法供染色逻辑快速查询。同时提供 {@link #dumpTintColor} 调试方法将缓存数据持久化为
 * PNG 图片。
 * <p>
 * 注册为 NeoForge 客户端事件总线订阅者（{@link EventBusSubscriber}），自动接收
 * 纹理图集拼接事件。
 */
@EventBusSubscriber(modid = Beecrasy.MODID, value = Dist.CLIENT)
public class TintColorCache {
	/**
	 * 纹理到 ARGB 整数的快速引用映射。
	 * 初始容量为 1024，使用引用相等性（而非 {@code equals}）比较键，
	 * 适用于纹理精灵引用在生命周期内不会发生变化的场景。
	 */
	private static Reference2IntOpenHashMap<TextureAtlasSprite> colorMap=new Reference2IntOpenHashMap<>(1024);
	
	/**
	 * 纹理图集拼接事件处理器。
	 * <p>
	 * 当纹理图集拼接完成后触发。遍历图集中所有纹理（跳过缺失纹理），对每个 RGBA 格式的
	 * 纹理使用 {@link TintColorExtractor#processImage} 和
	 * {@link TintColorExtractor#extractTintColor} 分析并提取主色调，
	 * 将结果存入 {@link #colorMap} 缓存。
	 *
	 * @param ev 纹理图集拼接事件
	 */
	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void onTextureStitched(TextureAtlasStitchedEvent ev) {

    	TextureAtlasSprite missingSprite=ev.getAtlas().missingSprite();
		for(TextureAtlasSprite text:ev.getAtlas().getTextures().values()) {
			
			SpriteContents content=text.contents();
			if(missingSprite==text)
				continue;
			NativeImage ni=content.getOriginalImage();
			if(ni.format()!=NativeImage.Format.RGBA)
				continue;
			Bucket processResult=TintColorExtractor.processImage(TintColorExtractor.samplePixelsABGR(ni.getPixelsABGR(),ni.getWidth(),ni.getHeight(),0,0,ni.getWidth(),ni.getHeight()));
			if(processResult==null)
				continue;
			int tintColor=TintColorExtractor.extractTintColor(processResult, 8);

			colorMap.put(text, tintColor);
		}

	}
	/**
	 * 查询缓存中的染色颜色。
	 * <p>
	 * 在 {@link #colorMap} 中查找指定纹理精灵的缓存颜色值。
	 * 若未找到则返回默认的白色（{@code 0xffffffff}）。
	 *
	 * @param id 纹理精灵
	 * @return 缓存的 ARGB 颜色值，未缓存时返回白色
	 */
	public static int getTintColor(TextureAtlasSprite id) {
		return colorMap.getOrDefault(id, 0xffffffff);
	}
	/**
	 * 调试工具——将所有缓存的纹理及其提取的颜色渲染为一整张图片并保存为 PNG。
	 * <p>
	 * 生成一张网格状图片：每行最多 40 项，每项左侧为原始纹理（16x16）、右侧为提取的
	 * 染色颜色色块（16x16）。输出文件位于 {@code logs/tintColorDump.png}。
	 * 仅用于调试目的，在生产环境中应避免调用。
	 */
	@SuppressWarnings("resource")
	public static void dumpTintColor() {

    	try {
    		int columns = 40;
    		int total = colorMap.size();
    		int rows = (total + columns - 1) / columns;

    		int imageWidth = columns * 36;
    		int imageHeight = rows * 18;

    		BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
    		Graphics2D graph = image.createGraphics();

    		int index = 0;
    		for (Entry<TextureAtlasSprite> ent : colorMap.reference2IntEntrySet()) {
    		    int row = index / columns;
    		    int col = index % columns;
    		    int x = col * 36;
    		    int y = row * 18;

    		    NativeImage ni = ent.getKey().contents().getOriginalImage();
    		    BufferedImage bi = new BufferedImage(ni.getWidth(), ni.getHeight(), BufferedImage.TYPE_INT_ARGB);
    		    int[] pixels = ni.getPixels();
    		    for (int px = 0; px < ni.getWidth(); px++) {
    		        for (int py = 0; py < ni.getHeight(); py++) {
    		            bi.setRGB(px, py, pixels[px + py * bi.getWidth()]);
    		        }
    		    }
    		    graph.drawImage(bi, x, y, 16, 16, null);
    		    graph.setColor(new java.awt.Color(ent.getIntValue()));
    		    graph.fillRect(x + 18, y, 16, 16);

    		    index++;
    		}
    		ImageIO.write(image, "png", new File("logs","tintColorDump.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
