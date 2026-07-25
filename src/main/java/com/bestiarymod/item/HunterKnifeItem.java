package com.bestiarymod.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import java.util.function.Consumer;

public class HunterKnifeItem extends Item {
    private static final Component NAME = Component.literal("\u00a76Cuchillo de Caza");
    private static final double BASE_DAMAGE = 5.0;
    private static final double ATTACK_SPEED = -2.0;

    public HunterKnifeItem(Properties properties) {
        super(properties.stacksTo(1)
            .component(DataComponents.ATTRIBUTE_MODIFIERS, createAttributes()));
    }

    private static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                Item.BASE_ATTACK_DAMAGE_ID, BASE_DAMAGE, AttributeModifier.Operation.ADD_VALUE
            ), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(
                Item.BASE_ATTACK_SPEED_ID, ATTACK_SPEED, AttributeModifier.Operation.ADD_VALUE
            ), EquipmentSlotGroup.MAINHAND)
            .build();
    }

    @Override
    public Component getName(ItemStack stack) {
        return NAME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        tooltipAdder.accept(Component.literal("\u00a77Hace \u00a7f5\u00a77 de da\u00f1o base."));
        tooltipAdder.accept(Component.literal("\u00a77Inflige \u00a7f+1\u00a77 de da\u00f1o por nivel de bestiario"));
        tooltipAdder.accept(Component.literal("\u00a77del mob al que golpeas."));
    }

    public static double getBaseDamage() {
        return BASE_DAMAGE;
    }
}
