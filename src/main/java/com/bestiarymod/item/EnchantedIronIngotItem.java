package com.bestiarymod.item;

import com.bestiarymod.access.ConsumableDataAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
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
    private static final Component NAME = Component.literal("\u00a7bLingote de Hierro Encantado");
    private static final String CONSUMABLE_KEY = "enchanted_iron_ingot";

    public EnchantedIronIngotItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return NAME;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            ConsumableDataAccessor accessor = (ConsumableDataAccessor) serverPlayer;
            if (accessor.hasConsumed(CONSUMABLE_KEY)) {
                serverPlayer.sendSystemMessage(Component.literal("\u00a7c\u00a1Ya has consumido este lingote!"));
                return InteractionResult.FAIL;
            }
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (livingEntity instanceof ServerPlayer player) {
            ConsumableDataAccessor accessor = (ConsumableDataAccessor) player;
            if (!accessor.hasConsumed(CONSUMABLE_KEY)) {
                accessor.markConsumed(CONSUMABLE_KEY);
                AttributeInstance attr = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
                if (attr != null) {
                    attr.setBaseValue(attr.getBaseValue() + 1.0);
                }
                stack.shrink(1);
                player.sendSystemMessage(Component.literal("\u00a7a\u00a1Tu piel se vuelve m\u00e1s resistente! Ahora tienes +1 de Armor Toughness."));
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, player.getSoundSource(), 1.0F, 1.0F);
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
        tooltipAdder.accept(Component.literal("\u00a77Un lingote imbuido con magia antigua."));
        tooltipAdder.accept(Component.literal(""));
        tooltipAdder.accept(Component.literal("\u00a77Al consumirlo, tu piel se vuelve m\u00e1s"));
        tooltipAdder.accept(Component.literal("\u00a77resistente, otorg\u00e1ndote un punto de"));
        tooltipAdder.accept(Component.literal("\u00a77Armor Toughness permanente."));
        tooltipAdder.accept(Component.literal(""));
        tooltipAdder.accept(Component.literal("\u00a78\u00a7oSolo se puede consumir una vez."));
    }
}
