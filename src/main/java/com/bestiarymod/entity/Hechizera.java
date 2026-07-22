package com.bestiarymod.entity;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownLingeringPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Hechizera extends Witch {
    private int teleportCooldown = 0;
    private int teleportingTicks = 0;
    private Vec3 teleportTarget = null;
    private Vec3 teleportOrigin = null;
    private int lingeringPotionCooldown = 0;
    private static final int TELEPORT_INTERVAL = 200;
    private static final int TELEPORT_DURATION = 60;
    private static final int MAX_TELEPORT_RANGE = 10;
    private static final int LINGERING_POTION_INTERVAL = 100;
    @SuppressWarnings("unchecked")
    private static final Holder<MobEffect>[] HARMFUL_EFFECTS = new Holder[]{
        MobEffects.POISON,
        MobEffects.WITHER,
        MobEffects.WEAKNESS,
        MobEffects.DARKNESS,
        MobEffects.BLINDNESS,
        MobEffects.LEVITATION,
        MobEffects.HUNGER
    };

    public Hechizera(EntityType<? extends Hechizera> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(net.minecraft.network.chat.Component.literal("Hechizera"));
        this.setCustomNameVisible(true);
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
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (teleportingTicks > 0) {
                this.setInvisible(true);
                if (teleportOrigin != null && teleportTarget != null) {
                    ServerLevel serverLevel = (ServerLevel) this.level();
                    serverLevel.sendParticles(ParticleTypes.WITCH, teleportOrigin.x, teleportOrigin.y + 1, teleportOrigin.z, 1, 0.3, 0.3, 0.3, 0.03);
                    serverLevel.sendParticles(ParticleTypes.SOUL, teleportOrigin.x, teleportOrigin.y + 1, teleportOrigin.z, 1, 0.2, 0.2, 0.2, 0.01);
                    serverLevel.sendParticles(ParticleTypes.WITCH, teleportTarget.x, teleportTarget.y + 1, teleportTarget.z, 1, 0.3, 0.3, 0.3, 0.03);
                    serverLevel.sendParticles(ParticleTypes.SOUL, teleportTarget.x, teleportTarget.y + 1, teleportTarget.z, 1, 0.2, 0.2, 0.2, 0.01);
                    for (float t = 0.1f; t < 1.0f; t += 0.2f) {
                        double px = teleportOrigin.x + (teleportTarget.x - teleportOrigin.x) * t;
                        double py = teleportOrigin.y + 1 + (teleportTarget.y - teleportOrigin.y) * t;
                        double pz = teleportOrigin.z + (teleportTarget.z - teleportOrigin.z) * t;
                        serverLevel.sendParticles(ParticleTypes.WITCH, px, py, pz, 1, 0.15, 0.15, 0.15, 0.02);
                        serverLevel.sendParticles(ParticleTypes.SOUL, px, py, pz, 1, 0.1, 0.1, 0.1, 0.01);
                    }
                }

                teleportingTicks--;
                if (teleportingTicks <= 0) {
                    finishTeleport();
                }
            } else {
                if (teleportCooldown <= 0 && this.getTarget() != null) {
                    startTeleport();
                } else if (teleportCooldown > 0) {
                    teleportCooldown--;
                }

                if (lingeringPotionCooldown <= 0 && this.getTarget() != null) {
                    throwLingeringPotion();
                    lingeringPotionCooldown = LINGERING_POTION_INTERVAL;
                } else if (lingeringPotionCooldown > 0) {
                    lingeringPotionCooldown--;
                }
            }
        }
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel serverLevel, DamageSource damageSource) {
        if (teleportingTicks > 0) {
            return true;
        }
        return super.isInvulnerableTo(serverLevel, damageSource);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        if (!effectInstance.getEffect().value().isBeneficial()) {
            return false;
        }
        return super.canBeAffected(effectInstance);
    }

    private void throwLingeringPotion() {
        LivingEntity target = this.getTarget();
        if (target == null) return;

        ItemStack potionStack = new ItemStack(Items.LINGERING_POTION);
        Holder<MobEffect> effect = HARMFUL_EFFECTS[this.getRandom().nextInt(HARMFUL_EFFECTS.length)];
        int duration = 200 + this.getRandom().nextInt(200);
        int amplifier = this.getRandom().nextInt(2);
        potionStack.set(DataComponents.POTION_CONTENTS, new PotionContents(
            Optional.empty(), Optional.empty(),
            List.of(new MobEffectInstance(effect, duration, amplifier)),
            Optional.empty()
        ));

        ThrownLingeringPotion potion = new ThrownLingeringPotion(this.level(), this, potionStack);
        Vec3 toTarget = target.position().subtract(this.position()).add(0, target.getEyeY() - this.getEyeY(), 0);
        potion.shoot(toTarget.x, toTarget.y, toTarget.z, 0.8f, 6.0f);

        this.level().addFreshEntity(potion);
    }

    private void startTeleport() {
        teleportOrigin = this.position();
        Vec3 targetPos = findTeleportDestination();
        if (targetPos == null) return;

        teleportTarget = targetPos;
        teleportingTicks = TELEPORT_DURATION;
        this.setInvisible(true);
        teleportCooldown = TELEPORT_INTERVAL;

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.WITCH, teleportOrigin.x, teleportOrigin.y + 1, teleportOrigin.z, 8, 0.5, 0.5, 0.5, 0.08);
            serverLevel.sendParticles(ParticleTypes.SOUL, teleportOrigin.x, teleportOrigin.y + 1, teleportOrigin.z, 4, 0.3, 0.3, 0.3, 0.04);
        }
    }

    private void finishTeleport() {
        Vec3 target = teleportTarget;
        teleportTarget = null;
        teleportOrigin = null;

        if (target != null) {
            this.teleportTo(target.x, target.y, target.z);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.WITCH, target.x, target.y + 1, target.z, 8, 0.5, 0.5, 0.5, 0.08);
                serverLevel.sendParticles(ParticleTypes.SOUL, target.x, target.y + 1, target.z, 4, 0.3, 0.3, 0.3, 0.04);
            }
        }
        this.setInvisible(false);
    }

    private Vec3 findTeleportDestination() {
        for (int attempt = 0; attempt < 30; attempt++) {
            double angle = this.getRandom().nextDouble() * 2 * Math.PI;
            double dist = 3 + this.getRandom().nextDouble() * (MAX_TELEPORT_RANGE - 3);
            double nx = this.getX() + Math.cos(angle) * dist;
            double nz = this.getZ() + Math.sin(angle) * dist;

            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            pos.set((int) Math.floor(nx), (int) Math.floor(this.getY()), (int) Math.floor(nz));

            for (int yOff = -5; yOff <= 5; yOff++) {
                pos.setY((int) Math.floor(this.getY()) + yOff);
                BlockState state = this.level().getBlockState(pos);
                BlockState below = this.level().getBlockState(pos.below());
                if (state.isAir() && !below.isAir() && !below.liquid()) {
                    return new Vec3(nx, pos.getY() + 0.5, nz);
                }
            }
        }
        return null;
    }

    @Override
    public boolean isInvisible() {
        return teleportingTicks > 0 || super.isInvisible();
    }

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createHechizeraAttributes() {
        return Witch.createAttributes().add(Attributes.MAX_HEALTH, 50.0);
    }
}
