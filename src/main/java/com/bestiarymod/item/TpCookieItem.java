package com.bestiarymod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import java.util.Set;
import java.util.function.Consumer;

public class TpCookieItem extends Item {
    private static final Component NAME = Component.literal("\u00a7eGalleta On\u00edrica");

    public TpCookieItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return NAME;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!level.isClientSide() && remainingUseDuration % 6 == 0) {
            level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                SoundEvents.GENERIC_EAT, livingEntity.getSoundSource(), 0.5F, 1.0F);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(level instanceof ServerLevel serverLevel)) return stack;
        if (!(livingEntity instanceof ServerPlayer player)) return stack;

        MinecraftServer server = serverLevel.getServer();
        ServerPlayer.RespawnConfig config = player.getRespawnConfig();
        LevelData.RespawnData respawnData = config.respawnData();

        GlobalPos respawnPos = respawnData.globalPos();
        ServerLevel targetLevel = server.getLevel(respawnPos.dimension());
        if (targetLevel == null) targetLevel = server.overworld();

        BlockPos pos = respawnPos.pos();
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        boolean hasBed = !respawnData.equals(LevelData.RespawnData.DEFAULT);
        if (hasBed) {
            player.sendSystemMessage(Component.literal("\u00a7aTu cuerpo se desvanece y regresa a tu \u00faltimo descanso..."));
        } else {
            player.sendSystemMessage(Component.literal("\u00a7aEl mundo te acoge en su seno..."));
        }

        player.teleportTo(targetLevel, x, y, z, Set.of(), player.getYRot(), player.getXRot(), false);
        serverLevel.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(), 1.0F, 1.0F);
        stack.shrink(1);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 200;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.EAT;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        tooltipAdder.accept(Component.literal("\u00a77Al morder esta galleta, tu cuerpo se"));
        tooltipAdder.accept(Component.literal("\u00a77desvanece entre sue\u00f1os y regresa al"));
        tooltipAdder.accept(Component.literal("\u00a77lugar donde cerraste los ojos por"));
        tooltipAdder.accept(Component.literal("\u00a77\u00faltima vez."));
    }
}
