package com.bestiarymod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import java.util.function.Consumer;

public class EnchantedIronIngotItem extends Item {
    private static final Component NAME = Component.literal("\u00a76Fragmento de Berserker");
    public static final Identifier ARMOR_TOUGHNESS_MODIFIER_ID = Identifier.fromNamespaceAndPath("extremo", "consumable_armor_toughness");

    public EnchantedIronIngotItem(Properties properties) {
        super(properties);
    }

    public static void removeModifier(ServerPlayer player) {
        AttributeInstance attr = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (attr != null) {
            attr.removeModifier(ARMOR_TOUGHNESS_MODIFIER_ID);
        }
    }

    private static boolean hasConsumed(ServerPlayer player) {
        AttributeInstance attr = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
        return attr != null && attr.getModifier(ARMOR_TOUGHNESS_MODIFIER_ID) != null;
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
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (livingEntity instanceof ServerPlayer player) {
            if (hasConsumed(player)) {
                player.sendSystemMessage(Component.literal("\u00a7cYa has consumido este objeto (1/1)"));
                return stack;
            }
            AttributeInstance attr = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
            if (attr != null) {
                attr.removeModifier(ARMOR_TOUGHNESS_MODIFIER_ID);
                attr.addTransientModifier(new AttributeModifier(
                    ARMOR_TOUGHNESS_MODIFIER_ID, 1.0, AttributeModifier.Operation.ADD_VALUE
                ));
            }
            stack.shrink(1);
            player.sendSystemMessage(Component.literal("\u00a7a\u00a1Tu piel se vuelve m\u00e1s resistente! Ahora tienes +1 de Armor Toughness."));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, player.getSoundSource(), 1.0F, 1.0F);
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
        tooltipAdder.accept(Component.literal("\u00a77Un fragmento del poder de un Berserker Golem."));
        tooltipAdder.accept(Component.literal("\u00a77Al consumirlo, tu piel se endurece como"));
        tooltipAdder.accept(Component.literal("\u00a77el acero, otorg\u00e1ndote \u00a7a+1 de Armor"));
        tooltipAdder.accept(Component.literal("\u00a77Toughness\u00a77."));
        tooltipAdder.accept(Component.literal(""));
        tooltipAdder.accept(Component.literal("\u00a78\u00a7oSolo una vez por vida."));
    }
}
