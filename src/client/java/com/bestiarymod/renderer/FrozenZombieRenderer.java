package com.bestiarymod.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

public class FrozenZombieRenderer extends ZombieRenderer {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("extremo", "textures/entity/frozen_zombie.png");

    public FrozenZombieRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState state) {
        return TEXTURE;
    }
}
