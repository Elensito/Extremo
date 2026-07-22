package com.bestiarymod.mixin;

import com.bestiarymod.item.CarteraItem;
import com.bestiarymod.item.CoinItem;
import com.bestiarymod.screen.CarteraContainer;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.ArrayList;
import java.util.List;

@Mixin(Inventory.class)
public class PlayerInventoryMixin {
    @Shadow @Final public Player player;

    @Inject(method = "add(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void onAddItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.isEmpty()) return;
        if (!(stack.getItem() instanceof CoinItem)) return;

        if (tryAddToOpenContainer(stack)) {
            stack.setCount(0);
            cir.setReturnValue(true);
            return;
        }

        ItemStack cartera = findCartera();
        if (cartera.isEmpty()) return;

        ItemContainerContents contents = cartera.get(DataComponents.CONTAINER);
        List<ItemStack> items = new ArrayList<>();
        if (contents != null) {
            NonNullList<ItemStack> nonNull = NonNullList.withSize(9, ItemStack.EMPTY);
            contents.copyInto(nonNull);
            items.addAll(nonNull);
        } else {
            for (int i = 0; i < 9; i++) items.add(ItemStack.EMPTY);
        }

        int toAdd = stack.getCount();
        for (int i = 0; i < 9 && toAdd > 0; i++) {
            ItemStack slot = items.get(i);
            if (slot.isEmpty()) {
                ItemStack copy = stack.copyWithCount(Math.min(toAdd, stack.getMaxStackSize()));
                items.set(i, copy);
                toAdd -= copy.getCount();
            } else if (ItemStack.isSameItemSameComponents(slot, stack)) {
                int space = slot.getMaxStackSize() - slot.getCount();
                if (space > 0) {
                    int add = Math.min(toAdd, space);
                    slot.grow(add);
                    toAdd -= add;
                }
            }
        }

        if (toAdd < stack.getCount()) {
            cartera.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
            stack.setCount(toAdd);
            cir.setReturnValue(true);
        }
    }

    private boolean tryAddToOpenContainer(ItemStack stack) {
        if (!(player.containerMenu instanceof DispenserMenu dm)) return false;
        Container c = dm.getSlot(0).container;
        if (!(c instanceof CarteraContainer cc)) return false;
        int toAdd = stack.getCount();
        for (int i = 0; i < 9 && toAdd > 0; i++) {
            ItemStack slot = cc.getItem(i);
            if (slot.isEmpty()) {
                ItemStack copy = stack.copyWithCount(Math.min(toAdd, stack.getMaxStackSize()));
                cc.setItem(i, copy);
                toAdd -= copy.getCount();
            } else if (ItemStack.isSameItemSameComponents(slot, stack)) {
                int space = slot.getMaxStackSize() - slot.getCount();
                if (space > 0) {
                    int add = Math.min(toAdd, space);
                    slot.grow(add);
                    toAdd -= add;
                }
            }
        }
        return toAdd < stack.getCount();
    }

    private ItemStack findCartera() {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.getItem() instanceof CarteraItem) return s;
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof CarteraItem) return offhand;
        return ItemStack.EMPTY;
    }
}
