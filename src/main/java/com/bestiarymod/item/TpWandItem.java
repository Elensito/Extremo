package com.bestiarymod.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.MutableComponent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TpWandItem extends Item {
    private static final int PINK = 0xFF69B4;
    private static final int GOLD = 0xFFAA00;
    private static final Set<ServerPlayer> fallImmunity = new HashSet<>();

    public static void registerTickHandler() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (fallImmunity.isEmpty()) return;
            Iterator<ServerPlayer> it = fallImmunity.iterator();
            while (it.hasNext()) {
                ServerPlayer p = it.next();
                if (!p.isAlive() || p.onGround()) {
                    it.remove();
                    continue;
                }
                p.fallDistance = 0;
            }
        });
    }

    private final int range;
    private final int cooldownTicks;
    private final Component displayName;

    public TpWandItem(Properties properties, int range, int cooldownTicks, int starCount) {
        super(properties);
        this.range = range;
        this.cooldownTicks = cooldownTicks;

        MutableComponent name = Component.literal("Cetro Dimensional").withStyle(style -> style.withColor(PINK));
        if (starCount > 0) {
            StringBuilder sb = new StringBuilder(" ");
            for (int i = 0; i < starCount; i++) sb.append("\u272A");
            name.append(Component.literal(sb.toString()).withStyle(style -> style.withColor(GOLD)));
        }
        this.displayName = name;
    }

    public int getRange() {
        return range;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    @Override
    public Component getName(ItemStack stack) {
        return displayName;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        Vec3 look = player.getLookAngle();
        Vec3 pos = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 target = pos.add(look.scale(range));

        BlockPos feetPos = BlockPos.containing(target.x, target.y, target.z);
        BlockPos headPos = feetPos.above();

        BlockState feetBlock = level.getBlockState(feetPos);
        BlockState headBlock = level.getBlockState(headPos);

        if (!feetBlock.isAir() && !feetBlock.canBeReplaced()) {
            return InteractionResult.FAIL;
        }
        if (!headBlock.isAir() && !headBlock.canBeReplaced()) {
            return InteractionResult.FAIL;
        }

        if (player instanceof ServerPlayer sp) {
            sp.teleportTo(target.x, target.y, target.z);
        } else {
            player.setPos(target.x, target.y, target.z);
        }
        player.resetFallDistance();
        if (player instanceof ServerPlayer sp && !sp.onGround()) {
            fallImmunity.add(sp);
        }
        player.getCooldowns().addCooldown(stack, cooldownTicks);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.WITCH, target.x, target.y + 1, target.z, 12, 0.4, 0.4, 0.4, 0.06);
            serverLevel.playSound(null, target.x, target.y, target.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.8f);
        }

        return InteractionResult.SUCCESS;
    }
}
