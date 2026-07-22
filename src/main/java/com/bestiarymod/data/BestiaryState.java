package com.bestiarymod.data;

import com.bestiarymod.Extremo;
import com.bestiarymod.config.BestiaryConfigManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BestiaryState {
    private static final Map<UUID, Map<String, Integer>> KILL_TRACKER = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, Set<Integer>>> CLAIMED_TRACKER = new ConcurrentHashMap<>();

    public static int addKill(UUID playerUuid, String mobId) {
        return KILL_TRACKER.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>())
                .merge(mobId, 1, Integer::sum);
    }

    public static int getKills(UUID playerUuid, String mobId) {
        Map<String, Integer> playerData = KILL_TRACKER.get(playerUuid);
        if (playerData == null) return 0;
        return playerData.getOrDefault(mobId, 0);
    }

    public static Map<String, Integer> getAllKills(UUID playerUuid) {
        return KILL_TRACKER.getOrDefault(playerUuid, Collections.emptyMap());
    }

    public static boolean hasUnclaimedRewards(UUID playerUuid, String mobId, int levelIndex) {
        int kills = getKills(playerUuid, mobId);
        var entry = BestiaryConfigManager.getEntry(mobId);
        if (entry == null || levelIndex >= entry.levels.size()) return false;
        int required = entry.levels.get(levelIndex).killCount;
        if (kills < required) return false;
        var claimed = getClaimedLevels(playerUuid, mobId);
        return !claimed.contains(levelIndex);
    }

    public static Set<Integer> getClaimedLevels(UUID playerUuid, String mobId) {
        return CLAIMED_TRACKER
                .computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(mobId, k -> ConcurrentHashMap.newKeySet());
    }

    public static void claimReward(UUID playerUuid, String mobId, int levelIndex) {
        getClaimedLevels(playerUuid, mobId).add(levelIndex);
    }

    public static void load(MinecraftServer server) {
        Path dataDir = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("bestiary");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {}

        Path killsFile = dataDir.resolve("kills.nbt");
        if (Files.exists(killsFile)) {
            try {
                CompoundTag tag = NbtIo.readCompressed(killsFile, NbtAccounter.unlimitedHeap());
                ListTag playersList = tag.getList("players").orElse(new ListTag());
                for (int i = 0; i < playersList.size(); i++) {
                    CompoundTag playerTag = playersList.getCompound(i).orElse(new CompoundTag());
                    long most = playerTag.getLongOr("uuidMost", 0);
                    long least = playerTag.getLongOr("uuidLeast", 0);
                    if (most == 0 && least == 0) continue;
                    UUID uuid = new UUID(most, least);
                    ListTag mobsList = playerTag.getList("mobs").orElse(new ListTag());
                    Map<String, Integer> mobMap = new ConcurrentHashMap<>();
                    for (int j = 0; j < mobsList.size(); j++) {
                        CompoundTag mobTag = mobsList.getCompound(j).orElse(new CompoundTag());
                        mobMap.put(mobTag.getString("mobId").orElse(""), mobTag.getInt("kills").orElse(0));
                    }
                    KILL_TRACKER.put(uuid, mobMap);
                }
            } catch (IOException e) {
                Extremo.LOGGER.error("Failed to load kills", e);
            }
        }

        Path claimedFile = dataDir.resolve("claimed.nbt");
        if (Files.exists(claimedFile)) {
            try {
                CompoundTag tag = NbtIo.readCompressed(claimedFile, NbtAccounter.unlimitedHeap());
                ListTag playersList = tag.getList("players").orElse(new ListTag());
                for (int i = 0; i < playersList.size(); i++) {
                    CompoundTag playerTag = playersList.getCompound(i).orElse(new CompoundTag());
                    long uuidMost = playerTag.getLongOr("uuidMost", 0);
                    long uuidLeast = playerTag.getLongOr("uuidLeast", 0);
                    UUID uuid = new UUID(uuidMost, uuidLeast);
                    if (uuidMost == 0 && uuidLeast == 0) continue;
                    ListTag mobsList = playerTag.getList("mobs").orElse(new ListTag());
                    Map<String, Set<Integer>> mobClaimed = new ConcurrentHashMap<>();
                    for (int j = 0; j < mobsList.size(); j++) {
                        CompoundTag mobTag = mobsList.getCompound(j).orElse(new CompoundTag());
                        Set<Integer> levels = ConcurrentHashMap.newKeySet();
                        ListTag levelsList = mobTag.getList("levels").orElse(new ListTag());
                        for (int k = 0; k < levelsList.size(); k++) {
                            levelsList.getInt(k).ifPresent(levels::add);
                        }
                        mobClaimed.put(mobTag.getString("mobId").orElse(""), levels);
                    }
                    CLAIMED_TRACKER.put(uuid, mobClaimed);
                }
            } catch (IOException e) {
                Extremo.LOGGER.error("Failed to load claimed rewards", e);
            }
        }

        Extremo.LOGGER.info("Bestiary data loaded for {} players", KILL_TRACKER.size());
    }

    public static void save(MinecraftServer server) {
        Path dataDir = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("bestiary");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {}

        CompoundTag killsTag = new CompoundTag();
        ListTag playersList = new ListTag();
        for (var entry : KILL_TRACKER.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putLong("uuidMost", entry.getKey().getMostSignificantBits());
            playerTag.putLong("uuidLeast", entry.getKey().getLeastSignificantBits());
            ListTag mobsList = new ListTag();
            for (var mobEntry : entry.getValue().entrySet()) {
                CompoundTag mobTag = new CompoundTag();
                mobTag.putString("mobId", mobEntry.getKey());
                mobTag.putInt("kills", mobEntry.getValue());
                mobsList.add(mobTag);
            }
            playerTag.put("mobs", mobsList);
            playersList.add(playerTag);
        }
        killsTag.put("players", playersList);
        try {
            NbtIo.writeCompressed(killsTag, dataDir.resolve("kills.nbt"));
        } catch (IOException e) {
            Extremo.LOGGER.error("Failed to save kills", e);
        }

        CompoundTag claimedTag = new CompoundTag();
        ListTag claimedPlayersList = new ListTag();
        for (var entry : CLAIMED_TRACKER.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putLong("uuidMost", entry.getKey().getMostSignificantBits());
            playerTag.putLong("uuidLeast", entry.getKey().getLeastSignificantBits());
            ListTag mobsList = new ListTag();
            for (var mobEntry : entry.getValue().entrySet()) {
                CompoundTag mobTag = new CompoundTag();
                mobTag.putString("mobId", mobEntry.getKey());
                ListTag levelsList = new ListTag();
                for (int level : mobEntry.getValue()) {
                    levelsList.add(IntTag.valueOf(level));
                }
                mobTag.put("levels", levelsList);
                mobsList.add(mobTag);
            }
            playerTag.put("mobs", mobsList);
            claimedPlayersList.add(playerTag);
        }
        claimedTag.put("players", claimedPlayersList);
        try {
            NbtIo.writeCompressed(claimedTag, dataDir.resolve("claimed.nbt"));
        } catch (IOException e) {
            Extremo.LOGGER.error("Failed to save claimed rewards", e);
        }
    }
}
