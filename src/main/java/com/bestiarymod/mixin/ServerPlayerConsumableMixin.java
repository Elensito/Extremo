package com.bestiarymod.mixin;

import com.bestiarymod.access.ConsumableDataAccessor;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerConsumableMixin {
    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void onRestoreFrom(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        ServerPlayer newPlayer = (ServerPlayer)(Object)this;
        ConsumableDataAccessor newAccessor = (ConsumableDataAccessor) newPlayer;
        ConsumableDataAccessor oldAccessor = (ConsumableDataAccessor) oldPlayer;
        newAccessor.setConsumedItems(oldAccessor.getConsumedItems());
    }
}
