package com.bestiarymod;

import com.bestiarymod.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.WitchRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;

public class ExtremoClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.DASHER, SkeletonRenderer::new);
        EntityRendererRegistry.register(ModEntities.HECHIZERA, WitchRenderer::new);
        EntityRendererRegistry.register(ModEntities.CAVE_BRUTE, ZombieRenderer::new);
    }
}
