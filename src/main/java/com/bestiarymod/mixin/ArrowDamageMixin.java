package com.bestiarymod.mixin;

import com.bestiarymod.item.BoneArrowItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public class ArrowDamageMixin {
    @Shadow
    private double baseDamage;

    @Unique
    private boolean extremo$damageBoosted = false;

    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void extremo$boostArrowDamage(EntityHitResult result, CallbackInfo ci) {
        if (extremo$damageBoosted) return;
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        if (arrow.level().isClientSide()) return;
        Entity owner = arrow.getOwner();
        if (owner instanceof ServerPlayer player) {
            AttributeInstance attr = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attr != null && attr.getModifier(BoneArrowItem.DAMAGE_MODIFIER_ID) != null) {
                this.baseDamage += 50.0;
                extremo$damageBoosted = true;
            }
        }
    }
}
