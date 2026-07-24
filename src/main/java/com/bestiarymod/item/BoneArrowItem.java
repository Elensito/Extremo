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

public class BoneArrowItem extends Item {
    private static final Component NAME = Component.literal("\u00a7bFlecha de Hueso");
    public static final Identifier DAMAGE_MODIFIER_ID = Identifier.fromNamespaceAndPath("extremo", "bone_arrow");

    public BoneArrowItem(Properties properties) {
        super(properties);
    }

    private static boolean hasConsumed(ServerPlayer player) {
        AttributeInstance attr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        return attr != null && attr.getModifier(DAMAGE_MODIFIER_ID) != null;
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
                player.sendSystemMessage(Component.literal("\u00a7cYa has consumido esta flecha (1/1)"));
                return stack;
            }
            AttributeInstance attr = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attr != null) {
                attr.removeModifier(DAMAGE_MODIFIER_ID);
                attr.addTransientModifier(new AttributeModifier(
                    DAMAGE_MODIFIER_ID, 1.0, AttributeModifier.Operation.ADD_VALUE
                ));
            }
            stack.shrink(1);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, player.getSoundSource(), 1.0F, 1.5F);
            player.sendSystemMessage(Component.literal("\u00a7a\u00a1El poder \u00f3seo fluye en ti! +1 da\u00f1o de ataque y proyectil."));
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
        tooltipAdder.accept(Component.literal("\u00a77Una flecha forjada con huesos de cazadores ca\u00eddos."));
        tooltipAdder.accept(Component.literal(""));
        tooltipAdder.accept(Component.literal("\u00a77Al consumirla, aumentas tu \u00a7bda\u00f1o de ataque y proyectil \u00a77en \u00a7a+1\u00a77."));
        tooltipAdder.accept(Component.literal("\u00a78\u00a7oSolo una vez por vida."));
    }
}
