package com.bestiarymod.handler;

import com.bestiarymod.Extremo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.LevelResource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AccessoryItemState {
    private static final Map<UUID, List<ItemStack>> ACCESSORY_MAP = new ConcurrentHashMap<>();

    public static List<ItemStack> getItems(UUID playerUuid) {
        return ACCESSORY_MAP.computeIfAbsent(playerUuid, k -> {
            List<ItemStack> list = new ArrayList<>();
            for (int i = 0; i < 54; i++) list.add(ItemStack.EMPTY);
            return list;
        });
    }

    public static void setItems(UUID playerUuid, List<ItemStack> items) {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < Math.min(items.size(), 54); i++) {
            list.add(items.get(i).copy());
        }
        while (list.size() < 54) {
            list.add(ItemStack.EMPTY);
        }
        ACCESSORY_MAP.put(playerUuid, list);
    }

    public static void load(MinecraftServer server) {
        Path dataDir = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("extremo");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {}

        Path file = dataDir.resolve("accessories.nbt");
        if (!Files.exists(file)) return;

        try {
            CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
            ListTag playersList = root.getList("players").orElse(new ListTag());
            for (int i = 0; i < playersList.size(); i++) {
                CompoundTag playerTag = playersList.getCompound(i).orElse(new CompoundTag());
                long most = playerTag.getLongOr("uuidMost", 0);
                long least = playerTag.getLongOr("uuidLeast", 0);
                if (most == 0 && least == 0) continue;
                UUID uuid = new UUID(most, least);
                ListTag itemsList = playerTag.getList("items").orElse(new ListTag());
                List<ItemStack> items = new ArrayList<>();
                for (int j = 0; j < 54; j++) {
                    if (j < itemsList.size()) {
                        CompoundTag itemTag = itemsList.getCompound(j).orElse(new CompoundTag());
                        items.add(deserializeItem(itemTag));
                    } else {
                        items.add(ItemStack.EMPTY);
                    }
                }
                ACCESSORY_MAP.put(uuid, items);
            }
            Extremo.LOGGER.info("Loaded accessory items for {} players", ACCESSORY_MAP.size());
        } catch (IOException e) {
            Extremo.LOGGER.error("Failed to load accessory items", e);
        }
    }

    public static void save(MinecraftServer server) {
        Path dataDir = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("extremo");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {}

        CompoundTag root = new CompoundTag();
        ListTag playersList = new ListTag();
        for (var entry : ACCESSORY_MAP.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putLong("uuidMost", entry.getKey().getMostSignificantBits());
            playerTag.putLong("uuidLeast", entry.getKey().getLeastSignificantBits());
            ListTag itemsList = new ListTag();
            for (ItemStack stack : entry.getValue()) {
                itemsList.add(serializeItem(stack));
            }
            playerTag.put("items", itemsList);
            playersList.add(playerTag);
        }
        root.put("players", playersList);
        try {
            NbtIo.writeCompressed(root, dataDir.resolve("accessories.nbt"));
        } catch (IOException e) {
            Extremo.LOGGER.error("Failed to save accessory items", e);
        }
    }

    private static CompoundTag serializeItem(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        if (!stack.isEmpty()) {
            tag.putString("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            tag.putInt("count", stack.getCount());
        }
        return tag;
    }

    private static ItemStack deserializeItem(CompoundTag tag) {
        String id = tag.getString("id").orElse("minecraft:air");
        int count = tag.getInt("count").orElse(1);
        if (id.equals("minecraft:air") || count <= 0) return ItemStack.EMPTY;
        ItemStack stack = BuiltInRegistries.ITEM.get(Identifier.tryParse(id))
            .map(ref -> new ItemStack(ref.value(), count))
            .orElse(ItemStack.EMPTY);
        return stack;
    }
}
