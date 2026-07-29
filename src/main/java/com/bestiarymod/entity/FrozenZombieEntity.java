package com.bestiarymod.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;

public class FrozenZombieEntity extends Zombie {

    public FrozenZombieEntity(EntityType<? extends FrozenZombieEntity> entityType, Level level) {
        super(entityType, level);
        setCustomName(Component.literal("\u00a7bZombie de Hielo"));
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
        if (level().getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) {
            discard();
            return;
        }
        super.checkDespawn();
    }

    public static AttributeSupplier.Builder createFrozenZombieAttributes() {
        return Zombie.createAttributes()
            .add(Attributes.MAX_HEALTH, 30.0)
            .add(Attributes.ATTACK_DAMAGE, 5.0);
    }
}
