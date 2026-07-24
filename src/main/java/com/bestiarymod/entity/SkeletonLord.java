package com.bestiarymod.entity;

import com.bestiarymod.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;

public class SkeletonLord extends Skeleton {
    private static final double SUMMON_THRESHOLD = 0.25;
    private double nextSummonHp;

    public SkeletonLord(EntityType<? extends SkeletonLord> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.literal("\u00a76Se\u00f1or de los Huesos"));
        this.setCustomNameVisible(true);
        this.nextSummonHp = getMaxHealth() * (1 - SUMMON_THRESHOLD);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return false;
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (!this.level().isClientSide() && this.random.nextFloat() < 0.1f) {
            this.spawnAtLocation((ServerLevel) this.level(), new ItemStack(ModItems.ENCHANTED_ARROW), 0.0f);
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
        this.goalSelector.addGoal(3, new RangedBowAttackGoal<>(this, 1.0, 20, 15.0f));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void setHealth(float health) {
        super.setHealth(health);
        checkSummon();
    }

    private void checkSummon() {
        if (this.level().isClientSide() || !this.isAlive()) return;
        if (this.getHealth() <= this.nextSummonHp) {
            spawnMinions();
            this.nextSummonHp -= getMaxHealth() * SUMMON_THRESHOLD;
        }
    }

    private void spawnMinions() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        for (int i = 0; i < 2; i++) {
            SkeletonMinion minion = new SkeletonMinion(ModEntities.SKELETON_MINION, serverLevel);
            minion.setPos(this.getX() + (i - 0.5) * 2, this.getY(), this.getZ() + (i - 0.5) * 2);
            serverLevel.addFreshEntity(minion);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
        return data;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float pullProgress) {
        ItemStack bowStack = this.getItemInHand(getMainHandItem().getItem() == Items.BOW ? net.minecraft.world.InteractionHand.MAIN_HAND : net.minecraft.world.InteractionHand.OFF_HAND);
        ItemStack ammo = new ItemStack(Items.ARROW);
        ArrowItem arrowItem = (ArrowItem)(ammo.getItem() instanceof ArrowItem ? ammo.getItem() : Items.ARROW);
        AbstractArrow arrow = arrowItem.createArrow(this.level(), ammo, this, bowStack);
        arrow.setBaseDamage(6.0);
        if (arrow instanceof Arrow arrowEntity) {
            arrowEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
            arrowEntity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 0));
        }

        double dx = target.getX() - this.getX();
        double dy = target.getY(target.getEyeHeight() * 0.333) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        arrow.shoot(dx, dy + dist * 0.2, dz, 1.6f, (float)(14 - this.level().getDifficulty().getId() * 4));
        this.level().addFreshEntity(arrow);
    }

    public static AttributeSupplier.Builder createSkeletonLordAttributes() {
        return Skeleton.createAttributes()
            .add(Attributes.MAX_HEALTH, 50.0)
            .add(Attributes.SCALE, 1.2);
    }
}
