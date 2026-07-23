package com.bestiarymod.mixin;

import com.bestiarymod.access.HeartDataAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerTabListMixin {

    @Inject(method = "getTabListDisplayName", at = @At("HEAD"), cancellable = true)
    private void appendHeartsToTabList(CallbackInfoReturnable<Component> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        HeartDataAccessor accessor = (HeartDataAccessor) player;
        int hearts = accessor.getExtremoHearts();

        MutableComponent name = Component.literal(player.getScoreboardName());
        MutableComponent separator = Component.literal(" \u00a77\u00a7o|\u00a7r ");
        MutableComponent heartsText = Component.literal("\u00a7c\u2764 ");
        heartsText = heartsText.append(Component.literal("\u00a7f" + hearts));

        cir.setReturnValue(name.append(separator).append(heartsText));
    }
}
