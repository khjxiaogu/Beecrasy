package com.khjxiaogu.beecrasy.client.apistle;

import com.khjxiaogu.beecrasy.network.OpenApistleMessage;

import net.minecraft.client.Minecraft;

public class ApistleManager {
	public static void handlePackage(OpenApistleMessage msg) {
		Minecraft.getInstance().setScreen(new ApistleScreen(msg.id(),msg.title()));
	}
}
