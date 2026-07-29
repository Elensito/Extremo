package com.bestiarymod.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import java.util.EnumSet;

public class WildfireEntity extends Monster {

    private static final EntityDataAccessor<Integer> ACTIVE_SHIELDS_COUNT = SynchedEntityData.defineId(WildfireEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TICKS_UNTIL_SHIELD_REGENERATION = SynchedEntityData.defineId(WildfireEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SUMMONED_BLAZES_COUNT = SynchedEntityData.defineId(WildfireEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WILDFIRE_POSE = SynchedEntityData.defineId(WildfireEntity.class, EntityDataSerializers.INT);

    public WildfireEntity(EntityType<? extends WildfireEntity> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.LAVA, 8.0F);
        this.setPathfindingMalus(PathType.FIRE, 0.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, -1.0F);
        this.setCustomName(Component.literal("\u00a76\u00cdgneo"));
        this.setCustomNameVisible(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTIVE_SHIELDS_COUNT, 4);
        builder.define(TICKS_UNTIL_SHIELD_REGENERATION, 0);
        builder.define(SUMMONED_BLAZES_COUNT, 0);
        builder.define(WILDFIRE_POSE, WildfirePose.IDLE.ordinal());
    }

    public static AttributeSupplier.Builder createWildfireAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 120.0)
            .add(Attributes.ATTACK_DAMAGE, 8.0)
            .add(Attributes.MOVEMENT_SPEED, 0.23)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.ATTACK_KNOCKBACK, 2.0);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData);
        this.setActiveShieldsCount(4);
        this.setSummonedBlazesCount(0);
        this.setPose(WildfirePose.IDLE);
        return data;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            int ticks = this.getTicksUntilShieldRegeneration();
            if (ticks > 0) {
                this.setTicksUntilShieldRegeneration(ticks - 1);
            } else if (this.getActiveShieldsCount() < 4) {
                this.regenerateShield();
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, net.minecraft.world.damagesource.DamageSource damageSource, float amount) {
        if (this.hasActiveShields() && !damageSource.is(DamageTypes.GENERIC)) {
            this.breakShield();
            return false;
        }
        return super.hurtServer(level, damageSource, amount);
    }

    public void breakShield() {
        int shields = this.getActiveShieldsCount();
        this.setActiveShieldsCount(Math.max(0, shields - 1));
        this.setTicksUntilShieldRegeneration(200);
        this.playSound(SoundEvents.SHIELD_BREAK.value(), 1.0F, 0.8F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT,
                this.getX(), this.getY() + 1.0, this.getZ(),
                15, 0.5, 0.5, 0.5, 0.1);
        }
    }

    public void regenerateShield() {
        int shields = this.getActiveShieldsCount();
        this.setActiveShieldsCount(Math.min(4, shields + 1));
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME,
                this.getX(), this.getY() + 1.0, this.getZ(),
                5, 0.3, 0.3, 0.3, 0.05);
        }
    }

    public boolean hasActiveShields() {
        return this.getActiveShieldsCount() > 0;
    }

    public int getActiveShieldsCount() {
        return this.entityData.get(ACTIVE_SHIELDS_COUNT);
    }

    public void setActiveShieldsCount(int count) {
        this.entityData.set(ACTIVE_SHIELDS_COUNT, count);
    }

    public int getTicksUntilShieldRegeneration() {
        return this.entityData.get(TICKS_UNTIL_SHIELD_REGENERATION);
    }

    public void setTicksUntilShieldRegeneration(int ticks) {
        this.entityData.set(TICKS_UNTIL_SHIELD_REGENERATION, ticks);
    }

    public int getSummonedBlazesCount() {
        return this.entityData.get(SUMMONED_BLAZES_COUNT);
    }

    public void setSummonedBlazesCount(int count) {
        this.entityData.set(SUMMONED_BLAZES_COUNT, count);
    }

    public WildfirePose getWildfirePose() {
        return WildfirePose.values()[this.entityData.get(WILDFIRE_POSE)];
    }

    public void setPose(WildfirePose pose) {
        this.entityData.set(WILDFIRE_POSE, pose.ordinal());
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setActiveShieldsCount(input.getInt("ActiveShieldsCount").orElse(4));
        this.setTicksUntilShieldRegeneration(input.getInt("TicksUntilShieldRegeneration").orElse(0));
        this.setSummonedBlazesCount(input.getInt("SummonedBlazesCount").orElse(0));
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("ActiveShieldsCount", this.getActiveShieldsCount());
        output.putInt("TicksUntilShieldRegeneration", this.getTicksUntilShieldRegeneration());
        output.putInt("SummonedBlazesCount", this.getSummonedBlazesCount());
    }

    @Override
    public void checkDespawn() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            this.discard();
        } else {
            super.checkDespawn();
        }
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    public boolean isOnFire() {
        return true;
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel level) {
        return 10;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new BarrageAttackGoal(this));
        this.goalSelector.addGoal(2, new ShockwaveAttackGoal(this));
        this.goalSelector.addGoal(3, new SummonBlazeGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    class BarrageAttackGoal extends Goal {
        private final WildfireEntity wildfire;
        private int attackCooldown = 0;
        private int barrageCount = 0;

        BarrageAttackGoal(WildfireEntity wildfire) {
            this.wildfire = wildfire;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = wildfire.getTarget();
            return target != null && target.isAlive() && wildfire.distanceTo(target) >= 5.0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.barrageCount < 3 && wildfire.getTarget() != null && wildfire.getTarget().isAlive();
        }

        @Override
        public void start() {
            this.barrageCount = 0;
            this.attackCooldown = 0;
        }

        @Override
        public void tick() {
            if (this.attackCooldown > 0) {
                this.attackCooldown--;
                return;
            }
            LivingEntity target = wildfire.getTarget();
            if (target == null) return;

            if (wildfire.level() instanceof ServerLevel serverLevel) {
                double dx = target.getX() - wildfire.getX();
                double dy = target.getY(0.5) - wildfire.getY(0.5);
                double dz = target.getZ() - wildfire.getZ();
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (distance == 0) distance = 1;

                SmallFireball fireball = new SmallFireball(serverLevel, wildfire,
                    new Vec3(dx / distance * 0.5, dy / distance * 0.5, dz / distance * 0.5));
                fireball.setPos(wildfire.getX(), wildfire.getY(0.5) + 0.5, wildfire.getZ());
                serverLevel.addFreshEntity(fireball);
                this.barrageCount++;
                this.attackCooldown = 10;
            }
        }

        @Override
        public void stop() {
        }
    }

    class ShockwaveAttackGoal extends Goal {
        private final WildfireEntity wildfire;
        private int cooldown = 0;
        private int windup = 0;

        ShockwaveAttackGoal(WildfireEntity wildfire) {
            this.wildfire = wildfire;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.cooldown > 0) return false;
            LivingEntity target = wildfire.getTarget();
            return target != null && target.isAlive() && wildfire.distanceTo(target) <= 4.0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.windup < 20;
        }

        @Override
        public void start() {
            this.windup = 0;
        }

        @Override
        public void tick() {
            wildfire.getNavigation().stop();
            this.windup++;
            if (wildfire.level() instanceof ServerLevel serverLevel) {
                if (this.windup == 15) {
                    double px = wildfire.getX();
                    double py = wildfire.getY();
                    double pz = wildfire.getZ();
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, px, py + 1, pz, 1, 0, 0, 0, 0);
                    serverLevel.sendParticles(ParticleTypes.FLAME, px, py + 0.5, pz, 30, 3.0, 0.5, 3.0, 0.1);
                    wildfire.playSound(SoundEvents.GENERIC_EXPLODE.value(), 2.0F, 0.7F);

                    var entities = serverLevel.getEntities(wildfire,
                        wildfire.getBoundingBox().inflate(4.0, 2.0, 4.0));
                    for (var entity : entities) {
                        if (entity instanceof LivingEntity living && entity != wildfire) {
                            living.hurtServer(serverLevel, wildfire.damageSources().mobAttack(wildfire), 10.0F);
                            Vec3 knockback = living.position().subtract(wildfire.position()).normalize().scale(1.5);
                            living.setDeltaMovement(knockback.x, 0.5, knockback.z);
                        }
                    }
                    this.cooldown = 100;
                }
            }
        }

        @Override
        public void stop() {
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
    }

    class SummonBlazeGoal extends Goal {
        private final WildfireEntity wildfire;
        private int cooldown = 0;

        SummonBlazeGoal(WildfireEntity wildfire) {
            this.wildfire = wildfire;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.cooldown > 0) return false;
            if (wildfire.getSummonedBlazesCount() >= 2) return false;
            return wildfire.getHealth() <= wildfire.getMaxHealth() * 0.5;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            if (wildfire.level() instanceof ServerLevel serverLevel) {
                EntityType<Blaze> blazeType = (EntityType<Blaze>) BuiltInRegistries.ENTITY_TYPE.get(Identifier.tryParse("blaze")).map(ref -> ref.value()).orElse(null);
                if (blazeType == null) return;
                for (int i = 0; i < 2; i++) {
                    Blaze blaze = blazeType.create(serverLevel, EntitySpawnReason.SPAWNER);
                    if (blaze != null) {
                        double angle = (i / 2.0) * Math.PI * 2;
                        double offsetX = Math.cos(angle) * 3.0;
                        double offsetZ = Math.sin(angle) * 3.0;
                        blaze.setPos(wildfire.getX() + offsetX, wildfire.getY(), wildfire.getZ() + offsetZ);
                        serverLevel.addFreshEntity(blaze);
                    }
                }
                wildfire.setSummonedBlazesCount(wildfire.getSummonedBlazesCount() + 2);
                this.cooldown = 600;
                serverLevel.sendParticles(ParticleTypes.SOUL,
                    wildfire.getX(), wildfire.getY() + 1, wildfire.getZ(),
                    20, 1.0, 0.5, 1.0, 0.1);
            }
        }

        @Override
        public void tick() {
        }

        @Override
        public void stop() {
        }
    }
}
