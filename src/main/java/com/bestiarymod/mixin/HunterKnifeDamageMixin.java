package com.bestiarymod.mixin;

import com.bestiarymod.handler.AccessoryEffectHandler;
import com.bestiarymod.item.HunterKnifeItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class HunterKnifeDamageMixin {
    @Unique
    private static final ThreadLocal<Boolean> extremoApplyingKnife = ThreadLocal.withInitial(() -> false);

    @Inject(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)V", at = @At("HEAD"), cancellable = true)
    private void onHurt(DamageSource source, float amount, CallbackInfo ci) {
        if (extremoApplyingKnife.get()) return;
        Entity self = (Entity) (Object) this;
        if (!(self instanceof LivingEntity)) return;
        if (!(source.getEntity() instanceof ServerPlayer player)) return;
        ItemStack weapon = player.getMainHandItem();
        if (!(weapon.getItem() instanceof HunterKnifeItem)) return;
        LivingEntity target = (LivingEntity) self;
        int bonus = AccessoryEffectHandler.getHunterKnifeBonus(player, target);
        if (bonus > 0) {
            ci.cancel();
            extremoApplyingKnife.set(true);
            target.hurt(source, amount + bonus);
            extremoApplyingKnife.set(false);
        }
    }
}
