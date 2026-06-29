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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.khjxiaogu.beecrasy.client.apistle.lines.HLine;
import com.khjxiaogu.beecrasy.client.apistle.lines.Image;
import com.khjxiaogu.beecrasy.client.apistle.lines.ItemSpotLine;
import com.khjxiaogu.beecrasy.client.apistle.lines.SpaceLine;
import com.khjxiaogu.beecrasy.client.apistle.lines.Table;
import com.khjxiaogu.beecrasy.client.apistle.lines.Table.Border;
import com.khjxiaogu.beecrasy.client.apistle.lines.Table.Cell;
import com.khjxiaogu.beecrasy.client.apistle.lines.Text;
import com.khjxiaogu.beecrasy.client.apistle.lines.UnbakedLine;
import com.mojang.datafixers.util.Either;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;

public class PageBuilder {
	public class ComplexItemBuilder{
		float scale;
		List<Either<HolderSet<Item>,List<ItemStackTemplate>>> items=new ArrayList<>();
		public ComplexItemBuilder item(Ingredient item) {
			items.add(Either.left(item.getValues()));
			return this;
		}
		public ComplexItemBuilder item(List<ItemStackTemplate> item) {
			items.add(Either.right(item));
			return this;
		}

		public ComplexItemBuilder item(ItemStackTemplate item) {
			return item(List.of(item));
		}
		public ComplexItemBuilder item(Item item) {
			return item(new ItemStackTemplate(item));
		}
		public ComplexItemBuilder item(Holder<Item> item) {
			return item(new ItemStackTemplate(item));
		}
		public ComplexItemBuilder item(Item... item) {
			return item(List.of(item).stream().map(ItemStackTemplate::new).toList());
		}
		@SafeVarargs
		public final ComplexItemBuilder item(Holder<Item>... item) {
			return item(List.of(item).stream().map(ItemStackTemplate::new).toList());
		}
		public PageBuilder end() {
			
			return PageBuilder.this.addLine(new ItemSpotLine(items,scale));
		}
		ComplexItemBuilder(float scale) {
			super();
			this.scale = scale;
		}
	}
	public class TableBuilder{
		public class ColumnBuilder {
			List<Cell> colCells=new ArrayList<>();
			public ColumnBuilder cell(Ingredient item,Border border) {
				colCells.add(new Cell(Either.left(item.getValues()),border));
				return this;
			}
			public ColumnBuilder cell(String text,Border border) {
				colCells.add(new Cell(Either.right(text),border));
				return this;
			}
			public ColumnBuilder cell(String text) {
				return cell(text, Border.DEFAULT);
			}
			public ColumnBuilder cell(Item item,Border border) {
				return cell(Ingredient.of(item),border);
			}
			public ColumnBuilder cell(Holder<Item> item,Border border) {
				return cell(Ingredient.of(item.value()),border);
			}
			public ColumnBuilder cell(TagKey<Item> item,Border border) {
				return cell(Ingredient.of(registries.get(item).get()),border);
			}
			public ColumnBuilder cell(Ingredient item) {
			    return cell(item, Border.DEFAULT);
			}

			public ColumnBuilder cell(Item item) {
			    return cell(item, Border.DEFAULT);
			}

			public ColumnBuilder cell(Holder<Item> item) {
			    return cell(item, Border.DEFAULT);
			}

			public ColumnBuilder cell(TagKey<Item> item) {
			    return cell(item, Border.DEFAULT);
			}
			public ColumnBuilder column(int width) {
				addColumn(colCells);
				return TableBuilder.this.column(width);
			}
			public PageBuilder end() {
				addColumn(colCells);
				return PageBuilder.this.addLine(new Table(Optional.of(columns),cells));
			}
		}
		IntList columns=new IntArrayList();
		List<List<Cell>> cells=new ArrayList<>();
		public ColumnBuilder column(int width) {
			columns.add(width);
			return new ColumnBuilder();
		}
		private void addColumn(List<Cell> column) {
		    int index=columns.size()-1;
		    int targetRows = Math.max(cells.size(), column.size());
		    for (int i = 0; i < targetRows; i++) {
		        // 获取或创建当前行
		        List<Cell> row;
		        if (i < cells.size()) {
		            row = cells.get(i);
		        } else {
		            row = new ArrayList<>();
		            cells.add(row);
		        }
		        // 保证行长度至少为 index（即索引前有足够位置）
		        while (row.size() < index) {
		            row.add(null);
		        }
		        // 取出列值（若列长度不足则 null）
		        Cell value = (i < column.size()) ? column.get(i) : null;
		        // 在 index 位置插入（原有元素右移）
		        row.add(index, value);
		    }
		}
		public PageBuilder end() {
			return PageBuilder.this.addLine(new Table(Optional.of(columns),cells));
		}
		
	}
	public class AutoTableBuilder{
		public class AutoColumnBuilder {
			List<Cell> colCells=new ArrayList<>();
			public AutoColumnBuilder cell(Ingredient item,Border border) {
				colCells.add(new Cell(Either.left(item.getValues()),border));
				return this;
			}
			public AutoColumnBuilder cell(String text,Border border) {
				colCells.add(new Cell(Either.right(text),border));
				return this;
			}
			public AutoColumnBuilder cell(String text) {
				return cell(text, Border.DEFAULT);
			}
			public AutoColumnBuilder cell(Item item,Border border) {
				return cell(Ingredient.of(item),border);
			}
			public AutoColumnBuilder cell(Holder<Item> item,Border border) {
				return cell(Ingredient.of(item.value()),border);
			}
			public AutoColumnBuilder cell(TagKey<Item> item,Border border) {
				return cell(Ingredient.of(registries.get(item).get()),border);
			}
			public AutoColumnBuilder cell(Ingredient item) {
			    return cell(item, Border.DEFAULT);
			}

			public AutoColumnBuilder cell(Item item) {
			    return cell(item, Border.DEFAULT);
			}

			public AutoColumnBuilder cell(Holder<Item> item) {
			    return cell(item, Border.DEFAULT);
			}

			public AutoColumnBuilder cell(TagKey<Item> item) {
			    return cell(item, Border.DEFAULT);
			}
			public AutoColumnBuilder column() {
				addColumn(colCells);
				return AutoTableBuilder.this.column();
			}
			public PageBuilder end() {
				addColumn(colCells);
				return PageBuilder.this.addLine(new Table(Optional.empty(),cells));
			}
		}
		List<List<Cell>> cells=new ArrayList<>();
		public AutoColumnBuilder column() {
			return new AutoColumnBuilder();
		}
		private void addColumn(List<Cell> column) {
		    int index = cells.size();
		    int targetRows = Math.max(cells.size(), column.size());
		    for (int i = 0; i < targetRows; i++) {
		        List<Cell> row;
		        if (i < cells.size()) {
		            row = cells.get(i);
		        } else {
		            row = new ArrayList<>();
		            cells.add(row);
		        }
		        while (row.size() < index) {
		            row.add(null);
		        }
		        Cell value = (i < column.size()) ? column.get(i) : null;
		        row.add(index, value);
		    }
		}
		public PageBuilder end() {
			return PageBuilder.this.addLine(new Table(Optional.empty(),cells));
		}
		
	}
	List<UnbakedLine> lines=new ArrayList<>();
	Optional<Either<Identifier,ItemStackTemplate>> icon=Optional.empty();
	String title;
	int order;
	HolderLookup.Provider registries;
	public PageBuilder(HolderLookup.Provider registries,String title) {
		super();
		this.title = title;
		this.registries = registries;
	}
	public PageBuilder setIcon(Optional<Either<Identifier, ItemStackTemplate>> icon) {
		this.icon=icon;
		return this;
	}
	public PageBuilder setIcon(Either<Identifier, ItemStackTemplate> icon) {
		return setIcon(Optional.of(icon));
	}
	public PageBuilder setIcon(Identifier icon) {
		return setIcon(Either.left(icon));
	}
	public PageBuilder setIcon(ItemStackTemplate icon) {
		return setIcon(Either.right(icon));
	}
	public PageBuilder setIcon(Item icon) {
		return setIcon(new ItemStackTemplate(icon));
	}
	public PageBuilder setIcon(Holder<Item> icon) {
		return setIcon(new ItemStackTemplate(icon));
	}
	public PageBuilder setOrder(int order) {
		this.order=order;
		return this;
	}
	public PageBuilder addLine(UnbakedLine line) {
		this.lines.add(line);
		return this;
	}
	public TableBuilder table() {
		return new TableBuilder();
	}
	public AutoTableBuilder autoTable() {
		return new AutoTableBuilder();
	}
	public PageBuilder space() {
		return addLine(SpaceLine.DEFAULT);
	}
	public PageBuilder space(int height) {
		return addLine(new SpaceLine(height));
	}
	public PageBuilder hr() {
		return addLine(HLine.DEFAULT);
	}
	public PageBuilder hr(int color) {
		return addLine(new HLine(color));
	}
	
	public PageBuilder image(Identifier image,int width,int height) {
		return addLine(new Image(image,width,height));
	}
	public PageBuilder item(Ingredient item,float scale) {
		return addLine(new ItemSpotLine(List.of(Either.left(item.getValues())),scale));
	}
	public ComplexItemBuilder item(float scale) {
		return new ComplexItemBuilder(scale);
	}
	public ComplexItemBuilder item() {
		return item(1f);
	}
	public PageBuilder item(List<ItemStackTemplate> item,float scale) {
		return addLine(new ItemSpotLine(List.of(Either.right(item)),scale));
	}
	public PageBuilder item(ItemStackTemplate item,float scale) {
		return item(List.of(item),scale);
	}
	public PageBuilder item(Item item,float scale) {
		return item(new ItemStackTemplate(item),scale);
	}
	public PageBuilder item(Holder<Item> item,float scale) {
		return item(new ItemStackTemplate(item),scale);
	}
	public PageBuilder item(float scale,Item... item) {
		return item(List.of(item).stream().map(ItemStackTemplate::new).toList(),scale);
	}
	@SafeVarargs
	public final PageBuilder item(float scale,Holder<Item>... item) {
		return item(List.of(item).stream().map(ItemStackTemplate::new).toList(),scale);
	}
	public PageBuilder item(float scale,ItemStackTemplate... item) {
		return item(List.of(item),scale);
	}

	public PageBuilder item(Ingredient item) {
	    return item(item, 1.0f);
	}

	public PageBuilder item(List<ItemStackTemplate> item) {
	    return item(item, 1.0f);
	}

	public PageBuilder item(ItemStackTemplate item) {
	    return item(item, 1.0f);
	}

	public PageBuilder item(Item item) {
	    return item(Ingredient.of(item), 1.0f);
	}

	public PageBuilder item(Holder<Item> item) {
	    return item(Ingredient.of(item.value()), 1.0f);
	}
	public PageBuilder item(TagKey<Item> item) {
	    return item(Ingredient.of(registries.getOrThrow(item)), 1.0f);
	}
	public PageBuilder item(Item... item) {
	    return item(1.0f, item);
	}

	@SafeVarargs
	public final PageBuilder item(Holder<Item>... item) {
	    return item(1.0f, item);
	}

	public PageBuilder item(ItemStackTemplate... item) {
	    return item(1.0f, item);
	}
	public PageBuilder text(List<String> lines,float scale,boolean centered) {
		return addLine(new Text(lines,scale,centered));
	}

	public PageBuilder text(List<String> lines,float scale) {
		return text(lines,scale,false);
	}
	public PageBuilder text(List<String> lines,boolean centered) {
		return text(lines,1f,centered);
	}

	public PageBuilder text(List<String> lines) {
		return text(lines,1f,false);
	}
	public PageBuilder text(String lines,float scale,boolean centered) {
		return text(List.of(lines.split("\n")),scale,centered);
	}
	public PageBuilder text(String lines,float scale) {
		return text(lines,scale,false);
	}
	public PageBuilder text(String lines,boolean centered) {
		return text(lines,1f,centered);
	}
	public PageBuilder text(String lines) {
		return text(lines,1f,false);
	}
	public Page build() {
		return new Page(lines,icon,title,order);
	}
}
