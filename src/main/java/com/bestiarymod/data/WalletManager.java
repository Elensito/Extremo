package com.bestiarymod.data;

import com.bestiarymod.Extremo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WalletManager {
    private static final Map<UUID, Long> WALLETS = new ConcurrentHashMap<>();

    public static long getBalance(UUID uuid) {
        return WALLETS.getOrDefault(uuid, 0L);
    }

    public static void addCoins(UUID uuid, long amount) {
        WALLETS.merge(uuid, amount, Long::sum);
    }

    public static boolean removeCoins(UUID uuid, long amount) {
        long current = WALLETS.getOrDefault(uuid, 0L);
        if (current < amount) return false;
        if (current == amount) {
            WALLETS.put(uuid, 0L);
        } else {
            WALLETS.put(uuid, current - amount);
        }
        return true;
    }

    public static boolean hasWallet(UUID uuid) {
        return WALLETS.containsKey(uuid);
    }

    public static void unlock(UUID uuid) {
        WALLETS.putIfAbsent(uuid, 0L);
    }

    public static void load(MinecraftServer server) {
        Path dataDir = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("extremo");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {}

        Path file = dataDir.resolve("wallets.nbt");
        if (!Files.exists(file)) return;
        try {
            CompoundTag tag = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
            ListTag list = tag.getList("wallets").orElse(new ListTag());
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i).orElse(new CompoundTag());
                long most = entry.getLongOr("uuidMost", 0);
                long least = entry.getLongOr("uuidLeast", 0);
                long coins = entry.getLongOr("coins", 0);
                if (most != 0 || least != 0) {
                    WALLETS.put(new UUID(most, least), coins);
                }
            }
            Extremo.LOGGER.info("Loaded {} wallets", WALLETS.size());
        } catch (IOException e) {
            Extremo.LOGGER.error("Failed to load wallets", e);
        }
    }

    public static void save(MinecraftServer server) {
        Path dataDir = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("extremo");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {}

        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (var entry : WALLETS.entrySet()) {
            CompoundTag walletEntry = new CompoundTag();
            walletEntry.putLong("uuidMost", entry.getKey().getMostSignificantBits());
            walletEntry.putLong("uuidLeast", entry.getKey().getLeastSignificantBits());
            walletEntry.putLong("coins", entry.getValue());
            list.add(walletEntry);
        }
        tag.put("wallets", list);
        try {
            NbtIo.writeCompressed(tag, dataDir.resolve("wallets.nbt"));
            Extremo.LOGGER.info("Saved {} wallets", WALLETS.size());
        } catch (IOException e) {
            Extremo.LOGGER.error("Failed to save wallets", e);
        }
    }
}
