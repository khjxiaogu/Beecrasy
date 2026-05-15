package com.khjxiaogu.beecrasy.genome;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public record BeeHiveParameters(ServerLevel level,BlockPos position) {

}
