package com.bestiarymod.gui;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AccessoriesGui implements MenuProvider {
    private static final Identifier LOCKED_SLOT_MODEL = Identifier.fromNamespaceAndPath("extremo", "accessory_slot");

    @Override
    public Component getDisplayName() {
        return Component.literal("Accesorios");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new Handler(syncId, inv, player);
    }

    private static class Handler extends AbstractContainerMenu {
        private final SimpleContainer inventory = new SimpleContainer(54);

        Handler(int syncId, Inventory playerInventory, Player player) {
            super(null, syncId);

            for (int i = 0; i < 54; i++) {
                addSlot(new Slot(inventory, i, 0, 0) {
                    @Override public boolean mayPlace(ItemStack s) { return false; }
                    @Override public boolean mayPickup(Player p) { return false; }
                });
            }
            for (int i = 9; i < 36; i++) {
                addSlot(new Slot(playerInventory, i, 0, 0) {
                    @Override public boolean mayPlace(ItemStack s) { return false; }
                    @Override public boolean mayPickup(Player p) { return false; }
                });
            }
            for (int i = 0; i < 9; i++) {
                addSlot(new Slot(playerInventory, i, 0, 0) {
                    @Override public boolean mayPlace(ItemStack s) { return false; }
                    @Override public boolean mayPickup(Player p) { return false; }
                });
            }
            fillSlots();
        }

        @Override
        public MenuType<?> getType() {
            return MenuType.GENERIC_9x6;
        }

        private void fillSlots() {
            inventory.clearContent();
            for (int i = 0; i < 54; i++) {
                ItemStack slot = new ItemStack(Items.BARRIER);
                slot.set(DataComponents.ITEM_MODEL, LOCKED_SLOT_MODEL);
                slot.set(DataComponents.CUSTOM_NAME, Component.literal("\u00a78Slot bloqueado"));
                inventory.setItem(i, slot);
            }
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
