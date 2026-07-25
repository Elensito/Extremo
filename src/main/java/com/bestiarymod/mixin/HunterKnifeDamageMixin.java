package com.bestiarymod.mixin;

import com.bestiarymod.handler.AccessoryEffectHandler;
import com.bestiarymod.item.HunterKnifeItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class HunterKnifeDamageMixin {
    @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true)
    private float modifyDamage(float amount, DamageSource source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return amount;
        ItemStack weapon = player.getMainHandItem();
        if (!(weapon.getItem() instanceof HunterKnifeItem)) return amount;
        if (!(LivingEntity.class.cast(this) instanceof LivingEntity target)) return amount;
        int bonus = AccessoryEffectHandler.getHunterKnifeBonus(player, target);
        if (bonus > 0) {
            amount += bonus;
        }
        return amount;
    }
}
