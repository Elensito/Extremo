package com.bestiarymod.item;

import com.bestiarymod.screen.CarteraContainer;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CarteraItem extends Item {
    private static final Component TITLE = Component.literal("\u00a7eCartera");
    private static final TooltipDisplay HIDE_CONTAINER = TooltipDisplay.DEFAULT.withHidden(DataComponents.CONTAINER, true);

    public CarteraItem(Properties properties) {
        super(properties.component(DataComponents.TOOLTIP_DISPLAY, HIDE_CONTAINER));
    }

    @Override
    public Component getName(ItemStack stack) {
        return TITLE;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        tooltipAdder.accept(Component.literal("\u00a77Almacena las monedas recogidas autom\u00e1ticamente"));
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (contents != null) {
            NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
            contents.copyInto(items);
            Map<Item, Integer> totals = new HashMap<>();
            Map<Item, Component> names = new HashMap<>();
            for (ItemStack s : items) {
                if (!s.isEmpty()) {
                    Item type = s.getItem();
                    totals.merge(type, s.getCount(), Integer::sum);
                    if (!names.containsKey(type)) {
                        names.put(type, s.getHoverName());
                    }
                }
            }
            for (Map.Entry<Item, Integer> entry : totals.entrySet()) {
                tooltipAdder.accept(Component.literal("x" + entry.getValue() + " ").append(names.get(entry.getKey())));
            }
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack stack = player.getItemInHand(hand);
        CarteraContainer container = new CarteraContainer(stack);
        player.openMenu(new SimpleMenuProvider(
            (containerId, inv, p) -> new DispenserMenu(containerId, inv, container),
            TITLE
        ));
        return InteractionResult.SUCCESS;
    }
}
