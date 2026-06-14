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

import java.util.function.Consumer;

import org.joml.Matrix3x2f;

import com.khjxiaogu.beecrasy.utils.Utils;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.gui.TiledBlitRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;


/**
 * 流体渲染工具类，提供世界内和 GUI 中的流体绘制辅助方法。
 * <p>
 * 包含获取流体颜色、获取流体模型、GUI 储罐绘制、重复纹理绘制及着色矩形绘制等静态方法。
 * 所有方法均为静态，构造器私有以防止实例化。
 */
public class FluidRenderHelper {

	/**
	 * 私有构造器，防止实例化。
	 */
	private FluidRenderHelper() {
	}
	/**
	 * 根据流体的 {@link FluidModel} 和 {@link FluidStack} 获取染色颜色。
	 * <p>
	 * 若模型提供了流体染色源（{@code fluidTintSource()}），则使用其计算颜色；
	 * 否则返回默认的白色（{@code 0xffffffff}）。
	 *
	 * @param model 流体模型，包含可选的染色源
	 * @param stack 流体栈，传递给染色源用于计算颜色
	 * @return ARGB 颜色值
	 */
	public static int getFluidColor(FluidModel model,FluidStack stack) {
		int color = 0xffffffff;
		if(model.fluidTintSource() != null)
			color=model.fluidTintSource().colorAsStack(stack);
		return color;
	}
	/**
	 * 从模型管理器获取流体对应的 {@link FluidModel}。
	 * <p>
	 * 通过流体默认状态的 {@code FluidState} 在模型的流体状态模型集中查找并返回。
	 *
	 * @param stack 流体栈，用于获取流体类型
	 * @return 对应的流体模型
	 */
	@SuppressWarnings("resource")
	public static FluidModel getFluidModel(FluidStack stack) {
		return Minecraft.getInstance().getModelManager().getFluidStateModelSet()
			.get(stack.getFluid().defaultFluidState());
	}
	/**
	 * 在 GUI 界面中绘制储罐内的流体。
	 * <p>
	 * 根据储罐当前储量按比例计算流体显示高度，使用流体的静止纹理进行 {@link TiledBlitRenderState}
	 * 平铺渲染。若鼠标悬停在储罐区域内，则通过 {@code tooltip} 消费者添加流体名称、组件提示
	 * 和储量百分比信息。
	 *
	 * @param transform GUI 图形提取器，用于提交渲染状态
	 * @param tank      储罐的资源处理器，用于获取流体和容量信息
	 * @param tankIndex 储罐的索引
	 * @param x         储罐左上角 X 坐标
	 * @param y         储罐左上角 Y 坐标
	 * @param w         储罐宽度
	 * @param h         储罐高度
	 * @param mouseX    鼠标当前 X 坐标
	 * @param mouseY    鼠标当前 Y 坐标
	 * @param tooltip   提示文本消费者，用于收集悬停时显示的提示信息
	 */
	@SuppressWarnings("resource")
	public static void handleGuiTank(GuiGraphicsExtractor transform, ResourceHandler<FluidResource> tank,int tankIndex, int x, int y, int w, int h,int mouseX,int mouseY,Consumer<Component> tooltip) {
		FluidResource fr=tank.getResource(tankIndex);
		if(fr.isEmpty())return;
		FluidStack fluid = fr.toStack(tank.getAmountAsInt(tankIndex));
		if (fluid != null && fluid.getFluid() != null) {
			
			int fluidHeight = (int) (h * (tank.getAmountAsInt(tankIndex) / (float) tank.getCapacityAsInt(tankIndex,tank.getResource(tankIndex))));
			FluidModel model = FluidRenderHelper.getFluidModel(fluid);
			int color = FluidRenderHelper.getFluidColor(model, fluid);
			TextureAtlasSprite sprite=model.stillMaterial().sprite();
			AbstractTexture spriteTexture = Minecraft.getInstance().getTextureManager().getTexture(sprite.atlasLocation());
			GpuTextureView texture = spriteTexture.getTextureView();
			transform.submitGuiElementRenderState(new TiledBlitRenderState(
				RenderPipelines.GUI_TEXTURED,
                    TextureSetup.singleTexture(texture, spriteTexture.getSampler()),
                    new Matrix3x2f(transform.pose()),
                    16,16,
                    x,y+h-fluidHeight,x+w,y+h,
                    sprite.getU0(),sprite.getU1(),
                    sprite.getV0(),sprite.getV1(),
                    color,
                    transform.peekScissorStack()
                ));
			if (mouseX >= x && mouseY >= y && mouseX < x + w && mouseY < y + h) {
				Player p=Minecraft.getInstance().player;
				tooltip.accept(fluid.getHoverName());
				for(TypedDataComponent<?> o:fluid.getComponents()) {
					if(o.value() instanceof TooltipProvider tt) {
						tt.addToTooltip(TooltipContext.of(p.level(), p), tooltip, TooltipFlag.NORMAL, fluid);
					}
				}
				tooltip.accept(Utils.string(tank.getAmountAsInt(tankIndex)+"/"+tank.getCapacityAsInt(tankIndex, fr)));
			}
			
		}
	}
	/**
	 * 在世界中提交一个着色纹理矩形。
	 * <p>
	 * 使用 {@link RenderTypes#translucentMovingBlock()} 半透明渲染类型，
	 * 向 {@link SubmitNodeCollector} 提交自定义几何体。
	 * 适用于世界内流体等半透明平面的渲染。
	 *
	 * @param buffer       渲染节点收集器
	 * @param poseStack    当前的变换矩阵栈
	 * @param sprite       纹理精灵
	 * @param x0           矩形起点 X 坐标
	 * @param z0           矩形起点 Z 坐标
	 * @param x1           矩形终点 X 坐标
	 * @param z1           矩形终点 Z 坐标
	 * @param color        着色 ARGB 颜色
	 * @param packedLight  打包的光照值
	 * @param packedOverlay 打包的覆盖层值
	 */
	public static void submitColoredTexturedRect(SubmitNodeCollector buffer,PoseStack poseStack,TextureAtlasSprite sprite,float x0,float z0,float x1,float z1,int color,int packedLight,int packedOverlay) {
		buffer.submitCustomGeometry(poseStack, RenderTypes.translucentMovingBlock(), (matrixStack, builder) -> {
			FluidRenderHelper.drawTexturedColoredRect(builder, matrixStack, x0, z0, x1, z1,
					color,
					sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(), packedLight,
					packedOverlay);
		});
	}

	/**
	 * 构建单个顶点数据。
	 * <p>
	 * 设置顶点的位置、颜色、UV 坐标、覆盖层、光照和法线。
	 *
	 * @param bu      顶点消费者
	 * @param transform 当前变换矩阵
	 * @param color   顶点 ARGB 颜色
	 * @param p1      顶点 X 坐标
	 * @param p2      顶点 Y 坐标
	 * @param u0      顶点 U 纹理坐标
	 * @param u1      顶点 V 纹理坐标
	 * @param light   打包的光照值
	 * @param overlay 打包的覆盖层值
	 */
	private static void buildVertex(VertexConsumer bu, Pose transform, int color,
			float p1, float p2, float u0, float u1, int light, int overlay) {
		bu.addVertex(transform, p1, p2, 0).setColor(color).setUv(u0, u1).setOverlay(overlay).setLight(light)
				.setNormal(1f, 1f, 1f);
	}



	/**
	 * 在指定区域内重复平铺纹理精灵。
	 * <p>
	 * 将纹理精灵以指定尺寸（{@code iconWidth} x {@code iconHeight}）在目标区域
	 * （{@code w} x {@code h}）内进行网格状平铺。当目标区域无法整除时，
	 * 边缘部分会以裁剪后的纹理片段填充。
	 *
	 * @param builder    顶点消费者
	 * @param transform  当前变换矩阵
	 * @param x          区域左上角 X 坐标
	 * @param y          区域左上角 Y 坐标
	 * @param w          区域宽度
	 * @param h          区域高度
	 * @param iconWidth  纹理精灵宽度
	 * @param iconHeight 纹理精灵高度
	 * @param uMin       纹理最小 U 坐标
	 * @param uMax       纹理最大 U 坐标
	 * @param vMin       纹理最小 V 坐标
	 * @param vMax       纹理最大 V 坐标
	 * @param color      着色 ARGB 颜色
	 * @param light      打包的光照值
	 * @param overlay    打包的覆盖层值
	 */
	public static void drawRepeatedSprite(VertexConsumer builder, Pose transform, float x, float y, float w,
			float h, int iconWidth, int iconHeight, float uMin, float uMax, float vMin, float vMax, int color, int light, int overlay) {
		int iterMaxW = (int) (w / iconWidth);
		int iterMaxH = (int) (h / iconHeight);
		float leftoverW = w % iconWidth;
		float leftoverH = h % iconHeight;
		float leftoverWf = leftoverW / iconWidth;
		float leftoverHf = leftoverH / iconHeight;
		float iconUDif = uMax - uMin;
		float iconVDif = vMax - vMin;
		for (int ww = 0; ww < iterMaxW; ww++) {
			for (int hh = 0; hh < iterMaxH; hh++)
				drawTexturedColoredRect(builder, transform, x + ww * iconWidth, y + hh * iconHeight, iconWidth,
						iconHeight, color, uMin, uMax, vMin, vMax, light, overlay);
			drawTexturedColoredRect(builder, transform, x + ww * iconWidth, y + iterMaxH * iconHeight, iconWidth,
					leftoverH, color, uMin, uMax, vMin, (vMin + iconVDif * leftoverHf), light, overlay);
		}
		if (leftoverW > 0) {
			for (int hh = 0; hh < iterMaxH; hh++)
				drawTexturedColoredRect(builder, transform, x + iterMaxW * iconWidth, y + hh * iconHeight, leftoverW,
						iconHeight, color, uMin, (uMin + iconUDif * leftoverWf), vMin, vMax, light, overlay);
			drawTexturedColoredRect(builder, transform, x + iterMaxW * iconWidth, y + iterMaxH * iconHeight, leftoverW,
					leftoverH, color, uMin, (uMin + iconUDif * leftoverWf), vMin,
					(vMin + iconVDif * leftoverHf), light, overlay);
		}
	}

	/**
	 * 绘制单个着色纹理矩形。
	 * <p>
	 * 构建四个顶点（逆时针顺序：左下、右下、右上、左上），提交一个带纹理和颜色的矩形。
	 *
	 * @param builder  顶点消费者
	 * @param transform 当前变换矩阵
	 * @param x        矩形左上角 X 坐标
	 * @param y        矩形左上角 Y 坐标
	 * @param w        矩形宽度
	 * @param h        矩形高度
	 * @param color    着色 ARGB 颜色
	 * @param u0       纹理最小 U 坐标
	 * @param u1       纹理最大 U 坐标
	 * @param v0       纹理最小 V 坐标
	 * @param v1       纹理最大 V 坐标
	 * @param light    打包的光照值
	 * @param overlay  打包的覆盖层值
	 */
	public static void drawTexturedColoredRect(VertexConsumer builder, Pose transform, float x, float y, float w,
			float h, int color, float u0, float u1, float v0, float v1, int light,
			int overlay) {
		buildVertex(builder, transform, color, x, y + h, u0, v1, light, overlay);
		buildVertex(builder, transform, color, x + w, y + h, u1, v1, light, overlay);
		buildVertex(builder, transform, color, x + w, y, u1, v0, light, overlay);
		buildVertex(builder, transform, color, x, y, u0, v0, light, overlay);
	}
}
