package com.bestiarymod.mixin;

import com.bestiarymod.access.AccessoryDataAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerAccessoryMixin implements AccessoryDataAccessor {
    @Unique
    private int extremoAccessorySlots = 0;

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void onRead(ValueInput input, CallbackInfo ci) {
        extremoAccessorySlots = input.getIntOr("extremoAccessorySlots", 0);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void onWrite(ValueOutput output, CallbackInfo ci) {
        output.putInt("extremoAccessorySlots", extremoAccessorySlots);
    }

    @Override
    public int getExtremoAccessorySlots() {
        return extremoAccessorySlots;
    }

    @Override
    public void setExtremoAccessorySlots(int slots) {
        extremoAccessorySlots = slots;
    }
}
