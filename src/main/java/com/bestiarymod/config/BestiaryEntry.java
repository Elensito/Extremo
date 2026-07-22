package com.bestiarymod.config;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BestiaryEntry {
    public String mobId;
    public List<BestiaryLevel> levels;

    public BestiaryEntry() {
        this.levels = new ArrayList<>();
    }

    public BestiaryEntry(String mobId) {
        this.mobId = mobId;
        this.levels = new ArrayList<>();
    }

    public static class BestiaryLevel {
        public int killCount;
        public int xpReward;
        public List<RewardItem> itemRewards;

        public BestiaryLevel() {
            this.itemRewards = new ArrayList<>();
        }

        public BestiaryLevel(int killCount, int xpReward) {
            this.killCount = killCount;
            this.xpReward = xpReward;
            this.itemRewards = new ArrayList<>();
        }
    }

    public static class RewardItem {
        public String itemId;
        public int count;
        public boolean enchanted;

        public RewardItem() {}

        public RewardItem(String itemId, int count, boolean enchanted) {
            this.itemId = itemId;
            this.count = count;
            this.enchanted = enchanted;
        }
    }

    public Map<String, Object> toYaml() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("mob_id", mobId);
        List<Map<String, Object>> levelsList = new ArrayList<>();
        for (BestiaryLevel level : levels) {
            Map<String, Object> levelMap = new LinkedHashMap<>();
            levelMap.put("kill_count", level.killCount);
            levelMap.put("xp_reward", level.xpReward);
            List<Map<String, Object>> itemsList = new ArrayList<>();
            for (RewardItem item : level.itemRewards) {
                Map<String, Object> itemMap = new LinkedHashMap<>();
                itemMap.put("item_id", item.itemId);
                itemMap.put("count", item.count);
                itemMap.put("enchanted", item.enchanted);
                itemsList.add(itemMap);
            }
            levelMap.put("item_rewards", itemsList);
            levelsList.add(levelMap);
        }
        map.put("levels", levelsList);
        return map;
    }

    public static BestiaryEntry fromYaml(Map<String, Object> data) {
        BestiaryEntry entry = new BestiaryEntry();
        entry.mobId = (String) data.get("mob_id");
        List<?> levelsList = (List<?>) data.get("levels");
        if (levelsList != null) {
            for (Object levelObj : levelsList) {
                @SuppressWarnings("unchecked")
                Map<String, Object> levelMap = (Map<String, Object>) levelObj;
                BestiaryLevel level = new BestiaryLevel();
                level.killCount = ((Number) levelMap.get("kill_count")).intValue();
                level.xpReward = ((Number) levelMap.get("xp_reward")).intValue();
                List<?> itemsList = (List<?>) levelMap.get("item_rewards");
                if (itemsList != null) {
                    for (Object itemObj : itemsList) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> itemMap = (Map<String, Object>) itemObj;
                        RewardItem ri = new RewardItem();
                        ri.itemId = (String) itemMap.get("item_id");
                        ri.count = ((Number) itemMap.get("count")).intValue();
                        ri.enchanted = (Boolean) itemMap.get("enchanted");
                        level.itemRewards.add(ri);
                    }
                }
                entry.levels.add(level);
            }
        }
        return entry;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("mobId", mobId);
        ListTag levelsList = new ListTag();
        for (BestiaryLevel level : levels) {
            CompoundTag levelTag = new CompoundTag();
            levelTag.putInt("killCount", level.killCount);
            levelTag.putInt("xpReward", level.xpReward);
            ListTag itemsList = new ListTag();
            for (RewardItem item : level.itemRewards) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putString("itemId", item.itemId);
                itemTag.putInt("count", item.count);
                itemTag.putBoolean("enchanted", item.enchanted);
                itemsList.add(itemTag);
            }
            levelTag.put("itemRewards", itemsList);
            levelsList.add(levelTag);
        }
        tag.put("levels", levelsList);
        return tag;
    }

    public static BestiaryEntry fromNbt(CompoundTag tag) {
        BestiaryEntry entry = new BestiaryEntry();
        entry.mobId = tag.getString("mobId").orElse("");
        ListTag levelsList = tag.getList("levels").orElse(new ListTag());
        for (int i = 0; i < levelsList.size(); i++) {
            CompoundTag levelTag = levelsList.getCompound(i).orElse(new CompoundTag());
            BestiaryLevel level = new BestiaryLevel();
            level.killCount = levelTag.getInt("killCount").orElse(0);
            level.xpReward = levelTag.getInt("xpReward").orElse(0);
            ListTag itemsList = levelTag.getList("itemRewards").orElse(new ListTag());
            for (int j = 0; j < itemsList.size(); j++) {
                CompoundTag itemTag = itemsList.getCompound(j).orElse(new CompoundTag());
                RewardItem ri = new RewardItem();
                ri.itemId = itemTag.getString("itemId").orElse("");
                ri.count = itemTag.getInt("count").orElse(0);
                ri.enchanted = itemTag.getBoolean("enchanted").orElse(false);
                level.itemRewards.add(ri);
            }
            entry.levels.add(level);
        }
        return entry;
    }

    public ItemStack getDisplayItem() {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(Identifier.tryParse(mobId)).map(ref -> ref.value()).orElse(null);
        if (type != null) {
            ItemStack egg = new ItemStack(Items.ZOMBIE_SPAWN_EGG);
            try {
                var eggItem = BuiltInRegistries.ITEM.get(
                    Identifier.fromNamespaceAndPath("minecraft", mobId.replace("minecraft:", "") + "_spawn_egg")
                ).map(ref -> ref.value()).orElse(Items.AIR);
                if (eggItem != Items.AIR) {
                    egg = new ItemStack(eggItem);
                }
            } catch (Exception e) {}
            return egg;
        }
        return new ItemStack(Items.SPAWNER);
    }
}
