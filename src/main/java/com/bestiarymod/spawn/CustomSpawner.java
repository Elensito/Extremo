package com.bestiarymod.spawn;

import com.bestiarymod.Extremo;
import com.bestiarymod.spawn.SpawnConfigManager.SpawnEntryConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.*;

public class CustomSpawner {

    private static final Map<String, Integer> configTicks = new HashMap<>();

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                for (SpawnEntryConfig config : SpawnConfigManager.getAllConfigs()) {
                    if (!config.enabled) continue;

                    int ticks = configTicks.getOrDefault(config.mobId, 0) + 1;
                    configTicks.put(config.mobId, ticks);
                    if (ticks < config.cycleInterval) continue;
                    configTicks.put(config.mobId, 0);

                    trySpawn(player, config);
                }
            }
        });
        Extremo.LOGGER.info("[CustomSpawner] Inicializado");
    }

    private static void trySpawn(ServerPlayer player, SpawnEntryConfig config) {
        try {
            int roll = player.getRandom().nextInt(100);
            if (roll >= config.weight) return;

            ServerLevel level = (ServerLevel) player.level();

            int count = config.minGroup + player.getRandom().nextInt(config.maxGroup - config.minGroup + 1);
            var spawned = new ArrayList<net.minecraft.world.entity.Mob>();

            for (int i = 0; i < count; i++) {
                for (int attempt = 0; attempt < 12; attempt++) {
                    double angle = player.getRandom().nextDouble() * 2 * Math.PI;
                    double dist = config.spawnMinDistance + player.getRandom().nextDouble() * (config.spawnMaxDistance - config.spawnMinDistance);
                    int x = player.blockPosition().getX() + (int)(Math.cos(angle) * dist);
                    int z = player.blockPosition().getZ() + (int)(Math.sin(angle) * dist);

                    var cursor = new BlockPos.MutableBlockPos(x, level.getMaxY(), z);
                    boolean found = false;
                    while (cursor.getY() > level.getMinY()) {
                        var state = level.getBlockState(cursor);
                        var below = level.getBlockState(cursor.below());
                        if (state.isAir() && below.isSolid()) {
                            found = true;
                            break;
                        }
                        cursor.move(0, -1, 0);
                    }
                    if (!found) continue;

                    if (level.getMaxLocalRawBrightness(cursor) > 7) continue;
                    if (config.conditions != null && !SpawnConfigManager.checkConditions(level, cursor, config.conditions)) continue;

                    var mob = SpawnerRegistry.create(config.mobId, level);
                    if (mob == null) return;

                    mob.setPos(cursor.getX() + 0.5, cursor.getY(), cursor.getZ() + 0.5);
                    mob.finalizeSpawn(level, level.getCurrentDifficultyAt(cursor), net.minecraft.world.entity.EntitySpawnReason.NATURAL, null);

                    String name = switch (config.mobId) {
                        case "extremo:hechizera" -> "\u00a7dHechizera";
                        case "extremo:zombie_crecimiento" -> "\u00a7aZombie Crecimiento";
                        default -> "\u00a76Dasher";
                    };
                    mob.setCustomName(Component.literal(name));
                    mob.setCustomNameVisible(true);

                    level.addFreshEntity(mob);
                    spawned.add(mob);
                    break;
                }
            }

            if (!spawned.isEmpty()) {
                Extremo.LOGGER.info("[CustomSpawner] Spawneados {} {} (peso={}, ciclo={}t)",
                    spawned.size(), config.mobId, config.weight, config.cycleInterval);
            }
        } catch (Exception e) {
            Extremo.LOGGER.error("[CustomSpawner] Error con {}:", config.mobId, e);
        }
    }
}
