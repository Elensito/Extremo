package com.bestiarymod.entity;

import com.bestiarymod.item.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;

public class BerserkerGolem extends IronGolem {
    private static final double IRON_SHELL_THRESHOLD = 0.4;
    private static final int LEAP_COOLDOWN = 200;
    private static final double LEAP_RANGE = 50.0;
    private static final double LEAF_AOE_RADIUS = 2.5;

    private boolean ironShellActive = false;
    private int leapCooldown = 0;
    private boolean leapActive = false;
    private int leapTicks = 0;
    private Vec3 leapLandingPos = Vec3.ZERO;

    public BerserkerGolem(EntityType<? extends BerserkerGolem> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.literal("\u00a7cBerserker Golem"));
        this.setCustomNameVisible(true);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return false;
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (!this.level().isClientSide() && this.random.nextFloat() < 0.1f) {
            this.spawnAtLocation((ServerLevel) this.level(), new ItemStack(ModItems.BERSERKER_FRAGMENT), 0.0f);
        }
    }

    @Override
    public void checkDespawn() {
        if (this.level().getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) {
            this.discard();
            return;
        }
        super.checkDespawn();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LeapSlamGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 50, true, false, null));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            if (leapCooldown > 0) leapCooldown--;

            if (!ironShellActive && this.getHealth() <= this.getMaxHealth() * IRON_SHELL_THRESHOLD) {
                ironShellActive = true;
                this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, -1, 2, false, false, true));
                serverLevel.sendParticles(ParticleTypes.SOUL,
                    this.getX(), this.getY() + 2.5, this.getZ(),
                    30, 1.0, 1.0, 1.0, 0.2);
            }

            if (leapActive) {
                leapTicks++;
                // Draw landing zone circle on ground
                for (int i = 0; i < 20; i++) {
                    double angle = (i / 20.0) * Math.PI * 2;
                    double dx = Math.cos(angle) * LEAF_AOE_RADIUS;
                    double dz = Math.sin(angle) * LEAF_AOE_RADIUS;
                    serverLevel.sendParticles(ParticleTypes.FLAME,
                        leapLandingPos.x + dx, leapLandingPos.y + 0.1, leapLandingPos.z + dz,
                        1, 0.02, 0, 0.02, 0.01);
                }
                // Cross marker at center
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                    leapLandingPos.x, leapLandingPos.y + 0.5, leapLandingPos.z,
                    5, 0.3, 0.3, 0.3, 0.05);

                if (leapTicks >= 40) {
                    // Land: teleport golem to landing spot
                    this.teleportTo(leapLandingPos.x, leapLandingPos.y, leapLandingPos.z);
                    this.hurtMarked = true;

                    // Explosion visuals
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                        leapLandingPos.x, leapLandingPos.y + 1, leapLandingPos.z,
                        1, 0, 0, 0, 0);
                    serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                        leapLandingPos.x, leapLandingPos.y + 0.5, leapLandingPos.z,
                        1, 0, 0, 0, 0);
                    serverLevel.sendParticles(ParticleTypes.CRIT,
                        leapLandingPos.x, leapLandingPos.y + 0.5, leapLandingPos.z,
                        40, LEAF_AOE_RADIUS, 0.5, LEAF_AOE_RADIUS, 0.3);
                    this.level().playSound(null,
                        leapLandingPos.x, leapLandingPos.y, leapLandingPos.z,
                        SoundEvents.GENERIC_EXPLODE, this.getSoundSource(), 2.0f, 0.8f);

                    // Damage + high knockback to all entities in AOE
                    var entities = this.level().getEntities(this,
                        this.getBoundingBox().inflate(LEAF_AOE_RADIUS, 1, LEAF_AOE_RADIUS));
                    for (var entity : entities) {
                        if (entity instanceof LivingEntity living && entity != this) {
                            living.hurt(this.damageSources().mobAttack(this), 12.0f);
                            Vec3 knockDir = living.position().subtract(leapLandingPos).normalize().scale(2.5);
                            living.setDeltaMovement(knockDir.x, 0.6, knockDir.z);
                            living.hurtMarked = true;
                        }
                    }

                    leapActive = false;
                }
            }
        }
    }

    public boolean canUseLeap() {
        return leapCooldown <= 0 && !leapActive && this.getTarget() != null;
    }

    public void executeLeap(LivingEntity target) {
        leapCooldown = LEAP_COOLDOWN;
        leapActive = true;
        leapTicks = 0;
        leapLandingPos = target.position();

        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        // Launch golem upward
        this.setDeltaMovement(0, 3.0, 0);

        // In-ground marker particles at landing zone
        for (int i = 0; i < 30; i++) {
            double angle = (i / 30.0) * Math.PI * 2;
            double dx = Math.cos(angle) * LEAF_AOE_RADIUS;
            double dz = Math.sin(angle) * LEAF_AOE_RADIUS;
            serverLevel.sendParticles(ParticleTypes.FLAME,
                leapLandingPos.x + dx, leapLandingPos.y + 0.1, leapLandingPos.z + dz,
                1, 0.05, 0, 0.05, 0.02);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.IRON_GOLEM_ATTACK, this.getSoundSource(), 2.0f, 0.5f);
    }

    public static AttributeSupplier.Builder createBerserkerAttributes() {
        return IronGolem.createAttributes()
            .add(Attributes.MAX_HEALTH, 100.0)
            .add(Attributes.ATTACK_DAMAGE, 6.0)
            .add(Attributes.SCALE, 1.5)
            .add(Attributes.MOVEMENT_SPEED, 0.18)
            .add(Attributes.FOLLOW_RANGE, 50.0);
    }

    // --- Leap Slam Goal ---

    static class LeapSlamGoal extends Goal {
        private final BerserkerGolem golem;
        private int windupTicks = 0;

        LeapSlamGoal(BerserkerGolem golem) {
            this.golem = golem;
        }

        @Override
        public boolean canUse() {
            if (!golem.canUseLeap()) return false;
            LivingEntity t = golem.getTarget();
            if (t == null || !t.isAlive()) return false;
            double dist = golem.distanceTo(t);
            return dist >= 4.0 && dist <= LEAP_RANGE;
        }

        @Override
        public void start() {
            windupTicks = 0;
        }

        @Override
        public boolean canContinueToUse() {
            return windupTicks < 15;
        }

        @Override
        public void tick() {
            if (golem.getTarget() == null) {
                windupTicks = 15;
                return;
            }

            golem.getNavigation().stop();
            windupTicks++;

            if (golem.level() instanceof ServerLevel serverLevel) {
                if (windupTicks < 10) {
                    // Wind-up: particles build up on golem
                    double x = golem.getX() + (golem.getRandom().nextDouble() - 0.5) * 1.5;
                    double z = golem.getZ() + (golem.getRandom().nextDouble() - 0.5) * 1.5;
                    serverLevel.sendParticles(ParticleTypes.SMOKE,
                        x, golem.getY() + 2.0, z,
                        1, 0.1, 0.1, 0.1, 0.02);
                } else if (windupTicks == 10) {
                    // Launch
                    serverLevel.sendParticles(ParticleTypes.SONIC_BOOM,
                        golem.getX(), golem.getY() + 1.5, golem.getZ(),
                        1, 0.5, 0.3, 0.5, 0);
                    golem.level().playSound(null, golem.blockPosition(), SoundEvents.IRON_GOLEM_ATTACK, golem.getSoundSource(), 2.0f, 0.4f);
                    golem.executeLeap(golem.getTarget());
                }
            }
        }

        @Override
        public void stop() {
        }
    }
}
