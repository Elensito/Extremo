package com.bestiarymod.mission;

import com.bestiarymod.Extremo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MissionState {
    private static final Map<UUID, Map<String, Integer>> PROGRESS = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, Boolean>> CLAIMED = new ConcurrentHashMap<>();
    private static final Map<String, Integer> CLAIM_COUNTS = new ConcurrentHashMap<>();

    public static int addProgress(UUID playerUuid, String questId) {
        return addProgress(playerUuid, questId, 1);
    }

    public static int addProgress(UUID playerUuid, String questId, int amount) {
        return PROGRESS.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>())
                .merge(questId, amount, Integer::sum);
    }

    public static int getProgress(UUID playerUuid, String questId) {
        return PROGRESS.getOrDefault(playerUuid, Collections.emptyMap()).getOrDefault(questId, 0);
    }

    public static boolean isCompleted(UUID playerUuid, String questId) {
        MissionEntry entry = MissionManager.getEntry(questId);
        if (entry == null) return false;
        return getProgress(playerUuid, questId) >= entry.amount;
    }

    public static boolean isClaimed(UUID playerUuid, String questId) {
        return CLAIMED.getOrDefault(playerUuid, Collections.emptyMap()).getOrDefault(questId, false);
    }

    public static int getClaimCount(String questId) {
        return CLAIM_COUNTS.getOrDefault(questId, 0);
    }

    public static boolean canClaim(UUID playerUuid, String questId) {
        if (isClaimed(playerUuid, questId)) return false;
        MissionEntry entry = MissionManager.getEntry(questId);
        if (entry == null) return false;
        if (entry.maxClaims > 0 && getClaimCount(questId) >= entry.maxClaims) return false;
        return true;
    }

    public static void claimReward(UUID playerUuid, String questId) {
        CLAIMED.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>()).put(questId, true);
        CLAIM_COUNTS.merge(questId, 1, Integer::sum);
    }

    public static void clearMissionData(String questId) {
        PROGRESS.values().forEach(m -> m.remove(questId));
        CLAIMED.values().forEach(m -> m.remove(questId));
        CLAIM_COUNTS.remove(questId);
    }

    public static void onPlayerKill(ServerPlayer player, String entityId) {
        for (MissionEntry entry : MissionManager.getAllEntries()) {
            if (!entry.type.equals("kill")) continue;
            if (!entry.target.equals(entityId)) continue;
            if (isClaimed(player.getUUID(), entry.id)) continue;
            int oldProgress = getProgress(player.getUUID(), entry.id);
            int newProgress = addProgress(player.getUUID(), entry.id);
            if (oldProgress < entry.amount && newProgress >= entry.amount) {
                player.sendSystemMessage(Component.literal("\u00a7a[Misiones] \u00a7f\u00a1Misi\u00f3n completada: " + entry.getDisplayName() + "! Usa /misiones para reclamar tu recompensa."));
            }
        }
    }

    public static void setProgress(UUID playerUuid, String questId, int amount) {
        PROGRESS.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>()).put(questId, amount);
    }

    private static int countInInventory(ServerPlayer player, Identifier itemId) {
        int total = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack slot = inv.getItem(i);
            if (!slot.isEmpty() && BuiltInRegistries.ITEM.getKey(slot.getItem()).equals(itemId)) {
                total += slot.getCount();
            }
        }
        return total;
    }

    public static void onPlayerCollect(ServerPlayer player, Item item, int count) {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        for (MissionEntry entry : MissionManager.getAllEntries()) {
            if (!entry.type.equals("collect")) continue;
            Identifier targetId = Identifier.tryParse(entry.target);
            if (targetId == null) continue;
            if (!targetId.equals(itemId)) continue;
            if (isClaimed(player.getUUID(), entry.id)) continue;
            int totalInInventory = countInInventory(player, targetId);
            int oldProgress = getProgress(player.getUUID(), entry.id);
            if (totalInInventory > oldProgress) {
                setProgress(player.getUUID(), entry.id, totalInInventory);
                if (oldProgress < entry.amount && totalInInventory >= entry.amount) {
                    player.sendSystemMessage(Component.literal("\u00a7a[Misiones] \u00a7f\u00a1Misi\u00f3n completada: " + entry.getDisplayName() + "! Usa /misiones para reclamar tu recompensa."));
                }
            }
        }
    }

    public static void refreshCollectProgress(ServerPlayer player) {
        for (MissionEntry entry : MissionManager.getAllEntries()) {
            if (!entry.type.equals("collect")) continue;
            if (isClaimed(player.getUUID(), entry.id)) continue;
            Identifier targetId = Identifier.tryParse(entry.target);
            if (targetId == null) continue;
            int totalInInventory = countInInventory(player, targetId);
            int oldProgress = getProgress(player.getUUID(), entry.id);
            if (totalInInventory > oldProgress) {
                setProgress(player.getUUID(), entry.id, totalInInventory);
                if (oldProgress < entry.amount && totalInInventory >= entry.amount) {
                    player.sendSystemMessage(Component.literal("\u00a7a[Misiones] \u00a7f\u00a1Misi\u00f3n completada: " + entry.getDisplayName() + "! Usa /misiones para reclamar tu recompensa."));
                }
            }
        }
    }

    public static Map<String, Integer> getAllProgress(UUID playerUuid) {
        return PROGRESS.getOrDefault(playerUuid, Collections.emptyMap());
    }

    public static void load(MinecraftServer server) {
        Path dataDir = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("extremo");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException ignored) {}

        Path progressFile = dataDir.resolve("missions_progress.nbt");
        if (Files.exists(progressFile)) {
            try {
                CompoundTag tag = NbtIo.readCompressed(progressFile, NbtAccounter.unlimitedHeap());
                ListTag playersList = tag.getList("players").orElse(new ListTag());
                for (int i = 0; i < playersList.size(); i++) {
                    CompoundTag pt = playersList.getCompound(i).orElse(new CompoundTag());
                    long most = pt.getLongOr("uuidMost", 0);
                    long least = pt.getLongOr("uuidLeast", 0);
                    if (most == 0 && least == 0) continue;
                    UUID uuid = new UUID(most, least);
                    ListTag questsList = pt.getList("quests").orElse(new ListTag());
                    Map<String, Integer> questMap = new ConcurrentHashMap<>();
                    for (int j = 0; j < questsList.size(); j++) {
                        CompoundTag qt = questsList.getCompound(j).orElse(new CompoundTag());
                        questMap.put(qt.getString("id").orElse(""), qt.getInt("progress").orElse(0));
                    }
                    PROGRESS.put(uuid, questMap);
                }
            } catch (IOException e) {
                Extremo.LOGGER.error("Failed to load mission progress", e);
            }
        }

        Path claimedFile = dataDir.resolve("missions_claimed.nbt");
        if (Files.exists(claimedFile)) {
            try {
                CompoundTag tag = NbtIo.readCompressed(claimedFile, NbtAccounter.unlimitedHeap());
                ListTag playersList = tag.getList("players").orElse(new ListTag());
                for (int i = 0; i < playersList.size(); i++) {
                    CompoundTag pt = playersList.getCompound(i).orElse(new CompoundTag());
                    long most = pt.getLongOr("uuidMost", 0);
                    long least = pt.getLongOr("uuidLeast", 0);
                    if (most == 0 && least == 0) continue;
                    UUID uuid = new UUID(most, least);
                    ListTag questsList = pt.getList("quests").orElse(new ListTag());
                    Map<String, Boolean> questMap = new ConcurrentHashMap<>();
                    for (int j = 0; j < questsList.size(); j++) {
                        questMap.put(questsList.getString(j).orElse(""), true);
                    }
                    CLAIMED.put(uuid, questMap);
                }
                ListTag countsList = tag.getList("claim_counts").orElse(new ListTag());
                for (int i = 0; i < countsList.size(); i++) {
                    CompoundTag ct = countsList.getCompound(i).orElse(new CompoundTag());
                    CLAIM_COUNTS.put(ct.getString("id").orElse(""), ct.getInt("count").orElse(0));
                }
            } catch (IOException e) {
                Extremo.LOGGER.error("Failed to load mission claimed", e);
            }
        }

        Extremo.LOGGER.info("Mission data loaded for {} players", PROGRESS.size());
    }

    public static void save(MinecraftServer server) {
        Path dataDir = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("extremo");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException ignored) {}

        CompoundTag progressTag = new CompoundTag();
        ListTag playersList = new ListTag();
        for (var entry : PROGRESS.entrySet()) {
            CompoundTag pt = new CompoundTag();
            pt.putLong("uuidMost", entry.getKey().getMostSignificantBits());
            pt.putLong("uuidLeast", entry.getKey().getLeastSignificantBits());
            ListTag questsList = new ListTag();
            for (var qe : entry.getValue().entrySet()) {
                CompoundTag qt = new CompoundTag();
                qt.putString("id", qe.getKey());
                qt.putInt("progress", qe.getValue());
                questsList.add(qt);
            }
            pt.put("quests", questsList);
            playersList.add(pt);
        }
        progressTag.put("players", playersList);
        try {
            NbtIo.writeCompressed(progressTag, dataDir.resolve("missions_progress.nbt"));
        } catch (IOException e) {
            Extremo.LOGGER.error("Failed to save mission progress", e);
        }

        CompoundTag claimedTag = new CompoundTag();
        ListTag claimedPlayersList = new ListTag();
        for (var entry : CLAIMED.entrySet()) {
            CompoundTag pt = new CompoundTag();
            pt.putLong("uuidMost", entry.getKey().getMostSignificantBits());
            pt.putLong("uuidLeast", entry.getKey().getLeastSignificantBits());
            ListTag questsList = new ListTag();
            for (String qid : entry.getValue().keySet()) {
                questsList.add(StringTag.valueOf(qid));
            }
            pt.put("quests", questsList);
            claimedPlayersList.add(pt);
        }
        claimedTag.put("players", claimedPlayersList);
        ListTag countsList = new ListTag();
        for (var ce : CLAIM_COUNTS.entrySet()) {
            CompoundTag ct = new CompoundTag();
            ct.putString("id", ce.getKey());
            ct.putInt("count", ce.getValue());
            countsList.add(ct);
        }
        claimedTag.put("claim_counts", countsList);
        try {
            NbtIo.writeCompressed(claimedTag, dataDir.resolve("missions_claimed.nbt"));
        } catch (IOException e) {
            Extremo.LOGGER.error("Failed to save mission claimed", e);
        }
    }
}
