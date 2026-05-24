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

package com.khjxiaogu.beecrasy.client.renderer;

import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.blocks.PressBlockEntity;
import com.khjxiaogu.beecrasy.client.ModelReference;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class PressBlockEntityRenderer implements BlockEntityRenderer<PressBlockEntity, PressRenderState> {
	private final QuadInstance quadInstance = new QuadInstance();
	private final ModelReference PLATE;
	private final ModelReference SCREW;
	/**
	 * @param rendererDispatcherIn
	 */
	public PressBlockEntityRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
		PLATE=ModelReference.getModel("block/dynamic/honey_press_plate");
		SCREW=ModelReference.getModel("block/dynamic/honey_press_screw");
	}
	@Override
	public PressRenderState createRenderState() {
		return new PressRenderState();
	}

	@Override
	public void extractRenderState(PressBlockEntity blockEntity, PressRenderState state, float partialTicks, Vec3 cameraPosition, @Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

		state.animateProcess=new Quaternionf().rotateY(getAngle((blockEntity.currentTicks+partialTicks)/20f,blockEntity.maxTicks/20f)*Mth.PI*2);
		state.process=getPosition((blockEntity.currentTicks+partialTicks)/20f,blockEntity.maxTicks/20f);
	}
	/**
	 * 计算转盘当前位置。
	 *
	 * @param t  当前时刻（秒）
	 * @param t1 减速开始和反向加速的基准时刻
	 * @param t2 运动结束时刻
	 * @return 偏转角
	 */
	public static float getAngle(float t, float t1) {
	    if (t < 0) return 0;
	    
	    float half = t1 / 2f; 
	    // 阶段0: 0 ≤ t ≤ 1，匀加速
	    if (t < 1) {
	        return 0.5f * t * t;
	    }
	    // 阶段1: 1 ≤ t ≤ half-1，匀速 1 周/s
	    else if (t < half - 1) {
	        return t - 0.5f;
	    }
	    // 阶段2: half-1 ≤ t ≤ half，匀减速
	    else if (t < half) {
	    	float tau = t - (half - 1); // 0~1
	        return (half - 1.5f) + (tau - 0.5f * tau * tau);
	    }
	    // 阶段3: half ≤ t ≤ half+0.5，静止
	    else if (t < half + 0.5) {
	        return half - 1;
	    }
	    // 阶段4: half+0.5 ≤ t ≤ half+1.5，反向匀加速
	    else if (t < half + 1.5) {
	    	float tau = t - (half + 0.5f); // 0~1
	        return (half - 1) - 0.5f * tau * tau;
	    }
	    // 阶段5: half+1.5 ≤ t ≤ t2，匀速 -1 周/s
	    else if (t < t1) {
	        return 2 * half - t;
	    }
	    // 阶段6: t ≥ t2，角度不变
	    else {
	        return 2 * half - t1;
	    }
	}
	/**
     * 计算滑块当前位置。
     *
     * @param t  当前时间（秒）
     * @param t1 结束时间（秒）
     * @return 位置
     */
    public static float getPosition(float t, float t1) {
    	float half = t1 / 2f;
    	float maxDist = half - 1f;
        // 若已回到原点并停止
        if (t >= t1) {
            return 0;
        }

        float distance;

        // 阶段 6：反向匀速（从 half+1.5 到 t1）
        if (t > half + 1.5) {
            distance = t1 - t;                // 线性减少至 0
        }
        // 阶段 5：反向加速（half+0.5 到 half+1.5）
        else if (t > half + 0.5) {
            float tau = t - (half + 0.5f);    // 相对时间 [0, 1]
            distance = maxDist - 0.5f * tau * tau;
        }
        // 阶段 4：静止（half 到 half+0.5）
        else if (t > half) {
            distance = maxDist;
        }
        // 阶段 3：正向减速（half-1 到 half）
        else if (t > half - 1) {
        	float tau = t - (half - 1);      // 相对时间 [0, 1]
            distance = (half - 1.5f) + tau - 0.5f * tau * tau;
        }
        // 阶段 2：正向匀速（1 到 half-1）
        else if (t > 1) {
            distance = t - 0.5f;
        }
        // 阶段 1：正向加速（0 到 1）
        else {
            distance = 0.5f * t * t;
        }

        // 返回比值
        return distance / maxDist;
    }
	@Override
	public void submit(PressRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		quadInstance.setLightCoords(state.lightCoords);
		poseStack.pushPose();
		float num=-1/2f*state.process;
		poseStack.translate(0f, num, 0f);
		PLATE.submit(poseStack, submitNodeCollector, quadInstance);

		poseStack.rotateAround(state.animateProcess, .5f, .5f, .5f);
		SCREW.submit(poseStack, submitNodeCollector, quadInstance);
		poseStack.popPose();
	}

}
