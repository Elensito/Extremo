package com.bestiarymod;

import com.bestiarymod.entity.ModEntities;
import com.bestiarymod.item.ModItems;
import com.bestiarymod.network.AllHeartsSyncPayload;
import com.bestiarymod.network.HeartSyncPayload;
import com.bestiarymod.network.ItemActivationPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.WitchRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExtremoClient implements ClientModInitializer {
    public static int hearts = 5;
    public static Map<UUID, Integer> allHearts = new HashMap<>();

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.DASHER, SkeletonRenderer::new);
        EntityRendererRegistry.register(ModEntities.HECHIZERA, WitchRenderer::new);
        EntityRendererRegistry.register(ModEntities.CAVE_BRUTE, ZombieRenderer::new);
        EntityRendererRegistry.register(ModEntities.BERSERKER_GOLEM, IronGolemRenderer::new);
        EntityRendererRegistry.register(ModEntities.SKELETON_LORD, SkeletonRenderer::new);
        EntityRendererRegistry.register(ModEntities.SKELETON_MINION, SkeletonRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(HeartSyncPayload.TYPE, (payload, context) -> {
            hearts = payload.hearts();
        });

        ClientPlayNetworking.registerGlobalReceiver(AllHeartsSyncPayload.TYPE, (payload, context) -> {
            allHearts = payload.hearts();
        });

        ClientPlayNetworking.registerGlobalReceiver(ItemActivationPayload.TYPE, (payload, context) -> {
            Minecraft.getInstance().gameRenderer.displayItemActivation(new ItemStack(ModItems.EXTREME_HEART));
        });
    }
}
