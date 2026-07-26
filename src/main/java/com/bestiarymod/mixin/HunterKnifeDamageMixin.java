package com.bestiarymod.mixin;

import com.bestiarymod.handler.AccessoryEffectHandler;
import com.bestiarymod.item.HunterKnifeItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class HunterKnifeDamageMixin {
    @ModifyVariable(method = "hurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At("HEAD"), argsOnly = true, index = 2)
    private float modifyDamage(float amount, ServerLevel level, DamageSource source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return amount;
        ItemStack weapon = player.getMainHandItem();
        if (!(weapon.getItem() instanceof HunterKnifeItem)) return amount;
        LivingEntity target = (LivingEntity) (Object) this;
        int bonus = AccessoryEffectHandler.getHunterKnifeBonus(player, target);
        return amount + bonus;
    }
}
