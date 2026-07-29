package com.bestiarymod.renderer.entity.renderer;

import com.bestiarymod.client.ModLayerDefinitions;
import com.bestiarymod.renderer.entity.model.WildfireEntityModel;
import com.bestiarymod.renderer.entity.state.WildfireRenderState;
import com.bestiarymod.entity.WildfireEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class WildfireEntityRenderer extends MobRenderer<WildfireEntity, WildfireRenderState, WildfireEntityModel> {
    private static final Identifier WILDFIRE_TEXTURE = Identifier.fromNamespaceAndPath("extremo", "textures/entity/wildfire/wildfire.png");

    public WildfireEntityRenderer(Context ctx) {
        super(ctx, new WildfireEntityModel(ctx.bakeLayer(ModLayerDefinitions.WILDFIRE)), 0.5F);
    }

    @Override
    public Identifier getTextureLocation(WildfireRenderState state) {
        return WILDFIRE_TEXTURE;
    }

    @Override
    public WildfireRenderState createRenderState() {
        return new WildfireRenderState();
    }

    @Override
    public void extractRenderState(WildfireEntity entity, WildfireRenderState state, float f) {
        super.extractRenderState(entity, state, f);
        state.wildfire = entity;
    }

    @Override
    protected void scale(WildfireRenderState state, PoseStack poseStack) {
        poseStack.scale(1.8F, 1.8F, 1.8F);
    }
}
