package com.bestiarymod.client;

import com.bestiarymod.renderer.entity.model.WildfireEntityModel;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

public class ModLayerDefinitions {
    public static final ModelLayerLocation WILDFIRE = new ModelLayerLocation(
        Identifier.fromNamespaceAndPath("extremo", "wildfire"), "main");

    public static void register() {
        ModelLayerRegistry.registerModelLayer(WILDFIRE, WildfireEntityModel::getTexturedModelData);
    }
}
