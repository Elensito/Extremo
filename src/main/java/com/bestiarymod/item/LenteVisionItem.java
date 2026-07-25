package com.bestiarymod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import java.util.function.Consumer;

public class LenteVisionItem extends Item {
    private static final Component NAME = Component.literal("\u00a7bLente de Visi\u00f3n");

    public LenteVisionItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return NAME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        tooltipAdder.accept(Component.literal("\u00a77Muestra el \u00a7f% de vida restante\u00a77 de cualquier"));
        tooltipAdder.accept(Component.literal("\u00a77mob al que est\u00e1s mirando en la action bar."));
    }
}
