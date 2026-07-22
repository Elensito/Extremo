package com.bestiarymod.mixin;

import com.bestiarymod.ExtremoClient;
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

    @Unique
    private static final int SIZE = 16;

    @Unique
    private static final int SPACING = 13;

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onExtractRenderState(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        int count = ExtremoClient.hearts;
        if (count <= 0) return;
        int centerScreen = graphics.guiWidth() / 2;
        int totalWidth = (count - 1) * SPACING;
        int startX = centerScreen - totalWidth / 2 - SIZE / 2;
        int y = 10;
        for (int i = 0; i < count; i++) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HARD_CORE_HEART, startX + i * SPACING, y, SIZE, SIZE);
        }
    }
}
