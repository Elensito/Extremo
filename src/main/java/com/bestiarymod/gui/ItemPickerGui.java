package com.bestiarymod.gui;

import com.bestiarymod.mission.MissionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import java.util.ArrayList;
import java.util.List;

public class ItemPickerGui implements MenuProvider {
    private static final Identifier LOCKED_SLOT = Identifier.fromNamespaceAndPath("extremo", "accessory_slot");
    private final String questId;
    private final int page;

    public ItemPickerGui(String questId, int page) {
        this.questId = questId;
        this.page = page;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Seleccionar Icono");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new Handler(syncId, inv, player, questId, page);
    }

    private static class Handler extends AbstractContainerMenu {
        private final SimpleContainer inventory = new SimpleContainer(54);
        private final Player player;
        private final String questId;
        private final int page;
        private static final int ITEMS_PER_PAGE = 45;

        Handler(int syncId, Inventory playerInventory, Player player, String questId, int page) {
            super(null, syncId);
            this.player = player;
            this.questId = questId;
            this.page = page;

            for (int i = 0; i < 54; i++) {
                addSlot(new Slot(inventory, i, 0, 0) {
                    @Override public boolean mayPlace(ItemStack s) { return false; }
                    @Override public boolean mayPickup(Player p) { return true; }
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
            setupSlots();
        }

        @Override
        public MenuType<?> getType() {
            return MenuType.GENERIC_9x6;
        }

        private void setupSlots() {
            inventory.clearContent();

            List<Identifier> allItems = new ArrayList<>(BuiltInRegistries.ITEM.keySet());
            int start = page * ITEMS_PER_PAGE;
            int end = Math.min(start + ITEMS_PER_PAGE, allItems.size());

            int slot = 0;
            for (int i = start; i < end; i++) {
                Identifier id = allItems.get(i);
                var itemRef = BuiltInRegistries.ITEM.get(id);
                if (itemRef.isEmpty()) continue;
                ItemStack stack = new ItemStack(itemRef.get().value());
                List<Component> lore = new ArrayList<>();
                lore.add(Component.literal("\u00a77" + id.toString()));
                lore.add(Component.literal(""));
                lore.add(Component.literal("Click para seleccionar como icono").withStyle(ChatFormatting.GRAY));
                stack.set(DataComponents.LORE, new ItemLore(lore));
                inventory.setItem(slot++, stack);
            }

            ItemStack backStack = new ItemStack(Items.ARROW);
            backStack.set(DataComponents.CUSTOM_NAME, Component.literal("\u00a7aVolver").withStyle(ChatFormatting.GREEN));
            inventory.setItem(48, backStack);

            ItemStack nextStack = new ItemStack(Items.ARROW);
            nextStack.set(DataComponents.CUSTOM_NAME, Component.literal("\u00a7aSiguiente").withStyle(ChatFormatting.GREEN));
            inventory.setItem(50, nextStack);

            ItemStack closeStack = new ItemStack(Items.BARRIER);
            closeStack.set(DataComponents.ITEM_MODEL, LOCKED_SLOT);
            closeStack.set(DataComponents.CUSTOM_NAME, Component.literal("\u00a7cSalir"));
            inventory.setItem(53, closeStack);
        }

        @Override
        public void clicked(int slotIndex, int button, ContainerInput action, Player player) {
            if (slotIndex >= 54) {
                super.clicked(slotIndex, button, action, player);
                return;
            }
            if (slotIndex == 53) {
                ((ServerPlayer) player).closeContainer();
                return;
            }
            if (slotIndex == 48) {
                // Previous page
                if (page > 0) {
                    ((ServerPlayer) player).closeContainer();
                    player.openMenu(new ItemPickerGui(questId, page - 1));
                }
                return;
            }
            if (slotIndex == 50) {
                // Next page
                int totalItems = BuiltInRegistries.ITEM.keySet().size();
                if ((page + 1) * ITEMS_PER_PAGE < totalItems) {
                    ((ServerPlayer) player).closeContainer();
                    player.openMenu(new ItemPickerGui(questId, page + 1));
                }
                return;
            }
            if (slotIndex >= 0 && slotIndex < 45) {
                List<Identifier> allItems = new ArrayList<>(BuiltInRegistries.ITEM.keySet());
                int itemIndex = page * ITEMS_PER_PAGE + slotIndex;
                if (itemIndex < allItems.size()) {
                    Identifier selectedId = allItems.get(itemIndex);
                    var entry = MissionManager.getEntry(questId);
                    if (entry != null) {
                        entry.iconItem = selectedId.toString();
                        MissionManager.saveEntry(entry);
                        ((ServerPlayer) player).closeContainer();
                        player.openMenu(new QuestDetailGui(questId));
                    }
                }
            }
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

        @Override
        public boolean stillValid(Player player) { return true; }
    }
}
