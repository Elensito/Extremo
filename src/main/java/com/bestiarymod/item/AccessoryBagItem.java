package com.bestiarymod.item;

import com.bestiarymod.access.AccessoryDataAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
            accessor.setExtremoAccessorySlots(accessor.getExtremoAccessorySlots() + 1);
            ItemStack stack = player.getItemInHand(hand);
            stack.shrink(1);
            serverPlayer.sendSystemMessage(Component.literal("\u00a7a\u00a1Has desbloqueado un slot de accesorio! Slots: " + accessor.getExtremoAccessorySlots() + "/" + MAX_SLOTS));
            serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.PLAYER_LEVELUP, serverPlayer.getSoundSource(), 1.0F, 1.0F);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        tooltipAdder.accept(Component.literal("\u00a77Al usarla, desbloqueas un slot"));
        tooltipAdder.accept(Component.literal("\u00a77adicional en la bolsa de accesorios."));
    }
}
