package com.bestiarymod.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;

public class EnderPearlUpgradeItem extends Item {
    private static final int PURPLE = 0xAA00FF;

    public EnderPearlUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("Perla Abisal").withStyle(style -> style.withColor(PURPLE));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }
}
