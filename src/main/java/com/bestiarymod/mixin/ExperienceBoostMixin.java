package com.bestiarymod.mixin;

import com.bestiarymod.handler.AccessoryItemState;
import com.bestiarymod.item.ExperienceArtifactItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(Player.class)
public class ExperienceBoostMixin {
    @Unique
    private static final ThreadLocal<Boolean> extremoApplyingXp = ThreadLocal.withInitial(() -> false);

    @Inject(method = "giveExperiencePoints", at = @At("HEAD"), cancellable = true)
    private void onGiveXp(int amount, CallbackInfo ci) {
        if (extremoApplyingXp.get()) return;
        Player self = (Player) (Object) this;
        if (!(self instanceof ServerPlayer player)) return;
        List<ItemStack> accessories = AccessoryItemState.getItems(player.getUUID());
        boolean hasArtifact = false;
        for (ItemStack stack : accessories) {
            if (stack.getItem() instanceof ExperienceArtifactItem) {
                hasArtifact = true;
                break;
            }
        }
        if (hasArtifact && amount > 0) {
            int boosted = amount + (int) Math.ceil(amount * 0.1f);
            ci.cancel();
            extremoApplyingXp.set(true);
            player.giveExperiencePoints(boosted);
            extremoApplyingXp.set(false);
        }
    }
}
