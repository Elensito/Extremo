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

public class LifeHeartItem extends Item {
    private static final Component NAME = Component.literal("\u00a7dCoraz\u00f3n Vital");
    private static final String CONSUMABLE_KEY = "life_heart";

    public LifeHeartItem(Properties properties) {
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
                serverPlayer.sendSystemMessage(Component.literal("\u00a7c\u00a1Ya has consumido este coraz\u00f3n!"));
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
                AttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
                if (attr != null) {
                    attr.setBaseValue(attr.getBaseValue() + 2.0);
                }
                stack.shrink(1);
                player.heal(2.0F);
                player.sendSystemMessage(Component.literal("\u00a7a\u00a1Tu esencia vital se expande! Ahora tienes \u00a7c+1 coraz\u00f3n \u00a7ade vida m\u00e1xima."));
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, player.getSoundSource(), 1.0F, 1.0F);
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
        tooltipAdder.accept(Component.literal("\u00a77Un coraz\u00f3n que late con energ\u00eda primigenia."));
        tooltipAdder.accept(Component.literal(""));
        tooltipAdder.accept(Component.literal("\u00a77Al consumirlo, tu esencia vital se expande,"));
        tooltipAdder.accept(Component.literal("\u00a77aumentando tu vida m\u00e1xima en \u00a7c+1 coraz\u00f3n\u00a77."));
        tooltipAdder.accept(Component.literal(""));
        tooltipAdder.accept(Component.literal("\u00a78\u00a7oSolo una vez."));
    }
}
