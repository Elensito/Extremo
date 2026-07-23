package com.bestiarymod.mixin;

import com.bestiarymod.access.ConsumableDataAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(Player.class)
public abstract class PlayerConsumableMixin implements ConsumableDataAccessor {
    @Unique
    private Set<String> extremoConsumedItems = new HashSet<>();

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void onRead(ValueInput input, CallbackInfo ci) {
        String raw = input.getStringOr("extremoConsumedItems", "");
        extremoConsumedItems = raw.isEmpty()
            ? new HashSet<>()
            : new HashSet<>(Arrays.asList(raw.split(",")));
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void onWrite(ValueOutput output, CallbackInfo ci) {
        output.putString("extremoConsumedItems", String.join(",", extremoConsumedItems));
    }

    @Override
    public Set<String> getConsumedItems() {
        return extremoConsumedItems;
    }

    @Override
    public void setConsumedItems(Set<String> items) {
        extremoConsumedItems = items;
    }
}
