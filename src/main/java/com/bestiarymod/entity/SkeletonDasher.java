package com.bestiarymod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.jetbrains.annotations.Nullable;

public class SkeletonDasher extends Skeleton implements CrossbowAttackMob {

    private int dashTicks = 0;

    public SkeletonDasher(EntityType<? extends SkeletonDasher> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return false;
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
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData);
        if (data == null) {
            data = new SpawnGroupData() {};
        }
        return data;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(3, new RangedCrossbowAttackGoal<>(this, 1.0, 15.0f));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (dashTicks > 0) {
            AABB bb = this.getBoundingBox().inflate(0.5);
            for (Entity entity : this.level().getEntities(this, bb)) {
                if (entity instanceof LivingEntity living && entity != this && !(entity instanceof SkeletonDasher)) {
                    living.hurt(this.damageSources().mobAttack(this), 50.0f);
                }
            }
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 0.1, this.getZ(), 3, 0.3, 0.05, 0.3, 0.05);
                serverLevel.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.05, this.getZ(), 2, 0.4, 0.02, 0.4, 0.02);
            }
            dashTicks--;
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float pullProgress) {
        InteractionHand hand = ProjectileUtil.getWeaponHoldingHand(this, Items.CROSSBOW);
        ItemStack crossbowStack = this.getItemInHand(hand);
        if (!crossbowStack.is(Items.CROSSBOW) || !CrossbowItem.isCharged(crossbowStack)) {
            return;
        }

        ChargedProjectiles charged = crossbowStack.get(DataComponents.CHARGED_PROJECTILES);
        if (charged == null || charged.isEmpty()) {
            return;
        }

        for (ItemStack ammo : charged.itemCopies()) {
            ArrowItem arrowItem = (ArrowItem)(ammo.getItem() instanceof ArrowItem ? ammo.getItem() : Items.ARROW);
            AbstractArrow arrow = arrowItem.createArrow(this.level(), ammo, this, crossbowStack);
            arrow.setBaseDamage(10.0);
            if (arrow instanceof Arrow arrowEntity) {
                arrowEntity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 0));
            }

            double dx = target.getX() - this.getX();
            double dy = target.getY(target.getEyeHeight() * 0.333) - arrow.getY();
            double dz = target.getZ() - this.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);

            if (this.level() instanceof ServerLevel serverLevel) {
                arrow.shoot(dx, dy + dist * 0.2, dz, 1.6f, (float)(14 - serverLevel.getDifficulty().getId() * 4));
                this.level().addFreshEntity(arrow);
            } else {
                arrow.shoot(dx, dy + dist * 0.2, dz, 1.6f, 0.0f);
                this.level().addFreshEntity(arrow);
            }
        }

        crossbowStack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);

        double angle = this.getRandom().nextDouble() * 2 * Math.PI;
        double dirX = Math.cos(angle);
        double dirZ = Math.sin(angle);
        this.setDeltaMovement(this.getDeltaMovement().add(dirX * 3.0, 0.4, dirZ * 3.0));
        this.hurtMarked = true;
        dashTicks = 8;

        this.onCrossbowAttackPerformed();
    }

    @Override
    public void performCrossbowAttack(LivingEntity target, float pullProgress) {
        performRangedAttack(target, pullProgress);
    }

    @Override
    public void onCrossbowAttackPerformed() {
    }

    @Override
    public void setChargingCrossbow(boolean charging) {
    }
}
