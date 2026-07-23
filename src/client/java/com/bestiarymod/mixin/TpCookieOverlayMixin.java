package com.bestiarymod.mixin;

import com.bestiarymod.item.ModItems;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class TpCookieOverlayMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void onRender(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !player.isUsingItem()) return;

        ItemStack using = player.getUseItem();
        if (!using.is(ModItems.TP_COOKIE)) return;

        int remaining = player.getUseItemRemainingTicks();
        int total = using.getItem().getUseDuration(using, player);
        if (total <= 0) return;

        int seconds = (remaining + 19) / 20;
        if (seconds < 1 || seconds > 10) return;

        Font font = mc.font;
        Component text = Component.literal("\u00a7eTeletransport\u00e1ndote... \u00a7l" + seconds);
        int screenWidth = graphics.guiWidth();
        int x = (screenWidth / 2) - (font.width(text) / 2);
        int y = screenWidth / 4;
        graphics.text(font, text, x, y, -1);
    }
}
