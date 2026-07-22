package com.bestiarymod.mixin;

import com.bestiarymod.Extremo;
import com.bestiarymod.entity.ModEntities;
import com.bestiarymod.entity.SkeletonDasher;
import com.bestiarymod.spawn.SpawnConfigManager;
import com.bestiarymod.spawn.SpawnConfigManager.SpawnEntryConfig;
import com.bestiarymod.spawn.SpawnConfigManager.SpawnConditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnPlacements.class)
public class SpawnPlacementsMixin {

    private static int mixinCallCount = 0;

    @Inject(method = "checkSpawnRules", at = @At("HEAD"), cancellable = true)
    private static <T extends Entity> void checkCustomSpawnRules(
            EntityType<T> entityType,
            ServerLevelAccessor level,
            EntitySpawnReason spawnReason,
            BlockPos pos,
            RandomSource random,
            CallbackInfoReturnable<Boolean> cir) {

        mixinCallCount++;
        if (mixinCallCount <= 50 || mixinCallCount % 100 == 0) {
            Extremo.LOGGER.info("[SpawnMixin] Called #{} — entity={}, reason={}, pos={}",
                    mixinCallCount, BuiltInRegistries.ENTITY_TYPE.getKey(entityType), spawnReason, pos);
        }

        if (spawnReason != EntitySpawnReason.NATURAL && spawnReason != EntitySpawnReason.PATROL) {
            return;
        }

        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        if (entityId == null) {
            Extremo.LOGGER.warn("[SpawnMixin] entityId is null for entity class={}", entityType.getClass().getName());
            return;
        }

        SpawnEntryConfig config = SpawnConfigManager.getConfig(entityId.toString());
        if (config == null) {
            return;
        }

        Extremo.LOGGER.info("[SpawnMixin] Config found for {} at pos={}, checking conditions...", entityId, pos);

        if (mixinCallCount <= 5) {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            StringBuilder sb = new StringBuilder("[SpawnMixin] Stack trace for ");
            sb.append(entityId).append(" (call #").append(mixinCallCount).append("):");
            for (int i = 2; i < Math.min(stack.length, 12); i++) {
                sb.append("\n  at ").append(stack[i].getClassName()).append(".").append(stack[i].getMethodName()).append("(").append(stack[i].getFileName()).append(":").append(stack[i].getLineNumber()).append(")");
            }
            Extremo.LOGGER.info(sb.toString());
        }

        if (config.conditions == null) {
            Extremo.LOGGER.info("[SpawnMixin] No conditions for {}, allowing spawn", entityId);
            return;
        }

        SpawnConditions c = config.conditions;
        boolean result = SpawnConfigManager.checkConditions(level, pos, c);
        Extremo.LOGGER.info("[SpawnMixin] checkConditions for {} result={} (biomes={}, dims={}, height={}-{}, blocks={})",
                entityId, result, c.biomes, c.dimensions, c.heightMin, c.heightMax, c.spawnBlocks);

        if (result) {
            ServerLevel serverLevel = level.getLevel();
            try {
                var mob = new SkeletonDasher(ModEntities.DASHER, serverLevel);
                mob.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                mob.finalizeSpawn(level, serverLevel.getCurrentDifficultyAt(pos), spawnReason, null);
                serverLevel.addFreshEntity(mob);
            } catch (Exception e) {
                Extremo.LOGGER.error("[SpawnMixin] Force spawn failed for {}", entityId, e);
            }
            cir.setReturnValue(false);
        }
    }
}
