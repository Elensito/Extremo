package com.bestiarymod.handler;

import com.bestiarymod.config.BestiaryConfigManager;
import com.bestiarymod.config.BestiaryEntry;
import com.bestiarymod.data.BestiaryState;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class MobKillHandler {
    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity.level().isClientSide()) return;

            var source = damageSource.getEntity();
            if (!(source instanceof ServerPlayer player)) return;

            if (entity instanceof Player) return;

            String mobId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
            if (mobId.isEmpty()) return;

            BestiaryEntry entry = BestiaryConfigManager.getEntry(mobId);
            if (entry == null) return;

            int oldKills = BestiaryState.getKills(player.getUUID(), mobId);
            int newKills = BestiaryState.addKill(player.getUUID(), mobId);

            for (int i = 0; i < entry.levels.size(); i++) {
                int threshold = entry.levels.get(i).killCount;
                if (oldKills < threshold && newKills >= threshold) {
                    if (!BestiaryState.getClaimedLevels(player.getUUID(), mobId).contains(i)) {
                        String playerName = player.getName().getString();
                        int levelNum = i + 1;
                        Component mobDisplay = entity.getType().getDescription();
                        Component msg = Component.literal("§6" + playerName + "§e ha llegado al nivel §6" + levelNum + "§e del bestiario de §6")
                                .append(mobDisplay);
                        com.bestiarymod.Extremo.currentServer.getPlayerList().broadcastSystemMessage(msg, false);
                    }
                }
            }
        });
    }
}
