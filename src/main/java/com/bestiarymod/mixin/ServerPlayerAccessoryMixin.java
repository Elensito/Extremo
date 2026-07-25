package com.bestiarymod.mixin;

import com.bestiarymod.access.AccessoryDataAccessor;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerAccessoryMixin {
    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void onRestoreFrom(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        ServerPlayer newPlayer = (ServerPlayer)(Object)this;
        AccessoryDataAccessor newAccessor = (AccessoryDataAccessor) newPlayer;
        AccessoryDataAccessor oldAccessor = (AccessoryDataAccessor) oldPlayer;
        newAccessor.setExtremoAccessorySlots(oldAccessor.getExtremoAccessorySlots());
    }
}
