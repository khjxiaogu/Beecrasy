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

@EventBusSubscriber(modid = Beecrasy.MODID, value = Dist.CLIENT)
public class TintColorCache {
	private static Reference2IntOpenHashMap<TextureAtlasSprite> colorMap=new Reference2IntOpenHashMap<>(1024);
	
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
	public static int getTintColor(TextureAtlasSprite id) {
		return colorMap.getOrDefault(id, 0xffffffff);
	}
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
