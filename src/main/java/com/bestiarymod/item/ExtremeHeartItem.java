package com.bestiarymod.item;

import com.bestiarymod.access.HeartDataAccessor;
import com.bestiarymod.network.HeartSyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import java.util.function.Consumer;

public class ExtremeHeartItem extends Item {
    private static final Component NAME = Component.literal("\u00a7c\u2764 Coraz\u00f3n Extremo");
    private static final int MAX_HEARTS = 5;

    public ExtremeHeartItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return NAME;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer serverPlayer) {
            HeartDataAccessor accessor = (HeartDataAccessor) serverPlayer;
            int hearts = accessor.getExtremoHearts();
            if (hearts >= MAX_HEARTS) {
                serverPlayer.sendSystemMessage(Component.literal("\u00a7c\u00a1Ya posees el m\u00e1ximo de vidas!"));
                return InteractionResult.FAIL;
            }
            accessor.setExtremoHearts(hearts + 1);
            ServerPlayNetworking.send(serverPlayer, new HeartSyncPayload(hearts + 1));
            player.getItemInHand(hand).shrink(1);
            serverPlayer.sendSystemMessage(Component.literal("\u00a7a\u2764 \u00a1Un latido eterno late en tu pecho! Tienes " + (hearts + 1) + "/" + MAX_HEARTS + " vidas."));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        tooltipAdder.accept(Component.literal("\u00a77Un fragmento del coraz\u00f3n del mundo."));
        tooltipAdder.accept(Component.literal("\u00a77Al consumirlo, obtienes una vida extra en este servidor."));
        tooltipAdder.accept(Component.literal(""));
        tooltipAdder.accept(Component.literal("\u00a7cSolo los m\u00e1s valientes portan este poder."));
    }
}
