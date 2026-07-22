package com.bestiarymod.mixin;

import com.bestiarymod.access.HeartDataAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerHeartMixin implements HeartDataAccessor {
    @Unique
    private int extremoHearts = 5;

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void onRead(ValueInput input, CallbackInfo ci) {
        extremoHearts = input.getIntOr("extremoHearts", 5);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void onWrite(ValueOutput output, CallbackInfo ci) {
        output.putInt("extremoHearts", extremoHearts);
    }

    @Override
    public int getExtremoHearts() {
        return extremoHearts;
    }

    @Override
    public void setExtremoHearts(int hearts) {
        extremoHearts = hearts;
    }
}
