package com.bestiarymod.spawn;

import com.bestiarymod.Extremo;
import com.bestiarymod.entity.BerserkerGolem;
import com.bestiarymod.entity.CaveBrute;
import com.bestiarymod.entity.Hechizera;
import com.bestiarymod.entity.ModEntities;
import com.bestiarymod.entity.SkeletonDasher;
import com.bestiarymod.entity.SkeletonLord;
import com.bestiarymod.entity.SkeletonMinion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class SpawnerRegistry {

    private static final Map<String, BiFunction<EntityType<?>, ServerLevel, Mob>> factories = new HashMap<>();

    public static void register() {
        factories.put("extremo:dasher", (type, level) -> new SkeletonDasher(ModEntities.DASHER, level));
        factories.put("extremo:hechizera", (type, level) -> new Hechizera(ModEntities.HECHIZERA, level));
        factories.put("extremo:cave_brute", (type, level) -> new CaveBrute(ModEntities.CAVE_BRUTE, level));
        factories.put("extremo:berserker_golem", (type, level) -> new BerserkerGolem(ModEntities.BERSERKER_GOLEM, level));
        factories.put("extremo:skeleton_lord", (type, level) -> new SkeletonLord(ModEntities.SKELETON_LORD, level));
        factories.put("extremo:skeleton_minion", (type, level) -> new SkeletonMinion(ModEntities.SKELETON_MINION, level));
        Extremo.LOGGER.info("[SpawnerRegistry] Fábricas registradas: {}", factories.keySet());
    }

    public static Mob create(String mobId, ServerLevel level) {
        BiFunction<EntityType<?>, ServerLevel, Mob> factory = factories.get(mobId);
        if (factory != null) {
            EntityType<?> entityType = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                .get(net.minecraft.resources.Identifier.tryParse(mobId))
                .map(ref -> ref.value()).orElse(null);
            if (entityType != null) {
                return factory.apply(entityType, level);
            }
        }
        return null;
    }
}
