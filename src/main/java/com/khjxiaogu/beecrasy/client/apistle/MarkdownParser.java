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

package com.khjxiaogu.beecrasy.client.apistle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;

import com.khjxiaogu.beecrasy.client.apistle.PageBuilder.AutoTableBuilder;
import com.khjxiaogu.beecrasy.client.apistle.lines.Image;
import com.khjxiaogu.beecrasy.client.apistle.lines.Space;
import com.khjxiaogu.beecrasy.client.apistle.lines.Split;
import com.khjxiaogu.beecrasy.client.apistle.lines.Text;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * 一个零依赖的 Markdown 转 Page 解析器，将 Markdown 字符串通过流式 {@link PageBuilder} API 转换为 {@link Page}。
 *
 * <p>支持的 Markdown 特性：</p>
 * <ul>
 *   <li>标题（# 到 ####）—— h1/h2 居中，h3/h4 左对齐</li>
 *   <li>水平分隔线（---、***、___）</li>
 *   <li>无序列表（- / *）—— 以项目符号前缀</li>
 *   <li>有序列表（1. / 2. …）</li>
 *   <li>引用块（&gt;）—— 以两个空格前缀</li>
 *   <li>表格（| 列 | 列 |）—— 自动宽度，支持物品单元格</li>
 *   <li>围栏代码块（```）—— 以较小比例渲染</li>
 *   <li>图片（{@code ![alt](ns:path)} 或 {@code ![alt](ns:path WxH)}）</li>
 *   <li>物品表达式（整行或表格单元格内）：
 *       {@code [modid:item]} → 单个物品，
 *       {@code [#modid:tag]} → 物品标签，
 *       {@code [modid:a, modid:b]} → 多个物品</li>
 *   <li>段落合并 —— 连续的文本行以换行符连接</li>
 *   <li>内联 Markdown 格式转换为 &amp;-码：
 *       <b>粗体</b>（** → &amp;l），<i>斜体</i>（* → &amp;o），
 *       <del>删除线</del>（~~ → &amp;m），{@code 代码}（去除反引号）</li>
 *   <li>&amp;-格式码直接传递给
 *       {@link com.khjxiaogu.beecrasy.utils.StringComponentParser}
 *       （支持 &amp;l、&amp;m、&amp;n、&amp;o、&amp;r、&amp;#rrggbb 等）</li>
 * </ul>
 */
public final class MarkdownParser {

	private static final Pattern IMAGE_LINE = Pattern.compile(
			"!\\[.*?\\]\\(([a-z0-9_.-]+:[a-z0-9_./-]+)(?:\\s+(\\d+)x(\\d+))?\\)");

	/** ASCII 字符的估算像素宽度。 */
	private static final int ASCII_CHAR_W = 6;
	/** 非 ASCII（如中日韩）字符的估算像素宽度。 */
	private static final int NON_ASCII_CHAR_W = 8;

	private final HolderLookup.Provider registries;

	/**
	 * 创建一个 Markdown 解析器。
	 *
	 * @param registries Holder 查找提供器
	 */
	public MarkdownParser(HolderLookup.Provider registries) {
		this.registries = registries;
	}

	/**
	 * 将 Markdown 字符串解析为具有给定标题的 {@link Page}。
	 *
	 * @param registries Holder 查找提供器（传递给 PageBuilder）
	 * @param title      页面标题
	 * @param markdown   原始 Markdown 输入
	 * @return 一个新的、待烘焙的 {@link Page}
	 */
	public static Page parse(HolderLookup.Provider registries, String title, String markdown) {
		return new MarkdownParser(registries).parse(title, markdown);
	}

	/**
	 * 将 Markdown 字符串解析为具有给定标题的 {@link Page}。
	 *
	 * @param title    页面标题
	 * @param markdown 原始 Markdown 输入
	 * @return 一个新的、待烘焙的 {@link Page}
	 */
	public Page parse(String title, String markdown) {
		PageBuilder pb = new PageBuilder(registries, title);
		parseInto(pb, markdown);
		return pb.build();
	}

	/**
	 * 将 Markdown 字符串输入到已有的 {@link PageBuilder} 中，追加从 Markdown 解析出的行。
	 *
	 * @param pb       要填充的 PageBuilder
	 * @param markdown 原始 Markdown 输入
	 */
	public void parseInto(PageBuilder pb, String markdown) {
		if (markdown == null || markdown.isEmpty()) {
			return;
		}
		String[] rawLines = markdown.split("\n", -1);
		new ParserStateMachine(pb, registries).process(rawLines);
	}

	/**
	 * 返回单个 Unicode 码点的估算像素宽度。
	 */
	static int charWidth(int codePoint) {
		return codePoint <= 0x7F ? ASCII_CHAR_W : NON_ASCII_CHAR_W;
	}

	/**
	 * 返回文本字符串的估算像素宽度，逐个字符累加宽度。
	 */
	static int estimateTextWidth(String text) {
		int w = 0;
		for (int i = 0; i < text.length();) {
			int cp = text.codePointAt(i);
			w += charWidth(cp);
			i += Character.charCount(cp);
		}
		return w;
	}

	/**
	 * 去除字符串中的 &amp;-格式码，仅保留可见文本。
	 * 移除 {@code &l}、{@code &m}、{@code &#ffffff} 等模式。
	 */
	static String stripFormatCodes(String text) {
		return text.replaceAll("&[0-9a-fklmnor]|&#[0-9a-fA-F]{3,6}", "");
	}

	// ---------------------------------------------------------------
	// 格式化辅助方法
	// ---------------------------------------------------------------

	/**
	 * 将 Markdown 内联格式转换为 {@link com.khjxiaogu.beecrasy.utils.StringComponentParser} 所能理解的 &amp;-码。
	 *
	 * <p>使用单遍扫描 + 格式栈，确保关闭格式（如 {@code &l}）后不会丢失外部格式：
	 * 关闭时输出 {@code &r} 后接保存的外部格式前缀。</p>
	 *
	 * <ul>
	 *   <li>{@code **粗体**} → {@code &l粗体&r外部格式}</li>
	 *   <li>{@code *斜体*} → {@code &o斜体&r外部格式}</li>
	 *   <li>{@code ~~删除线~~} → {@code &m删除线&r外部格式}</li>
	 *   <li>{@code `代码`} → {@code 代码}（去除反引号）</li>
	 * </ul>
	 *
	 * <p>输入中已有的 &amp;-码保持不变，同时更新内部状态以追踪活跃格式。</p>
	 */
	static String convertInlineFormatting(String text) {
		StringBuilder out = new StringBuilder();
		StringBuilder effPrefix = new StringBuilder();
		Deque<FmtEntry> stack = new ArrayDeque<>();

		int i = 0;
		while (i < text.length()) {
			// 1) 尝试闭合栈顶格式（内层优先关闭）
			if (!stack.isEmpty()) {
				FmtEntry top = stack.peek();
				if (text.startsWith(top.closeDelim, i)) {
					stack.pop();
					out.append("&r");
					out.append(top.savedPrefix);
					effPrefix.setLength(0);
					effPrefix.append(top.savedPrefix);
					i += top.closeDelim.length();
					continue;
				}
			}

			// 2) 尝试开启新格式
			// **bold**
			if (text.startsWith("**", i)) {
				int end = text.indexOf("**", i + 2);
				if (end >= 0) {
					stack.push(new FmtEntry("**", "&l", effPrefix.toString()));
					out.append("&l");
					effPrefix.append("&l");
					i += 2;
					continue;
				}
			}
			// *italic* (不是 ** 的一部分)
			if (text.charAt(i) == '*' && !text.startsWith("**", i)) {
				int end = text.indexOf('*', i + 1);
				if (end >= 0 && !text.startsWith("**", end)) {
					stack.push(new FmtEntry("*", "&o", effPrefix.toString()));
					out.append("&o");
					effPrefix.append("&o");
					i += 1;
					continue;
				}
			}
			// ~~strikethrough~~
			if (text.startsWith("~~", i)) {
				int end = text.indexOf("~~", i + 2);
				if (end >= 0) {
					stack.push(new FmtEntry("~~", "&m", effPrefix.toString()));
					out.append("&m");
					effPrefix.append("&m");
					i += 2;
					continue;
				}
			}

			// 3) 原生 & 格式码
			if (text.charAt(i) == '&' && i + 1 < text.length()) {
				char next = text.charAt(i + 1);
				if (next == '#' && i + 2 < text.length()) {
					// &#rrggbb 或 &#rgb
					int hexEnd = i + 2;
					while (hexEnd < text.length()) {
						char hc = text.charAt(hexEnd);
						if ((hc >= '0' && hc <= '9') || (hc >= 'a' && hc <= 'f')
								|| (hc >= 'A' && hc <= 'F')) {
							hexEnd++;
						} else {
							break;
						}
					}
					int hexLen = hexEnd - (i + 2);
					if (hexLen == 6 || hexLen == 3) {
						String colorCode = text.substring(i, hexEnd);
						// effPrefix 中移除旧颜色码，追加新颜色码
						removeColorFromPrefix(effPrefix);
						effPrefix.append(colorCode);
						out.append(colorCode);
						i = hexEnd;
						continue;
					}
				} else if ("lmno".indexOf(next) >= 0) {
					// 装饰格式码 &l &m &n &o
					effPrefix.append('&').append(next);
					out.append('&').append(next);
					i += 2;
					continue;
				} else if (next == 'r') {
					effPrefix.setLength(0);
					out.append("&r");
					i += 2;
					continue;
				}
			}

			// 4) 内联代码 `code` — 剥离反引号
			if (text.charAt(i) == '`') {
				int end = text.indexOf('`', i + 1);
				if (end >= 0) {
					out.append(text, i + 1, end);
					i = end + 1;
					continue;
				}
			}

			// 5) 普通字符
			out.append(text.charAt(i));
			i++;
		}

		// 末尾补关未闭合格式
		while (!stack.isEmpty()) {
			FmtEntry top = stack.pop();
			out.append("&r");
			out.append(top.savedPrefix);
		}

		return out.toString();
	}

	/** 记录 markdown 格式开关的记录。 */
	private record FmtEntry(String closeDelim, String openCode, String savedPrefix) {
	}

	/** 从格式前缀中移除已有的颜色码（&#rrggbb 或 &#rgb）。 */
	private static void removeColorFromPrefix(StringBuilder prefix) {
		for (int j = 0; j < prefix.length(); j++) {
			if (prefix.charAt(j) == '&' && j + 1 < prefix.length() && prefix.charAt(j + 1) == '#') {
				int end = j + 2;
				while (end < prefix.length()) {
					char c = prefix.charAt(end);
					if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
						end++;
					} else {
						break;
					}
				}
				prefix.delete(j, end);
				return;
			}
		}
	}

	/**
	 * 判断该行是否为分隔线（水平线）。
	 */
	static boolean isHr(String line) {
		String trimmed = line.trim();
		if (trimmed.length() < 3) {
			return false;
		}
		String stripped = trimmed.replace(" ", "");
		if (stripped.length() < 3) {
			return false;
		}
		char first = stripped.charAt(0);
		if (first != '-' && first != '*' && first != '_') {
			return false;
		}
		for (int i = 1; i < stripped.length(); i++) {
			if (stripped.charAt(i) != first) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 若该行为标题则返回标题级别（1–4），否则返回 0。
	 */
	static int headingLevel(String line) {
		String trimmed = line.trim();
		if (trimmed.isEmpty() || trimmed.charAt(0) != '#') {
			return 0;
		}
		int level = 0;
		for (int i = 0; i < trimmed.length() && i < 4; i++) {
			if (trimmed.charAt(i) == '#') {
				level++;
			} else {
				break;
			}
		}
		if (level < 1 || level > 4) {
			return 0;
		}
		if (trimmed.length() > level && trimmed.charAt(level) == ' ') {
			return level;
		}
		return 0;
	}

	/**
	 * 判断该行是否为表格行（以 '|' 开头和结尾）。
	 */
	static boolean isTableRow(String line) {
		String trimmed = line.trim();
		return trimmed.startsWith("|") && trimmed.endsWith("|");
	}

	/**
	 * 判断该行是否为表格分隔行（例如 |---|---|---|）。
	 */
	static boolean isTableSeparator(String line) {
		String trimmed = line.trim();
		if (!trimmed.startsWith("|") || !trimmed.endsWith("|")) {
			return false;
		}
		String inner = trimmed.substring(1, trimmed.length() - 1).trim();
		if (inner.isEmpty()) {
			return false;
		}
		String[] cells = inner.split("\\|");
		for (String cell : cells) {
			String c = cell.trim();
			if (!c.matches(":?-{3,}:?")) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断该行是否为围栏代码块标记。
	 */
	static boolean isFence(String line) {
		String trimmed = line.trim();
		return trimmed.startsWith("```");
	}

	/**
	 * 去除列表标记（-、* 或数字.）及随后的空格后返回内容。
	 * 若非列表项则返回 null。
	 */
	static String listItemContent(String line) {
		String trimmed = line.trim();
		// 无序列表："- " 或 "* "
		if (trimmed.length() >= 2 && (trimmed.charAt(0) == '-' || trimmed.charAt(0) == '*')
				&& trimmed.charAt(1) == ' ') {
			return trimmed.substring(2);
		}
		// 有序列表："1. " 等
		int dotIndex = trimmed.indexOf('.');
		if (dotIndex > 0 && dotIndex < trimmed.length() - 1 && trimmed.charAt(dotIndex + 1) == ' ') {
			boolean allDigits = true;
			for (int i = 0; i < dotIndex; i++) {
				if (!Character.isDigit(trimmed.charAt(i))) {
					allDigits = false;
					break;
				}
			}
			if (allDigits) {
				return trimmed.substring(dotIndex + 2);
			}
		}
		return null;
	}

	/**
	 * 去除 "> " 前缀后返回引用内容，若非引用则返回 null。
	 */
	static String blockquoteContent(String line) {
		String trimmed = line.trim();
		if (trimmed.startsWith(">")) {
			String after = trimmed.substring(1);
			if (after.startsWith(" ")) {
				return after.substring(1);
			}
			return after;
		}
		return null;
	}

	/**
	 * 将表格数据行解析为各个单元格的字符串。
	 */
	static List<String> parseTableRow(String line) {
		List<String> cells = new ArrayList<>();
		String trimmed = line.trim();
		// 去除首尾的 '|'
		String inner = trimmed;
		if (inner.startsWith("|")) {
			inner = inner.substring(1);
		}
		if (inner.endsWith("|")) {
			inner = inner.substring(0, inner.length() - 1);
		}
		String[] parts = inner.split("\\|", -1);
		for (String part : parts) {
			cells.add(part.trim());
		}
		return cells;
	}

	/**
	 * 若该行为 Markdown 图片引用，则返回包含解析后 Identifier 和尺寸的 {@link ImageInfo}；
	 * 否则返回 null。
	 *
	 * <p>支持的格式：</p>
	 * <ul>
	 *   <li>{@code ![alt](namespace:path)} — width=32, height=32</li>
	 *   <li>{@code ![alt](namespace:path WxH)} — 显式指定尺寸</li>
	 * </ul>
	 */
	static ImageInfo parseImageLine(String line) {
		var matcher = IMAGE_LINE.matcher(line.trim());
		if (!matcher.matches() || !matcher.group(1).contains(":")) {
			return null;
		}
		String idStr = matcher.group(1);
		Identifier id;
		try {
			id = Identifier.parse(idStr);
		} catch (Exception e) {
			return null;
		}
		int width = 32;
		int height = 32;
		if (matcher.group(2) != null && matcher.group(3) != null) {
			try {
				width = Integer.parseInt(matcher.group(2));
				height = Integer.parseInt(matcher.group(3));
			} catch (NumberFormatException ignored) {
			}
		}
		return new ImageInfo(id, width, height);
	}

	/**
	 * 保存图片行解析结果。
	 */
	record ImageInfo(Identifier id, int width, int height) {
	}

	/**
	 * 尝试将文本解析为物品表达式 {@code [modid:item]}、{@code [#modid:tag]}、
	 * 或 {@code [modid:a, modid:b, ...]}。
	 *
	 * @param text      待解析文本
	 * @param registries Holder 查找提供器
	 * @return 若匹配则返回对应 Ingredient，否则返回 null
	 */
	static Ingredient parseItemExpression(String text, HolderLookup.Provider registries) {
		String trimmed = text.trim();
		if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
			return null;
		}
		String inner = trimmed.substring(1, trimmed.length() - 1).trim();
		if (inner.isEmpty()) {
			return null;
		}

		try {
			// 标签: [#modid:path]
			if (inner.startsWith("#")) {
				Identifier id = Identifier.parse(inner.substring(1));
				TagKey<Item> tag = TagKey.create(Registries.ITEM, id);
				return Ingredient.of(registries.getOrThrow(tag));
			}

			// 逗号分隔的多物品
			String[] parts = inner.split(",");
			if (parts.length == 1) {
				Identifier id = Identifier.parse(parts[0].trim());
				Item item = registries.lookupOrThrow(Registries.ITEM)
						.getOrThrow(ResourceKey.create(Registries.ITEM, id)).value();
				return Ingredient.of(item);
			}

			Item[] items = new Item[parts.length];
			for (int j = 0; j < parts.length; j++) {
				Identifier id = Identifier.parse(parts[j].trim());
				items[j] = registries.lookupOrThrow(Registries.ITEM)
						.getOrThrow(ResourceKey.create(Registries.ITEM, id)).value();
			}
			return Ingredient.of(items);
		} catch (Exception e) {
			return null;
		}
	}

	// ---------------------------------------------------------------
	// 内部解析器状态机
	// ---------------------------------------------------------------

	private static final class ParserStateMachine {
		private final PageBuilder pb;
		private final HolderLookup.Provider registries;
		private final List<String> paragraphBuffer = new ArrayList<>();
		private boolean inCodeBlock = false;
		private final List<String> codeBlockLines = new ArrayList<>();
		private boolean inTable = false;
		private final List<List<String>> tableRows = new ArrayList<>();

		ParserStateMachine(PageBuilder pb, HolderLookup.Provider registries) {
			this.pb = pb;
			this.registries = registries;
		}

		void process(String[] lines) {
			for (String line : lines) {
				if (inCodeBlock) {
					handleCodeBlockLine(line);
				} else if (inTable) {
					handleTableLine(line);
				} else {
					handleNormalLine(line);
				}
			}
			flushParagraph();
			if (inCodeBlock) {
				if (!codeBlockLines.isEmpty()) {
					pb.addLine(new Text(new ArrayList<>(codeBlockLines), 0.8f, false));
					codeBlockLines.clear();
				}
				inCodeBlock = false;
			}
			if (inTable) {
				flushTable();
			}
		}

		private void handleNormalLine(String line) {
			if (line.trim().isEmpty()) {
				flushParagraph();
				pb.addLine(Space.DEFAULT);
				return;
			}

			ImageInfo img = parseImageLine(line);
			if (img != null) {
				flushParagraph();
				pb.addLine(new Image(img.id(), img.width(), img.height()));
				return;
			}

			if (isFence(line)) {
				flushParagraph();
				inCodeBlock = true;
				codeBlockLines.clear();
				return;
			}

			if (isHr(line)) {
				flushParagraph();
				pb.addLine(Split.DEFAULT);
				return;
			}

			int hLevel = headingLevel(line);
			if (hLevel > 0) {
				flushParagraph();
				String content = convertInlineFormatting(line.trim().substring(hLevel + 1));
				switch (hLevel) {
				case 1 -> pb.text(List.of(content), 2.0f, true);
				case 2 -> pb.text(List.of(content), 1.5f, true);
				case 3 -> pb.text(List.of(content), 1.25f, false);
				case 4 -> pb.text(List.of(content), 1.1f, false);
				default -> pb.text(List.of(content));
				}
				return;
			}

			if (isTableRow(line)) {
				flushParagraph();
				if (isTableSeparator(line)) {
					// 跳过分隔行
					return;
				}
				inTable = true;
				tableRows.clear();
				tableRows.add(parseTableRow(line));
				return;
			}

			String listContent = listItemContent(line);
			if (listContent != null) {
				flushParagraph();
				pb.text(List.of("\u2022 " + convertInlineFormatting(listContent)));
				return;
			}

			String quoteContent = blockquoteContent(line);
			if (quoteContent != null) {
				flushParagraph();
				pb.text(List.of("  " + convertInlineFormatting(quoteContent)));
				return;
			}

			// 物品表达式行: [modid:item] 或 [#modid:tag] 或 [modid:a, modid:b]
			Ingredient itemExpr = parseItemExpression(line.trim(), registries);
			if (itemExpr != null) {
				flushParagraph();
				pb.item(itemExpr);
				return;
			}

			paragraphBuffer.add(line);
		}

		private void handleCodeBlockLine(String line) {
			if (isFence(line)) {
				inCodeBlock = false;
				if (!codeBlockLines.isEmpty()) {
					pb.addLine(new Text(new ArrayList<>(codeBlockLines), 0.8f, false));
					codeBlockLines.clear();
				}
				return;
			}
			codeBlockLines.add(line);
		}

		private void handleTableLine(String line) {
			if (line.trim().isEmpty()) {
				flushTable();
				pb.addLine(Space.DEFAULT);
				return;
			}
			if (isTableRow(line)) {
				if (!isTableSeparator(line)) {
					tableRows.add(parseTableRow(line));
				}
				return;
			}
			flushTable();
			handleNormalLine(line);
		}

		private void flushParagraph() {
			if (!paragraphBuffer.isEmpty()) {
				String merged = String.join("\n", paragraphBuffer);
				pb.text(List.of(convertInlineFormatting(merged)));
				paragraphBuffer.clear();
			}
		}

		private void flushTable() {
			if (!inTable || tableRows.isEmpty()) {
				inTable = false;
				tableRows.clear();
				return;
			}
			int numCols = 0;
			for (List<String> row : tableRows) {
				numCols = Math.max(numCols, row.size());
			}
			if (numCols == 0) {
				inTable = false;
				tableRows.clear();
				return;
			}


			AutoTableBuilder tb = pb.autoTable();
			AutoTableBuilder.ColumnBuilder cb = tb.column();
			for (int j = 0; j < numCols; j++) {
				if (j > 0) {
					cb = cb.column();
				}
				for (List<String> row : tableRows) {
					String cellText = (j < row.size()) ? row.get(j) : "";
					if (cellText.isEmpty()) {
						cb.cell("");
						continue;
					}
					Ingredient itemExpr = parseItemExpression(cellText, registries);
					if (itemExpr != null) {
						cb.cell(itemExpr);
					} else {
						cb.cell(convertInlineFormatting(cellText));
					}
				}
			}
			cb.end();

			inTable = false;
			tableRows.clear();
		}
	}
}
