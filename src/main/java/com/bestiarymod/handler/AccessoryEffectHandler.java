package com.bestiarymod.handler;

import com.bestiarymod.config.BestiaryConfigManager;
import com.bestiarymod.config.BestiaryEntry;
import com.bestiarymod.data.BestiaryState;
import com.bestiarymod.config.BestiaryEntry;
import com.bestiarymod.data.BestiaryState;
import com.bestiarymod.item.HunterTalismanItem;
import com.bestiarymod.item.LenteVisionItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AccessoryEffectHandler {
    private static final int TICK_INTERVAL = 20;
    private static final double HUNTER_RANGE = 48.0;
    private static final Map<UUID, Integer> PREVIOUS_GLOWING = new ConcurrentHashMap<>();

    public static void tick(ServerLevel level) {
        if (level.getServer() == null) return;
        int tickCount = level.getServer().getTickCount();
        if (tickCount % TICK_INTERVAL != 0) return;

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level() != level) continue;
            List<ItemStack> accessories = AccessoryItemState.getItems(player.getUUID());

            boolean hasHunterTalisman = false;
            boolean hasLenteVision = false;

            for (ItemStack stack : accessories) {
                if (stack.getItem() instanceof HunterTalismanItem) hasHunterTalisman = true;
                if (stack.getItem() instanceof LenteVisionItem) hasLenteVision = true;
            }

            if (hasHunterTalisman) {
                applyHunterTalisman(player, level);
            } else {
                clearHunterTalisman(player);
            }

            if (hasLenteVision) {
                applyLenteVision(player);
            }
        }
    }

    private static void applyHunterTalisman(ServerPlayer player, ServerLevel level) {
        UUID playerId = player.getUUID();
        AABB aabb = player.getBoundingBox().inflate(HUNTER_RANGE);
        List<Mob> mobs = level.getEntitiesOfClass(Mob.class, aabb, mob -> {
            if (!mob.isAlive()) return false;
            String id = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();
            BestiaryEntry entry = BestiaryConfigManager.getEntry(id);
            if (entry == null) return false;
            int claimed = BestiaryState.getClaimedLevels(player.getUUID(), id).size();
            return claimed < entry.levels.size();
        });

        if (mobs.isEmpty()) {
            clearHunterTalisman(player);
            return;
        }

        Mob closest = mobs.get(0);
        double closestDist = player.distanceToSqr(closest);
        for (int i = 1; i < mobs.size(); i++) {
            double d = player.distanceToSqr(mobs.get(i));
            if (d < closestDist) {
                closestDist = d;
                closest = mobs.get(i);
            }
        }

        Integer prevId = PREVIOUS_GLOWING.get(playerId);
        if (prevId != null && prevId != closest.getId()) {
            Mob prevMob = (Mob) level.getEntity(prevId);
            if (prevMob != null) {
                prevMob.setGlowingTag(false);
            }
        }

        if (!closest.hasGlowingTag()) {
            closest.setGlowingTag(true);
        }
        PREVIOUS_GLOWING.put(playerId, closest.getId());
    }

    private static void clearHunterTalisman(ServerPlayer player) {
        UUID playerId = player.getUUID();
        Integer prevId = PREVIOUS_GLOWING.get(playerId);
        if (prevId != null) {
            if (player.level() instanceof ServerLevel sl) {
                Mob prevMob = (Mob) sl.getEntity(prevId);
                if (prevMob != null) {
                    prevMob.setGlowingTag(false);
                }
            }
            PREVIOUS_GLOWING.remove(playerId);
        }
    }

    private static void applyLenteVision(ServerPlayer player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(100.0));
        EntityHitResult hitResult = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
            player, eyePos, endPos,
            player.getBoundingBox().expandTowards(lookVec.scale(100.0)).inflate(1.0),
            entity -> entity instanceof LivingEntity && entity.isAlive(),
            100.0
        );
        if (hitResult == null) return;
        Entity target = hitResult.getEntity();
        if (target instanceof LivingEntity living && living.isAlive()) {
            float maxHp = living.getMaxHealth();
            float currentHp = living.getHealth();
            int pct = Math.round((currentHp / maxHp) * 100);
            Component name = living.getDisplayName();
            player.connection.send(new ClientboundSetActionBarTextPacket(
                Component.literal("\u00a7e" + name.getString() + " \u00a77- \u00a7f" + pct + "% \u00a7c\u2764")
            ));
        }
    }

    public static boolean hasAmuletoSangre(ServerPlayer player) {
        for (ItemStack stack : AccessoryItemState.getItems(player.getUUID())) {
            if (stack.getItem() instanceof com.bestiarymod.item.VampireTalismanItem) return true;
        }
        return false;
    }

    public static boolean hasAnilloAlquimista(ServerPlayer player) {
        for (ItemStack stack : AccessoryItemState.getItems(player.getUUID())) {
            if (stack.getItem() instanceof com.bestiarymod.item.WitchTalismanItem) return true;
        }
        return false;
    }


}
