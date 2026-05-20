package com.khjxiaogu.beecrasy.client.renderer;

import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.Beecrasy;
import com.khjxiaogu.beecrasy.blocks.PressBlockEntity;
import com.khjxiaogu.beecrasy.client.DynamicModelReference;
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
	private final DynamicModelReference PLATE;
	private final DynamicModelReference SCREW;
	/**
	 * @param rendererDispatcherIn
	 */
	public PressBlockEntityRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
		PLATE=DynamicModelReference.getModel(Beecrasy.rl("block/dynamic/honey_press_plate"));
		SCREW=DynamicModelReference.getModel(Beecrasy.rl("block/dynamic/honey_press_screw"));
	}
	@Override
	public PressRenderState createRenderState() {
		return new PressRenderState();
	}

	@Override
	public void extractRenderState(PressBlockEntity blockEntity, PressRenderState state, float partialTicks, Vec3 cameraPosition, @Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		float degrees=((blockEntity.ticks%20+partialTicks)*18)*Mth.DEG_TO_RAD;
		state.animateProcess=new Quaternionf().rotateY(blockEntity.ticks>160?-degrees:degrees);
		if(blockEntity.ticks>160)
			state.process=(320-blockEntity.ticks)/160f;
		else
			state.process=(blockEntity.ticks)/160f;
	}
	
	@Override
	public void submit(PressRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		poseStack.pushPose();
		float num=-1/2f*state.process;
		poseStack.translate(0f, num, 0f);
		PLATE.submit(poseStack, submitNodeCollector, quadInstance);

		poseStack.rotateAround(state.animateProcess, .5f, .5f, .5f);
		SCREW.submit(poseStack, submitNodeCollector, quadInstance);
		poseStack.popPose();
	}

}
