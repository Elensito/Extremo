package com.bestiarymod;

import com.bestiarymod.entity.ModEntities;
import com.bestiarymod.network.HeartSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.WitchRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;

public class ExtremoClient implements ClientModInitializer {
    public static int hearts = 5;

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.DASHER, SkeletonRenderer::new);
        EntityRendererRegistry.register(ModEntities.HECHIZERA, WitchRenderer::new);
        EntityRendererRegistry.register(ModEntities.CAVE_BRUTE, ZombieRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(HeartSyncPayload.TYPE, (payload, context) -> {
            hearts = payload.hearts();
        });
    }
}
