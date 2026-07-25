package com.bestiarymod.mission;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import java.util.*;

public class MissionEntry {
    public String id;
    public String type;
    public String target;
    public int amount;
    public int xpReward;
    public int coinReward;
    public long expiresAt; // epoch millis, 0 = no expiry
    public int maxClaims; // 0 = unlimited
    public String iconItem; // item ID for GUI display icon
    public List<ItemReward> itemRewards = new ArrayList<>();

    public boolean isExpired() {
        return expiresAt > 0 && System.currentTimeMillis() > expiresAt;
    }

    public static class ItemReward {
        public String item;
        public int count;
        public double chance = 1.0;
    }

    @SuppressWarnings("unchecked")
    public static MissionEntry fromYaml(Map<String, Object> data) {
        MissionEntry e = new MissionEntry();
        e.id = (String) data.get("id");
        e.type = (String) data.get("type");
        e.target = (String) data.get("target");
        e.amount = data.get("amount") instanceof Number n ? n.intValue() : 0;
        e.xpReward = data.get("xp") instanceof Number n ? n.intValue() : 0;
        e.coinReward = data.get("coins") instanceof Number n ? n.intValue() : 0;
        e.expiresAt = data.get("expires_at") instanceof Number n ? n.longValue() : 0;
        e.maxClaims = data.get("max_claims") instanceof Number n ? n.intValue() : 0;
        e.iconItem = (String) data.get("icon_item");
        if (data.containsKey("items")) {
            for (Map<String, Object> ri : (List<Map<String, Object>>) data.get("items")) {
                ItemReward ir = new ItemReward();
                ir.item = (String) ri.get("item");
                ir.count = ri.get("count") instanceof Number n ? n.intValue() : 1;
                ir.chance = ri.containsKey("chance") && ri.get("chance") instanceof Number n ? n.doubleValue() : 1.0;
                e.itemRewards.add(ir);
            }
        }
        return e;
    }

    public Map<String, Object> toYaml() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("type", type);
        data.put("target", target);
        data.put("amount", amount);
        if (xpReward > 0) data.put("xp", xpReward);
        if (coinReward > 0) data.put("coins", coinReward);
        if (expiresAt > 0) data.put("expires_at", expiresAt);
        if (maxClaims > 0) data.put("max_claims", maxClaims);
        if (iconItem != null && !iconItem.isEmpty()) data.put("icon_item", iconItem);
        if (!itemRewards.isEmpty()) {
            List<Map<String, Object>> items = new ArrayList<>();
            for (ItemReward ir : itemRewards) {
                Map<String, Object> ri = new LinkedHashMap<>();
                ri.put("item", ir.item);
                ri.put("count", ir.count);
                if (ir.chance < 1.0) ri.put("chance", ir.chance);
                items.add(ri);
            }
            data.put("items", items);
        }
        return data;
    }

    public String getDisplayName() {
        String name = type.equals("kill") ? "Matar " : "Obtener ";
        String n = id.replace("_", " ");
        String[] parts = n.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
        }
        return name + sb.toString().trim();
    }

    public String getTargetDisplay() {
        Identifier id = Identifier.tryParse(target);
        if (id == null) return target;
        if (type.equals("kill")) {
            return BuiltInRegistries.ENTITY_TYPE.get(id)
                    .map(ref -> Component.translatable(ref.value().getDescriptionId()).getString())
                    .orElse(target);
        } else {
            return BuiltInRegistries.ITEM.get(id)
                    .map(ref -> Component.translatable(ref.value().getDescriptionId()).getString())
                    .orElse(target);
        }
    }
}
