package com.bestiarymod.entity;

import com.bestiarymod.Extremo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.Registry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public class ModEntities {
    public static final EntityType<CaveBrute> CAVE_BRUTE = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "cave_brute"),
        EntityType.Builder.<CaveBrute>of(CaveBrute::new, MobCategory.MONSTER)
            .sized(0.6f, 1.95f)
            .build(ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(),
                Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "cave_brute")))
    );

    public static final EntityType<SkeletonDasher> DASHER = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "dasher"),
        EntityType.Builder.<SkeletonDasher>of(SkeletonDasher::new, MobCategory.MONSTER)
            .sized(0.6f, 1.99f)
            .build(ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(),
                Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "dasher")))
    );

    public static final EntityType<Hechizera> HECHIZERA = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "hechizera"),
        EntityType.Builder.<Hechizera>of(Hechizera::new, MobCategory.MONSTER)
            .sized(0.6f, 1.95f)
            .build(ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(),
                Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "hechizera")))
    );

    public static void register() {
        FabricDefaultAttributeRegistry.register(DASHER, AbstractSkeleton.createAttributes().add(Attributes.MAX_HEALTH, 50.0));
        FabricDefaultAttributeRegistry.register(HECHIZERA, Hechizera.createHechizeraAttributes());
        FabricDefaultAttributeRegistry.register(CAVE_BRUTE, CaveBrute.createAttributes());
        SpawnPlacements.register(DASHER, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, levelAccessor, reason, pos, randomSource) -> true);
        Extremo.LOGGER.info("Registered custom entities");
    }
}
