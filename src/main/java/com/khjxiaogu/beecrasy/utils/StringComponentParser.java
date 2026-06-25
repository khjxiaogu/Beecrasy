

package com.khjxiaogu.beecrasy.utils;




import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;


public class StringComponentParser {
	public static final Int2ObjectOpenHashMap<ChatFormatting> LEGACY_FORMAT_CODE_MAP = new Int2ObjectOpenHashMap<>();

	static {
		LEGACY_FORMAT_CODE_MAP.put('0', ChatFormatting.BLACK);
		LEGACY_FORMAT_CODE_MAP.put('1', ChatFormatting.DARK_BLUE);
		LEGACY_FORMAT_CODE_MAP.put('2', ChatFormatting.DARK_GREEN);
		LEGACY_FORMAT_CODE_MAP.put('3', ChatFormatting.DARK_AQUA);
		LEGACY_FORMAT_CODE_MAP.put('4', ChatFormatting.DARK_RED);
		LEGACY_FORMAT_CODE_MAP.put('5', ChatFormatting.DARK_PURPLE);
		LEGACY_FORMAT_CODE_MAP.put('6', ChatFormatting.GOLD);
		LEGACY_FORMAT_CODE_MAP.put('7', ChatFormatting.GRAY);
		LEGACY_FORMAT_CODE_MAP.put('8', ChatFormatting.DARK_GRAY);
		LEGACY_FORMAT_CODE_MAP.put('9', ChatFormatting.BLUE);
		LEGACY_FORMAT_CODE_MAP.put('a', ChatFormatting.GREEN);
		LEGACY_FORMAT_CODE_MAP.put('b', ChatFormatting.AQUA);
		LEGACY_FORMAT_CODE_MAP.put('c', ChatFormatting.RED);
		LEGACY_FORMAT_CODE_MAP.put('d', ChatFormatting.LIGHT_PURPLE);
		LEGACY_FORMAT_CODE_MAP.put('e', ChatFormatting.YELLOW);
		LEGACY_FORMAT_CODE_MAP.put('f', ChatFormatting.WHITE);
		LEGACY_FORMAT_CODE_MAP.put('k', ChatFormatting.OBFUSCATED);
		LEGACY_FORMAT_CODE_MAP.put('l', ChatFormatting.BOLD);
		LEGACY_FORMAT_CODE_MAP.put('m', ChatFormatting.STRIKETHROUGH);
		LEGACY_FORMAT_CODE_MAP.put('n', ChatFormatting.UNDERLINE);
		LEGACY_FORMAT_CODE_MAP.put('o', ChatFormatting.ITALIC);
		LEGACY_FORMAT_CODE_MAP.put('r', ChatFormatting.RESET);
	}
	private StringComponentParser() {
		
	}
	public static MutableComponent parse(String str) {

		MutableComponent mc=Component.literal("");
		int escapedChar=0;
		boolean escaped=false;
		boolean slashEscaped=false;
		Style style=Style.EMPTY;
		Reader reader=new StringReader(str);
		if(!reader.markSupported()) {
			reader=new BufferedReader(reader);
		}
		int ch;
		StringBuilder current=new StringBuilder();
		try {
		while((ch=reader.read())!=-1) {
			if(slashEscaped) {
				slashEscaped=false;
				current.appendCodePoint(ch);
				continue;
			}
			if(escaped) {
				escaped=false;
				if(ch=='&'||ch=='\u00a7') {
					current.appendCodePoint(ch);
					continue;
				}else if(ch=='#') {
					StringBuilder colorCode=new StringBuilder();
					reader.mark(32);
					for(int i=0;i<6;i++) {
						int cch;
						if((cch=reader.read())==-1)
							break;
						if(i==2)
							reader.mark(32);
						
						if((cch>='0'&&cch<='9')||(cch>='a'&&cch<='f')||(cch>='A'&&cch<='F'))
							colorCode.appendCodePoint(cch);
						else
							break;
					}
					TextColor tc=null;
					if(colorCode.length()==6) {
						tc=TextColor.parseColor("#"+colorCode).resultOrPartial().orElse(null);
						if(tc!=null) {
							if(!current.isEmpty()) {
								mc.append(Component.literal(current.toString()).withStyle(style));
								current=new StringBuilder();
							}
							style=style.withColor(tc);
						}
					}else if(colorCode.length()>=3) {
						int r=Integer.parseInt(colorCode, 0, 1, 16);
						int g=Integer.parseInt(colorCode, 1, 2, 16);
						int b=Integer.parseInt(colorCode, 2, 3, 16);
						
						tc=TextColor.fromRgb(ARGB.color(0x0, r*0x11, g*0x11, b*0x11));
						reader.reset();
						if(!current.isEmpty()) {
							mc.append(Component.literal(current.toString()).withStyle(style));
							current=new StringBuilder();
						}
						style=style.withColor(tc);
					}else {
						current.appendCodePoint(escapedChar);
						current.appendCodePoint(ch);
						reader.reset();
					}
					continue;
				}else {
					ChatFormatting format=LEGACY_FORMAT_CODE_MAP.get(ch);
					if(format!=null) {
						if(!current.isEmpty()) {
							mc.append(Component.literal(current.toString()).withStyle(style));
							current=new StringBuilder();
						}
						style=style.applyFormat(format);
						continue;
					}
				}
				current.appendCodePoint(escapedChar);
			}
			switch(ch) {
			case '\\':
				slashEscaped=true;break;
			case '&':
			case '\u00a7':
				escaped=true;
				escapedChar=ch;break;
			case '{':
				reader.mark(64);
				StringBuilder sb=new StringBuilder();
				int cch;
				while((cch=reader.read())!=-1&&cch!='}') {
					sb.appendCodePoint(cch);
				}
				String content = sb.toString();
				mc.append(Component.translatable(content).withStyle(style));
				break;
				default:
					current.appendCodePoint(ch);break;
			}
		}
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		if(escaped) {
			current.appendCodePoint(escapedChar);
		}
		if(!current.isEmpty()) {
			mc.append(Component.literal(current.toString()).withStyle(style));
		}
		return mc;
	}/*
	public static void main(String[] args) {
		System.out.println(parse("砧木&b&l&测试&r得到&#aaa测试&&123\\\\2333"));
	}*/
}
