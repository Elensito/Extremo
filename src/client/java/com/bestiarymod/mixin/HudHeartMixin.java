package com.bestiarymod.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HudHeartMixin {
    @Unique
    private static final Identifier HARD_CORE_HEART = Identifier.fromNamespaceAndPath("extremo", "hardcore_heart");

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onExtractRenderState(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        int size = 16;
        int x = (graphics.guiWidth() - size) / 2;
        int y = 10;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HARD_CORE_HEART, x, y, size, size);
    }
}
