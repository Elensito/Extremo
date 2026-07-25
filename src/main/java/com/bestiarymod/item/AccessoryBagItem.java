package com.bestiarymod.item;

import com.bestiarymod.access.AccessoryDataAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import java.util.function.Consumer;

public class AccessoryBagItem extends Item {
    private static final Component NAME = Component.literal("\u00a75Bolsa de Accesorios");
    private static final int MAX_SLOTS = 54;

    public AccessoryBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return NAME;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            AccessoryDataAccessor accessor = (AccessoryDataAccessor) serverPlayer;
            if (accessor.getExtremoAccessorySlots() >= MAX_SLOTS) {
                serverPlayer.sendSystemMessage(Component.literal("\u00a7c\u00a1Ya tienes todos los slots de accesorios desbloqueados!"));
                return InteractionResult.FAIL;
            }
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (livingEntity instanceof ServerPlayer player) {
            AccessoryDataAccessor accessor = (AccessoryDataAccessor) player;
            int slots = accessor.getExtremoAccessorySlots();
            if (slots < MAX_SLOTS) {
                accessor.setExtremoAccessorySlots(slots + 1);
                stack.shrink(1);
                player.sendSystemMessage(Component.literal("\u00a7a\u00a1Has desbloqueado un slot de accesorio! Slots: " + (slots + 1) + "/" + MAX_SLOTS));
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, player.getSoundSource(), 1.0F, 1.0F);
            }
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.EAT;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        tooltipAdder.accept(Component.literal("\u00a77Al consumirla, desbloqueas un slot"));
        tooltipAdder.accept(Component.literal("\u00a77adicional en la bolsa de accesorios."));
    }
}
