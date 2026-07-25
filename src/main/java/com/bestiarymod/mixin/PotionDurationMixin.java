package com.bestiarymod.mixin;

import com.bestiarymod.handler.AccessoryEffectHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class PotionDurationMixin {
    @ModifyVariable(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z", at = @At("HEAD"), argsOnly = true)
    private MobEffectInstance modifyPotionDuration(MobEffectInstance effectInstance) {
        if (effectInstance == null) return null;
        if (!(LivingEntity.class.cast(this) instanceof ServerPlayer player)) return effectInstance;
        if (!AccessoryEffectHandler.hasAnilloAlquimista(player)) return effectInstance;
        if (effectInstance.getEffect().value().isBeneficial()) {
            return new MobEffectInstance(effectInstance.getEffect(), effectInstance.getDuration() * 2, effectInstance.getAmplifier(), effectInstance.isAmbient(), effectInstance.isVisible(), effectInstance.showIcon());
        }
        return effectInstance;
    }
}
