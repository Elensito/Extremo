package com.bestiarymod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import java.util.function.Consumer;

public class HunterTalismanItem extends Item {
    private static final Component NAME = Component.literal("\u00a7dTalism\u00e1n del Cazador");

    public HunterTalismanItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return NAME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        tooltipAdder.accept(Component.literal("\u00a77Muestra en pantalla el mob del bestiario m\u00e1s cercano"));
        tooltipAdder.accept(Component.literal("\u00a77y le otorga brillo resplandeciente."));
    }
}
