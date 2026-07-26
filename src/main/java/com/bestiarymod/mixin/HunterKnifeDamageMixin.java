package com.bestiarymod.mixin;

import com.bestiarymod.handler.AccessoryEffectHandler;
import com.bestiarymod.item.HunterKnifeItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class HunterKnifeDamageMixin {
    @Unique
    private static final ThreadLocal<Integer> extremoHurtDepth = ThreadLocal.withInitial(() -> 0);

    @Inject(method = "hurt", at = @At("RETURN"))
    private void afterHurt(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (extremoHurtDepth.get() > 0) return;
        Entity self = (Entity) (Object) this;
        if (!(self instanceof LivingEntity)) return;
        if (!(cir.getReturnValue())) return;
        if (!(source.getEntity() instanceof ServerPlayer player)) return;
        ItemStack weapon = player.getMainHandItem();
        if (!(weapon.getItem() instanceof HunterKnifeItem)) return;
        LivingEntity target = (LivingEntity) self;
        int bonus = AccessoryEffectHandler.getHunterKnifeBonus(player, target);
        if (bonus > 0) {
            extremoHurtDepth.set(1);
            target.hurt(source, bonus);
            extremoHurtDepth.set(0);
        }
    }
}
