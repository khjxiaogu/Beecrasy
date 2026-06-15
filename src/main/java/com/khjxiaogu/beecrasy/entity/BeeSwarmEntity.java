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

package com.khjxiaogu.beecrasy.entity;

import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;

import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import com.khjxiaogu.beecrasy.client.BeecrasyParticles;
import com.khjxiaogu.beecrasy.mail.PostalOffice;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.debug.DebugValueSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class BeeSwarmEntity extends Animal implements FlyingAnimal {
    private @Nullable EntityReference<Entity> traceTarget;
    private @Nullable Vec3 targetPos;
    private @Nullable UUID mailId;
    private BeeSwarmEntity.GoToTargetGoal goToTargetGoal;
    private int remainTargetTick;
    private int lifeSpanTicks;
	public BeeSwarmEntity(EntityType<? extends BeeSwarmEntity> type,Level level) {
		super(type, level);
		this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setPathfindingMalus(PathType.FIRE_IN_NEIGHBOR, -1.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 16.0F);
        this.setPathfindingMalus(PathType.COCOA, -1.0F);
        this.setPathfindingMalus(PathType.FENCE, -1.0F);
	}

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return level.getBlockState(pos).isAir() ? 10.0F : 0.0F;
    }

    @Override
    protected void registerGoals() {
        this.goToTargetGoal = new BeeSwarmEntity.GoToTargetGoal();
        this.goalSelector.addGoal(5, this.goToTargetGoal);
        this.goalSelector.addGoal(9, new FloatGoal(this));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.storeNullable("target_pos", Vec3.CODEC, this.targetPos);
        output.storeNullable("mail_id", UUIDUtil.CODEC, this.mailId);
        output.putInt("lifespan", lifeSpanTicks);
        output.storeNullable("traceTarget", EntityReference.codec(), traceTarget);
        output.putInt("remainTargetTick", remainTargetTick);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.targetPos = input.read("target_pos", Vec3.CODEC).orElse(null);
        this.mailId = input.read("mail_id", UUIDUtil.CODEC).orElse(null);
        this.lifeSpanTicks=input.getIntOr("lifespan", 0);
        this.traceTarget = input.read("traceTarget",EntityReference.<Entity>codec()).orElse(null);
        this.remainTargetTick = input.getIntOr("remainTargetTick", 0);
    }

    public double distManhattan(Vec3 pos1,Vec3 pos2) {
        double xd = Math.abs(pos1.x() - pos2.x());
        double yd = Math.abs(pos1.y() - pos2.y());
        double zd = Math.abs(pos1.z() - pos2.z());
        return (xd + yd + zd);
    }
    private void pathfindRandomlyTowards(Vec3 targetVec) {
        int yAdjust = 0;
        Vec3 beePos = this.position();
        int yDelta = (int)(targetVec.y() - beePos.y());
        if (yDelta > 2) {
            yAdjust = 4;
        } else if (yDelta < -2) {
            yAdjust = -4;
        }
        double xzDist = 6;
        double yDist = 8;
        double dist = distManhattan(beePos,targetVec);
        if (dist < 15) {
            xzDist = dist / 2;
            yDist = dist / 2;
        }

        Vec3 nextPosTowards = AirRandomPos.getPosTowards(this, (int)xzDist, (int)yDist, yAdjust, targetVec, (float) (Math.PI / 10));
        if (nextPosTowards != null) {
            this.navigation.setMaxVisitedNodesMultiplier(0.5F);
            this.navigation.moveTo(nextPosTowards.x, nextPosTowards.y, nextPosTowards.z, 1.0);
        }
    }


    @VisibleForDebug
    public int getTravellingTicks() {
        return this.goToTargetGoal.travellingTicks;
    }


    public int getLifeSpanTicks() {
		return lifeSpanTicks;
	}

	public void setLifeSpanTicks(int lifeSpanTicks) {
		this.lifeSpanTicks = lifeSpanTicks;
	}

	@Override
    protected void customServerAiStep(ServerLevel level) {
    	if(lifeSpanTicks>0) {
    		lifeSpanTicks--;
    		if(lifeSpanTicks<=0) {
    			this.discard();
    			return;
    		}
    	}
    	if(traceTarget!=null) {
    		Entity entity=traceTarget.getEntity(level, Entity.class);
    		if(entity!=null) {
    			this.targetPos=entity.getEyePosition();
    		}else {
    			remainTargetTick++;
        		if(remainTargetTick>=40) {
        			this.discard();
        		}
    		}
    	}
    	if(mailId!=null){
    		PostalOffice po=PostalOffice.getPostalOffice(level);
    		if(!po.isStillValid(mailId)) {
    			this.discard();
    		}
    	}
    	if(hasTarget()&&this.closerThan(targetPos, 2)) {
    		remainTargetTick++;
    		
    		if(mailId!=null) {
    			if(remainTargetTick>=20) {
		    		PostalOffice po=PostalOffice.getPostalOffice(level);
		    		Entity entity=traceTarget.getEntity(level, Entity.class);
		    		if(entity instanceof ServerPlayer sp)
			    		if(po.deliver(mailId, sp)) {
			    			mailId=null;
			    		}
    			}
    		}
    		
    		if(remainTargetTick>=40) {
    			this.discard();
    		}
    	}
    }

    @VisibleForDebug
    public boolean hasTarget() {
        return this.targetPos != null||traceTarget != null;
    }

    @VisibleForDebug
    public @Nullable Vec3 getTargetPos() {
        return this.targetPos;
    }

    @VisibleForDebug
    public GoalSelector getGoalSelector() {
        return this.goalSelector;
    }

    private boolean isTooFarAway(Vec3 targetPos) {
        return !this.closerThan(targetPos, 48);
    }


    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
            .add(Attributes.MAX_HEALTH, 10.0)
            .add(Attributes.FLYING_SPEED, 0.6F)
            .add(Attributes.MOVEMENT_SPEED, 0.3F)
            .add(Attributes.GRAVITY,0.0f)
            .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingPathNavigation = new FlyingPathNavigation(this, level) {
            @Override
            public boolean isStableDestination(BlockPos pos) {
                return !this.level.getBlockState(pos.below()).isAir();
            }
        };
        flyingPathNavigation.setCanOpenDoors(false);
        flyingPathNavigation.setCanFloat(true);
        flyingPathNavigation.setRequiredPathLength(48.0F);
        return flyingPathNavigation;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return false;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
    }

    @Override
	public void push(Entity entity) {

	}

	@Override
	protected void pushEntities() {

	}

	@Override
	public void push(Vec3 impulse) {

	}

	@Override
	public void push(double xa, double ya, double za) {

	}

	@Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BEE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BEE_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean isFlying() {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (this.isInvulnerableTo(level, source)) {
            return false;
        }
		return super.hurtServer(level, source, damage);
    }

    @Override
    protected void jumpInLiquid(TagKey<Fluid> type) {
        this.jumpInLiquidInternal();
    }

    private void jumpInLiquidInternal() {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.01, 0.0));
    }

    @Override
    public void jumpInFluid(net.neoforged.neoforge.fluids.FluidType type) {
        this.jumpInLiquidInternal();
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.5F * this.getEyeHeight(), this.getBbWidth() * 0.2F);
    }

    private boolean closerThan(Vec3 targetPos, int distance) {
        return targetPos.distanceToSqr(this.position())<=distance*distance;
    }

    public void setTargetPos(Vec3 targetPos) {
        this.targetPos = targetPos;
    }

    public static boolean attractsBees(BlockState state) {
        if (state.is(BlockTags.BEE_ATTRACTIVE)) {
            if (state.getValueOrElse(BlockStateProperties.WATERLOGGED, false)) {
                return false;
            }
			return state.is(Blocks.SUNFLOWER) ? state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER : true;
        }
		return false;
    }

    @Override
    public void registerDebugValues(ServerLevel level, DebugValueSource.Registration registration) {
        super.registerDebugValues(level, registration);
    }



    @VisibleForDebug
    public class GoToTargetGoal extends Goal {
        private int travellingTicks;
        private @Nullable Path lastPath;
        private int ticksStuck;

        GoToTargetGoal() {
            Objects.requireNonNull(BeeSwarmEntity.this);
            super();
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return BeeSwarmEntity.this.targetPos != null
                && !BeeSwarmEntity.this.isTooFarAway(BeeSwarmEntity.this.targetPos)
                && !this.hasReachedTarget(BeeSwarmEntity.this.targetPos);
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void start() {
            this.travellingTicks = 0;
            this.ticksStuck = 0;
            super.start();
        }

        @Override
        public void stop() {
            this.travellingTicks = 0;
            this.ticksStuck = 0;
            BeeSwarmEntity.this.navigation.stop();
            BeeSwarmEntity.this.navigation.resetMaxVisitedNodesMultiplier();
        }

        @Override
        public void tick() {
            if (BeeSwarmEntity.this.targetPos != null) {
                this.travellingTicks++;
                if (!BeeSwarmEntity.this.navigation.isInProgress()) {
                    if (!BeeSwarmEntity.this.closerThan(BeeSwarmEntity.this.targetPos, 16)) {
                        BeeSwarmEntity.this.pathfindRandomlyTowards(BeeSwarmEntity.this.targetPos);
                    } else {
                        boolean canReachAllTheWayToTarget = this.pathfindDirectlyTowards(BeeSwarmEntity.this.targetPos);
                       if ((!canReachAllTheWayToTarget)||(this.lastPath != null && BeeSwarmEntity.this.navigation.getPath().sameAs(this.lastPath))) {
                            this.ticksStuck++;
                            if (this.ticksStuck > 100) {
                            	BeeSwarmEntity.this.setRemoved(RemovalReason.DISCARDED);
                                this.ticksStuck = 0;
                            }
                        } else {
                            this.lastPath = BeeSwarmEntity.this.navigation.getPath();
                        }
                    }
                }
            }
        }

        private boolean pathfindDirectlyTowards(Vec3 targetPos) {
            int closeEnough = BeeSwarmEntity.this.closerThan(targetPos, 3) ? 1 : 2;
            BeeSwarmEntity.this.navigation.setMaxVisitedNodesMultiplier(10.0F);
            BeeSwarmEntity.this.navigation.moveTo(targetPos.x(), targetPos.y(), targetPos.z(), closeEnough, 1.0);
            return BeeSwarmEntity.this.navigation.getPath() != null && BeeSwarmEntity.this.navigation.getPath().canReach();
        }
        private boolean hasReachedTarget(Vec3 targetPos) {
            if (BeeSwarmEntity.this.closerThan(targetPos, 1)) {
                return true;
            }
			Path path = BeeSwarmEntity.this.navigation.getPath();
			return path != null && path.getTarget().equals(BlockPos.containing(targetPos)) && path.canReach() && path.isDone();
        }

    }

	@Override
	public HumanoidArm getMainArm() {
		return null;
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return null;
	}

	@SuppressWarnings("resource")
	@Override
	public void tick() {
		super.tick();
		if(super.level().isClientSide()) {
			if(super.random.nextInt(4)==0) {
				double dx=this.position().x()+this.getKnownSpeed().x,dy=this.position().y()+this.getKnownSpeed().y,dz=this.position().z()+this.getKnownSpeed().z;

				level().addParticle(BeecrasyParticles.BEE_SWARM.get()
					.create(new Vector4f((float)dx,
						(float)dy,
						(float)dz,
						1.5f)),
					dx+random.nextGaussian(), dy+random.nextGaussian(), dz+random.nextGaussian(),
					0, 0, 0);
			}
		}
	}

	@Override
	public boolean isAlwaysTicking() {
		return true;
	}
    public boolean mayInteract(ServerLevel level, BlockPos pos) {
        return false;
    }

	@Override
	protected void actuallyHurt(ServerLevel level, DamageSource source, float dmg) {

	}

	@Override
	public boolean canFallInLove() {
		return false;
	}

	@Override
	public boolean canBreed() {
		return false;
	}

	@Override
	public boolean canAgeUp() {
		return false;
	}

	@Override
	public boolean canAttack(LivingEntity target) {
		return false;
	}

	@Override
	protected boolean canShearEquipment(Player player) {
		return false;
	}

	@Override
	protected boolean canReplaceCurrentItem(ItemStack newItemStack, ItemStack currentItemStack, EquipmentSlot slot) {
		return false;
	}

	@Override
	public boolean canReplaceEqualItem(ItemStack newItemStack, ItemStack currentItemStack) {
		return false;
	}

	@Override
	public boolean canHoldItem(ItemStack itemStack) {
		return false;
	}

	@Override
	public boolean canPickUpLoot() {
		return false;
	}

	@Override
	protected boolean canDispenserEquipIntoSlot(EquipmentSlot slot) {
		return false;
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	@Override
	public boolean canBeSeenByAnyone() {
		return false;
	}

	@Override
	public boolean canBeHitByProjectile() {
		return false;
	}

	@Override
	public boolean canCollideWith(Entity entity) {
		return false;
	}

	@Override
	public boolean canBeCollidedWith(@Nullable Entity other) {
		return false;
	}

	@Override
	public boolean canControlVehicle() {
		return false;
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	public EntityReference<Entity> getTraceTarget() {
		return traceTarget;
	}

	public void setTraceTarget(Entity traceTarget) {
		this.traceTarget = EntityReference.of(traceTarget);
	}
	public void resetTraceTarget() {
		this.traceTarget = null;
	}
	public UUID getMailId() {
		return mailId;
	}

	public void setMailId(UUID mailId) {
		this.mailId = mailId;
	}
}
