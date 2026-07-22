package com.bestiarymod.mixin;

import com.bestiarymod.ExtremoClient;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class PlayerNameTagMixin<T extends LivingEntity, S extends LivingEntityRenderState> {

    @Inject(method = "extractNameTags(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void appendHeartsToNameTag(T entity, S renderState, float tickDelta, CallbackInfo ci) {
        if (entity instanceof Player) {
            Integer playerHearts = ExtremoClient.allHearts.get(entity.getUUID());
            if (playerHearts != null && renderState.nameTag != null) {
                MutableComponent heartsText = Component.literal(" ");
                for (int i = 0; i < playerHearts; i++) {
                    if (i > 0) heartsText = heartsText.append(Component.literal(" "));
                    heartsText = heartsText.append(Component.literal("\u2764"));
                }
                renderState.nameTag = renderState.nameTag.copy().append(heartsText);
            }
        }
    }
}
