package com.bestiarymod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CoinItem extends Item {
    private final Component displayName;

    public CoinItem(Properties properties, Component displayName) {
        super(properties);
        this.displayName = displayName;
    }

    @Override
    public Component getName(ItemStack stack) {
        return displayName;
    }
}
