package com.bestiarymod.gui;

import com.bestiarymod.access.AccessoryDataAccessor;
import com.bestiarymod.handler.AccessoryItemState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.List;

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
        private final int unlockedSlots;
        private final Player player;

        Handler(int syncId, Inventory playerInventory, Player player) {
            super(null, syncId);
            this.player = player;
            this.unlockedSlots = Math.min(((AccessoryDataAccessor) player).getExtremoAccessorySlots(), 54);

            for (int i = 0; i < 54; i++) {
                boolean unlocked = i < unlockedSlots;
                int slotIndex = i;
                addSlot(new Slot(inventory, i, 0, 0) {
                    @Override public boolean mayPlace(ItemStack s) {
                        if (!unlocked || s.isEmpty()) return false;
                        for (int j = 0; j < 54; j++) {
                            if (j == slotIndex) continue;
                            ItemStack existing = inventory.getItem(j);
                            if (!existing.isEmpty() && existing.getItem() == s.getItem()) return false;
                        }
                        return true;
                    }
                    @Override public boolean mayPickup(Player p) { return unlocked; }
                });
            }
            for (int i = 9; i < 36; i++) {
                addSlot(new Slot(playerInventory, i, 0, 0) {
                    @Override public boolean mayPlace(ItemStack s) { return false; }
                    @Override public boolean mayPickup(Player p) { return true; }
                });
            }
            for (int i = 0; i < 9; i++) {
                addSlot(new Slot(playerInventory, i, 0, 0) {
                    @Override public boolean mayPlace(ItemStack s) { return false; }
                    @Override public boolean mayPickup(Player p) { return true; }
                });
            }
            fillSlots();
        }

        @Override
        public MenuType<?> getType() {
            return MenuType.GENERIC_9x6;
        }

        private void fillSlots() {
            List<ItemStack> items = AccessoryItemState.getItems(player.getUUID());
            inventory.clearContent();
            for (int i = 0; i < 54; i++) {
                if (i < unlockedSlots) {
                    if (i < items.size()) {
                        ItemStack saved = items.get(i);
                        if (!saved.isEmpty() && saved.getItem() != Items.BARRIER) {
                            inventory.setItem(i, saved.copy());
                            continue;
                        }
                    }
                    inventory.setItem(i, ItemStack.EMPTY);
                } else {
                    ItemStack slot = new ItemStack(Items.BARRIER);
                    slot.set(DataComponents.ITEM_MODEL, LOCKED_SLOT_MODEL);
                    slot.set(DataComponents.CUSTOM_NAME, Component.literal("\u00a78Slot bloqueado"));
                    inventory.setItem(i, slot);
                }
            }
        }

        @Override
        public void removed(Player player) {
            super.removed(player);
            saveItems();
        }

        private void saveItems() {
            List<ItemStack> items = AccessoryItemState.getItems(player.getUUID());
            for (int i = 0; i < 54; i++) {
                ItemStack slotStack = inventory.getItem(i).copy();
                if (slotStack.getItem() == Items.BARRIER) {
                    items.set(i, ItemStack.EMPTY);
                } else {
                    items.set(i, slotStack);
                }
            }
            AccessoryItemState.setItems(player.getUUID(), items);
        }

        @Override
        public void clicked(int slotIndex, int button, ContainerInput action, Player player) {
            if (slotIndex >= 0 && slotIndex < 54 && slotIndex >= unlockedSlots) return;
            super.clicked(slotIndex, button, action, player);
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
