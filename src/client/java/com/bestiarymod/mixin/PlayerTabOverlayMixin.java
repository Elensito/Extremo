package com.bestiarymod.mixin;

import com.bestiarymod.ExtremoClient;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
    @Unique
    private static final Identifier HARD_CORE_HEART = Identifier.fromNamespaceAndPath("extremo", "hardcore_heart");

    @Unique
    private static final int HEART_SIZE = 8;

    @Inject(method = "extractRenderState",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;extractPingIcon(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIILnet/minecraft/client/multiplayer/PlayerInfo;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void onExtractPingIcon(GuiGraphicsExtractor graphics, int screenWidth, Scoreboard scoreboard, Objective objective,
                                    CallbackInfo ci,
                                    @Local(index = 25) int nameX,
                                    @Local(index = 26) int nameY,
                                    @Local(index = 27) PlayerInfo playerInfo) {
        int heartCount = ExtremoClient.allHearts.getOrDefault(playerInfo.getProfile().id(), 0);
        if (heartCount <= 0) return;

        Font font = Minecraft.getInstance().font;
        Component displayName = playerInfo.getTabListDisplayName();
        if (displayName == null) {
            displayName = Component.literal(playerInfo.getProfile().name());
        }
        int textWidth = font.width(displayName);
        int heartX = nameX + textWidth + 2;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HARD_CORE_HEART, heartX, nameY, HEART_SIZE, HEART_SIZE);

        Component countText = Component.literal("\u00a7f" + heartCount);
        graphics.text(font, countText, heartX + HEART_SIZE + 1, nameY, -1);
    }
}
