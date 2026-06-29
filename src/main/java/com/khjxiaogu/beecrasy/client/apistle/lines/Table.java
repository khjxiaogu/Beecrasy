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

package com.khjxiaogu.beecrasy.client.apistle.lines;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.khjxiaogu.beecrasy.client.apistle.ApistleScreen;
import com.khjxiaogu.beecrasy.client.apistle.Constants;
import com.khjxiaogu.beecrasy.utils.StringComponentParser;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

public record Table(Optional<IntList> columns,List<List<Cell>> cells) implements UnbakedLine {
	public static record Border(int up,int down,int left,int right){
		public static final Codec<Border> CODEC=RecordCodecBuilder.create(t->t.group(
				Codec.INT.optionalFieldOf("up"   ,Constants.TEXT_COLOR).forGetter(Border::up   ),
				Codec.INT.optionalFieldOf("down" ,Constants.TEXT_COLOR).forGetter(Border::down ),
				Codec.INT.optionalFieldOf("left" ,Constants.TEXT_COLOR).forGetter(Border::left ),
				Codec.INT.optionalFieldOf("right",Constants.TEXT_COLOR).forGetter(Border::right)
				).apply(t, Border::new));
		public static final Border DEFAULT=new Border(Constants.TEXT_COLOR,Constants.TEXT_COLOR,Constants.TEXT_COLOR,Constants.TEXT_COLOR);
		public boolean isDefault() {
			return up()==Constants.TEXT_COLOR&&down()==Constants.TEXT_COLOR&&left()==Constants.TEXT_COLOR&&right()==Constants.TEXT_COLOR;
		}
	    public Border withUp(int up) {
	        return new Border(up, this.down, this.left, this.right);
	    }

	    public Border withDown(int down) {
	        return new Border(this.up, down, this.left, this.right);
	    }

	    public Border withLeft(int left) {
	        return new Border(this.up, this.down, left, this.right);
	    }

	    public Border withRight(int right) {
	        return new Border(this.up, this.down, this.left, right);
	    }
	    public Border withoutUp() {
	        return new Border(0, this.down, this.left, this.right);
	    }

	    public Border withoutDown() {
	        return new Border(this.up, 0, this.left, this.right);
	    }

	    public Border withoutLeft() {
	        return new Border(this.up, this.down, 0, this.right);
	    }

	    public Border withoutRight() {
	        return new Border(this.up, this.down, this.left, 0);
	    }
	}
	public static record Cell(Either<HolderSet<Item>,String> content,Border border) {
		
		public static final Codec<Either<HolderSet<Item>,String>> CONTENT_CODEC=Codec.either(Ingredient.NON_AIR_HOLDER_SET_CODEC, Codec.STRING.xmap(t->t.startsWith("'")?t.substring(1):t, t->"'"+t));
		public static final Codec<Cell> FULL_CODEC=RecordCodecBuilder.create(t->t.group(
				CONTENT_CODEC.fieldOf("content").forGetter(Cell::content),
				Border.CODEC.fieldOf("border").forGetter(Cell::border)
				).apply(t, Cell::new));
		public static final Codec<Cell> CODEC=Codec.either(CONTENT_CODEC, FULL_CODEC).xmap(t->t.map(c->new Cell(c,Border.DEFAULT), c->c), t->t.border().isDefault()?Either.left(t.content()):Either.right(t));
		public static final Cell DEFAULT=new Cell(Either.right(""),Border.DEFAULT);
	}
	public static record BakedCell(Line content,int height,Border border) {
		
	}
	public static final MapCodec<Table> CODEC=RecordCodecBuilder.mapCodec(t->t.group(
			Codec.INT.listOf().<IntList>xmap(IntArrayList::new, o->o).optionalFieldOf("columns").forGetter(Table::columns),
			Cell.CODEC.orElse(Cell.DEFAULT).listOf().listOf().fieldOf("cells").forGetter(Table::cells)
			).apply(t, Table::new)
			
			);
	Table(List<Integer> columns,List<List<Cell>> cells) {
		this(Optional.of(new IntArrayList(columns)),cells);
	}
	Table(List<List<Cell>> cells) {
		this(Optional.empty(),cells);
	}
	@Override
	public Line bake(int width) {
		int[] columns;
		if(this.columns().isPresent()) {
			columns=this.columns().get().toIntArray();
		}else {
			int numCols=0;
			for (List<Cell> row : cells) {
				numCols = Math.max(numCols, row.size());
			}
			int[] maxContentW = new int[numCols];
			for (List<Cell> row : cells) {
				for (int j = 0; j < row.size(); j++) {
					Cell plain = row.get(j);
					int w = plain.content().map(_->16, t->Minecraft.getInstance().font.width(StringComponentParser.parse(t)));
					if (w > maxContentW[j]) {
						maxContentW[j] = w;
					}
				}
			}
			// Calculate available content width after accounting for borders
			// Table.bake() computes total width as: 2 + sum(widths[j] + 2)
			// Per-column overhead = 2, plus initial 2 for outer edges
			int totalOverhead = 2 + 2 * numCols;
			int availableContentWidth = ApistleScreen.PAGE_WIDTH - totalOverhead;

			int totalContentWidth = 0;
			for (int j = 0; j < numCols; j++) {
				totalContentWidth += maxContentW[j];
			}

			columns = new int[numCols];
			if (totalContentWidth > availableContentWidth && totalContentWidth > 0) {
				// Scale proportionally to fit
				double scale = (double) availableContentWidth / totalContentWidth;
				int assigned = 0;
				for (int j = 0; j < numCols; j++) {
					columns[j] = Math.max(16, (int) (maxContentW[j] * scale));
					assigned += columns[j];
				}
				int diff = availableContentWidth - assigned;
				if (diff > 0) {
					// Distribute surplus: add 1 to each column starting from the first
					for (int j = 0; diff > 0 && j < numCols; j++) {
						columns[j]++;
						diff--;
					}
				} else if (diff < 0) {
					// Deficit: proportionally reduce columns (>16)
					int deficit = -diff;
					// Calculate total reducible space
					int reducibleTotal = 0;
					for (int j = 0; j < numCols; j++) {
						if (columns[j] > 16) {
							reducibleTotal += (columns[j] - 16);
						}
					}
					if (reducibleTotal > 0) {
						int reduced = 0;
						for (int j = 0; j < numCols; j++) {
							if (columns[j] > 16) {
								int reducible = columns[j] - 16;
								int cut = (int) ((long) deficit * reducible / reducibleTotal);
								columns[j] -= cut;
								reduced += cut;
							}
						}
						// Distribute rounding remainder from front to back
						int remainder = deficit - reduced;
						for (int j = 0; remainder > 0 && j < numCols; j++) {
							if (columns[j] > 16) {
								columns[j]--;
								remainder--;
							}
						}
					}
					// If reducibleTotal == 0, all columns already at minimum 16,
					// deficit cannot be eliminated — this is an extreme data case
				}
			} else {
				for (int j = 0; j < numCols; j++) {
					columns[j] = Math.max(16, maxContentW[j]);
				}
			}
			
		}
		BakedCell[][] cell=new BakedCell[cells.size()][columns.length];
		int[] rows=new int[cells.size()];
		int totalHeight=2;
		int colWidth=2;
		for(int j=0;j<columns.length;j++) {
			colWidth+=columns[j]+2;
		}
		for(int i=0;i<cells.size();i++) {
			List<Cell> currentRow=cells.get(i);
			int rowHeight=0;
			for(int j=0;j<columns.length;j++) {
				if(j>=currentRow.size())
					break;
				Cell currentCell=currentRow.get(j);
				Line content=currentCell.content().map(o->new ItemSpotLine(List.of(Either.left(o)),1f), o->new Text(List.of(o),1,true)).bake(columns[j]);
				int height=content.precalculateHeight();

				cell[i][j]=new BakedCell(content,height,currentCell.border());
				rowHeight=Math.max(rowHeight, height);
			}
			rows[i]=rowHeight;
			totalHeight+=rowHeight+3;
		}
		final int totalHeightf=totalHeight;
		final int colWidthf=colWidth;
		return new Line() {

			@Override
			public int extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int w, int mouseX, int mouseY,
					Consumer<Component> tooltips) {
				int initX=(w-colWidthf)/2+x+1;
				int curY=y+1;
				for(int i=0;i<cell.length;i++) {
					int curX=initX;
					int nextY=curY+rows[i]+3;
					BakedCell[] row=cell[i];
					for(int j=0;j<row.length;j++) {
						int curW=columns[j];
						int nextX=curX+curW+3;
						BakedCell curCell=row[j];
						if(curCell!=null) {
							if(curCell.border().up()!=0)
								graphics.fill(curX-1, curY-1, nextX, curY, curCell.border().up());
							if(curCell.border().left()!=0)
								graphics.fill(curX-1, curY-1, curX, nextY, curCell.border().left());
							if(curCell.border().down()!=0)
								graphics.fill(nextX-1, curY-1, nextX, nextY, curCell.border().down());
							if(curCell.border().right()!=0)
								graphics.fill(curX-1, nextY-1, nextX, nextY, curCell.border().right());
							curCell.content().extractRenderState(graphics, curX+1, curY+1, curW, mouseX, mouseY, tooltips);
						}
						curX=nextX;
					}
					curY=nextY;
				}
				return totalHeightf;
			}

			@Override
			public int precalculateHeight() {
				return totalHeightf;
			}
			
		};
	}

	@Override
	public String type() {
		return "table";
	}

}
