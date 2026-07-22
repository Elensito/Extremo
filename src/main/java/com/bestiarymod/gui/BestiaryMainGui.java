package com.bestiarymod.gui;

import com.bestiarymod.Extremo;
import com.bestiarymod.config.BestiaryConfigManager;
import com.bestiarymod.config.BestiaryEntry;
import com.bestiarymod.data.BestiaryState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import java.util.ArrayList;
import java.util.List;

public class BestiaryMainGui implements MenuProvider {
    private final int page;

    public BestiaryMainGui(int page) {
        this.page = page;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Bestiario");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new Handler(syncId, inv, player, page);
    }

    private static class Handler extends AbstractContainerMenu {
        private final SimpleContainer inventory = new SimpleContainer(54);
        private final Player player;
        private final List<BestiaryEntry> entries;
        private final int page;
        private static final int ENTRIES_PER_PAGE = 18;

        Handler(int syncId, Inventory playerInventory, Player player, int page) {
            super(null, syncId);
            this.player = player;
            this.page = page;
            this.entries = new ArrayList<>(BestiaryConfigManager.getAllEntries());

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
            try {
                inventory.clearContent();
                int start = page * ENTRIES_PER_PAGE;
                int end = Math.min(start + ENTRIES_PER_PAGE, entries.size());

                int index = 0;
                for (int i = start; i < end; i++) {
                    BestiaryEntry entry = entries.get(i);
                    if (entry == null) continue;
                    ItemStack displayItem = entry.getDisplayItem();
                    if (displayItem == null) continue;

                    int totalKills = 0;
                    int completedLevels = 0;
                    try {
                        totalKills = BestiaryState.getKills(player.getUUID(), entry.mobId);
                        for (int li = 0; li < entry.levels.size(); li++) {
                            if (BestiaryState.getClaimedLevels(player.getUUID(), entry.mobId).contains(li)) {
                                completedLevels++;
                            }
                        }
                    } catch (Exception e) {
                        Extremo.LOGGER.error("Error getting kills/claims for {}: {}", entry.mobId, e.getMessage());
                    }

                    if (completedLevels >= entry.levels.size()) continue;

                    int lastLevelKills = entry.levels.get(entry.levels.size() - 1).killCount;
                    int percent = lastLevelKills > 0 ? (int) Math.floor((double) totalKills / lastLevelKills * 100) : 0;
                    if (percent > 100) percent = 100;

                    displayItem.set(DataComponents.CUSTOM_NAME, Component.literal(formatMobName(entry.mobId)).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.literal(""));
                    lore.add(Component.literal("Asesinatos totales: " + totalKills).withStyle(ChatFormatting.GRAY));
                    lore.add(Component.literal("Progreso: " + percent + "%").withStyle(ChatFormatting.YELLOW));
                    lore.add(Component.literal(""));
                    lore.add(Component.literal("Click para ver detalles").withStyle(ChatFormatting.GREEN));

                    displayItem.set(DataComponents.LORE, new ItemLore(lore));
                    inventory.setItem(index++, displayItem);
                }

                ItemStack closeStack = new ItemStack(Items.BARRIER);
                closeStack.set(DataComponents.CUSTOM_NAME, Component.literal("Salir").withStyle(ChatFormatting.RED));
                inventory.setItem(53, closeStack);
            } catch (Exception e) {
                Extremo.LOGGER.error("Error in BestiaryMainGui.setupSlots: {}", e.getMessage(), e);
            }
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
            int start = page * ENTRIES_PER_PAGE;
            int entryIndex = slotIndex;
            if (entryIndex >= 0 && entryIndex < entries.size()) {
                BestiaryEntry entry = entries.get(entryIndex);
                ((ServerPlayer) player).closeContainer();
                player.openMenu(new BestiaryDetailGui(entry.mobId));
            }
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

        @Override
        public boolean stillValid(Player player) { return true; }

        private String formatMobName(String mobId) {
            String name = mobId.contains(":") ? mobId.substring(mobId.indexOf(":") + 1) : mobId;
            String[] parts = name.split("_");
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                if (part.isEmpty()) continue;
                sb.append(Character.toUpperCase(part.charAt(0)));
                sb.append(part.substring(1));
                sb.append(" ");
            }
            return sb.toString().trim();
        }
    }
}
