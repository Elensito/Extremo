package com.bestiarymod.entity;

import com.bestiarymod.Extremo;
import com.bestiarymod.item.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;

public class CaveBrute extends Zombie {

    private boolean growing = false;
    private int growTicks = 0;
    private boolean hasGrown = false;

    public CaveBrute(EntityType<? extends CaveBrute> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean isBaby() {
        return false;
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
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (!hasGrown && !growing && !this.isDeadOrDying() && this.getHealth() <= this.getMaxHealth() * 0.5f) {
                growing = true;
                growTicks = 0;
                setIronArmor();
                this.level().playSound(null, this.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, this.getSoundSource(), 1.0f, 0.8f);
                this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 0, false, false));
            }

            if (growing) {
                growTicks++;
                float progress = Math.min(1.0f, (float) growTicks / 40.0f);
                double scale = 1.0 + progress;
                var attr = this.getAttribute(Attributes.SCALE);
                if (attr != null) {
                    attr.setBaseValue(scale);
                    this.refreshDimensions();
                }
                if (progress >= 1.0f) {
                    growing = false;
                    hasGrown = true;
                }
            }
        }
    }

    private void setIronArmor() {
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        this.setDropChance(EquipmentSlot.HEAD, 0.0f);
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        this.setDropChance(EquipmentSlot.CHEST, 0.0f);
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        this.setDropChance(EquipmentSlot.LEGS, 0.0f);
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        this.setDropChance(EquipmentSlot.FEET, 0.0f);
    }

    private void setChainmailArmor() {
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
        this.setDropChance(EquipmentSlot.HEAD, 0.0f);
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
        this.setDropChance(EquipmentSlot.CHEST, 0.0f);
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
        this.setDropChance(EquipmentSlot.LEGS, 0.0f);
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
        this.setDropChance(EquipmentSlot.FEET, 0.0f);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData);
        if (data == null) {
            data = new SpawnGroupData() {};
        }
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.TORCH));
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0f);
        setChainmailArmor();
        this.setCustomName(Component.literal("\u00a74Bruto de las Cavernas"));
        this.setCustomNameVisible(true);
        return data;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.random.nextFloat() < 0.15f) {
            ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
            var enchants = level.registryAccess().lookup(Registries.ENCHANTMENT).orElse(null);
            if (enchants != null) {
                var efficiency = enchants.get(Enchantments.EFFICIENCY).orElse(null);
                if (efficiency != null) {
                    pickaxe.enchant(efficiency, 1);
                }
            }
            this.spawnAtLocation(level, pickaxe);
        }
        if (this.random.nextFloat() < 0.35f) {
            this.spawnAtLocation(level, new ItemStack(Items.IRON_INGOT));
        }
        if (this.random.nextFloat() < 0.75f) {
            this.spawnAtLocation(level, new ItemStack(ModItems.COIN_COPPER));
        }
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel level) {
        return 20;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        float damage = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        ItemStack weapon = this.getWeaponItem();
        var damageType = level.registryAccess().lookup(Registries.DAMAGE_TYPE)
            .flatMap(reg -> reg.get(Extremo.MOB_ATTACK_NO_SCALE)).orElse(null);
        DamageSource source = damageType != null
            ? new DamageSource(damageType, this)
            : weapon.getDamageSource(this);
        damage = EnchantmentHelper.modifyDamage(level, weapon, target, source, damage);
        damage = damage + weapon.getItem().getAttackDamageBonus(target, damage, source);
        Vec3 delta = target.getDeltaMovement();
        boolean hurt = target.hurtServer(level, source, damage);
        if (hurt) {
            this.causeExtraKnockback(target, this.getKnockback(target, source), delta, source, damage, true);
            if (target instanceof LivingEntity living) {
                weapon.hurtEnemy(living, this);
            }
            EnchantmentHelper.doPostAttackEffects(level, target, source);
            this.setLastHurtMob(target);
            this.playAttackSound();
        }
        this.postPiercingAttack();
        return hurt;
    }

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes().add(Attributes.MAX_HEALTH, 45.0).add(Attributes.ATTACK_DAMAGE, 2.0).add(Attributes.SCALE, 1.0);
    }
}
