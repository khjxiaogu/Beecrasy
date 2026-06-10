package com.khjxiaogu.beecrasy.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class BeeHiveSoundInstance extends AbstractTickableSoundInstance {



    BlockPos pos;
    BlockState bs;
    float speed=0;
    public BeeHiveSoundInstance(BlockPos pos,BlockState bs,SoundEvent event, SoundSource source) {
        super(event, source, SoundInstance.createUnseededRandom());
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
        this.pos=pos;
        this.bs=bs;
    }

    @Override
    public void tick() {
        if (Minecraft.getInstance().level.getBlockState(pos)==bs) {
        	speed+=super.random.nextGaussian()*0.1;
        	speed=Mth.clamp(speed, 0, 1);
            this.pitch = Mth.lerp(speed, this.getMinPitch(), this.getMaxPitch());
            this.volume = Mth.lerp(speed, 0.0F, 0.6F);

        } else {
            this.stop();
        }
    }

    private float getMinPitch() {
        return 0.7F;
    }

    private float getMaxPitch() {
        return 1.1F;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

}