package com.bestiarymod.gui;

import com.bestiarymod.Extremo;
import com.bestiarymod.mission.MissionEntry;
import com.bestiarymod.mission.MissionManager;
import com.bestiarymod.mission.MissionState;
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
import java.util.UUID;

public class QuestListGui implements MenuProvider {
    private static final Identifier LOCKED_SLOT = Identifier.fromNamespaceAndPath("extremo", "accessory_slot");
    private final int page;

    public QuestListGui(int page) {
        this.page = page;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Misiones");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new Handler(syncId, inv, player, page);
    }

    private static class Handler extends AbstractContainerMenu {
        private final SimpleContainer inventory = new SimpleContainer(54);
        private final Player player;
        private final List<MissionEntry> entries;
        private final int page;
        private static final int ENTRIES_PER_PAGE = 18;

        Handler(int syncId, Inventory playerInventory, Player player, int page) {
            super(null, syncId);
            this.player = player;
            this.page = page;
            UUID puid = player.getUUID();
            this.entries = new ArrayList<>();
            for (MissionEntry e : MissionManager.getAllEntries()) {
                if (MissionState.isClaimed(puid, e.id)) continue;
                if (e.isExpired() && MissionState.getProgress(puid, e.id) < e.amount) continue;
                if (e.maxClaims > 0 && MissionState.getClaimCount(e.id) >= e.maxClaims && !MissionState.isCompleted(puid, e.id)) continue;
                this.entries.add(e);
            }
            for (MissionEntry e : MissionManager.getAllDeleted()) {
                if (MissionState.isClaimed(puid, e.id)) continue;
                if (MissionState.getProgress(puid, e.id) >= e.amount) {
                    this.entries.add(e);
                }
            }

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
                    MissionEntry entry = entries.get(i);
                    if (entry == null) continue;

                    boolean claimed = MissionState.isClaimed(player.getUUID(), entry.id);
                    boolean completed = MissionState.isCompleted(player.getUUID(), entry.id);
                    int progress = MissionState.getProgress(player.getUUID(), entry.id);
                    int percent = entry.amount > 0 ? (int) Math.floor((double) progress / entry.amount * 100) : 0;
                    if (percent > 100) percent = 100;

                    ItemStack displayItem;
                    if (entry.iconItem != null && !entry.iconItem.isEmpty()) {
                        Identifier iconId = Identifier.tryParse(entry.iconItem);
                        var iconItem = iconId != null ? BuiltInRegistries.ITEM.get(iconId).map(h -> h.value()).orElse(Items.BARRIER) : Items.BARRIER;
                        displayItem = new ItemStack(iconItem);
                        if (iconItem == Items.BARRIER) displayItem.set(DataComponents.ITEM_MODEL, LOCKED_SLOT);
                    } else if (entry.type.equals("kill")) {
                        Identifier entityId = Identifier.tryParse(entry.target);
                        if (entityId != null) {
                            var eggId = Identifier.fromNamespaceAndPath(entityId.getNamespace(), entityId.getPath() + "_spawn_egg");
                            var eggItem = BuiltInRegistries.ITEM.get(eggId).map(h -> h.value()).orElse(Items.SKELETON_SPAWN_EGG);
                            displayItem = new ItemStack(eggItem);
                        } else {
                            displayItem = new ItemStack(Items.SKELETON_SPAWN_EGG);
                        }
                    } else {
                        Identifier itemId = Identifier.tryParse(entry.target);
                        var item = itemId != null ? BuiltInRegistries.ITEM.get(itemId).map(h -> h.value()).orElse(Items.APPLE) : Items.APPLE;
                        displayItem = new ItemStack(item);
                    }

                    if (entry.type.equals("kill")) {
                        displayItem.set(DataComponents.CUSTOM_NAME, Component.literal("\u2694 " + entry.getDisplayName()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                    } else {
                        displayItem.set(DataComponents.CUSTOM_NAME, Component.literal("\u2B06 " + entry.getDisplayName()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                    }

                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.literal(""));
                    int bars = 20;
                    int filled = (int) Math.floor(percent / 100.0 * bars);
                    StringBuilder bar = new StringBuilder("\u00a77[");
                    for (int b = 0; b < bars; b++) {
                        bar.append(b < filled ? "\u00a7a|" : "\u00a78|");
                    }
                    bar.append("\u00a77]");
                    lore.add(Component.literal(bar.toString()));
                    lore.add(Component.literal("Progreso: " + progress + "/" + entry.amount).withStyle(ChatFormatting.GRAY));
                    lore.add(Component.literal(""));
                    lore.add(Component.literal("Recompensas:").withStyle(ChatFormatting.LIGHT_PURPLE));
                    if (entry.xpReward > 0) {
                        lore.add(Component.literal("  - " + entry.xpReward + " XP").withStyle(ChatFormatting.AQUA));
                    }
                    if (entry.coinReward > 0) {
                        lore.add(Component.literal("  - " + entry.coinReward + " monedas").withStyle(ChatFormatting.GOLD));
                    }
                    for (var ri : entry.itemRewards) {
                        String name = ri.item.contains(":") ? ri.item.substring(ri.item.indexOf(":") + 1) : ri.item;
                        lore.add(Component.literal("  - " + ri.count + "x " + name).withStyle(ChatFormatting.LIGHT_PURPLE));
                    }
                    lore.add(Component.literal(""));
                    if (claimed) {
                        lore.add(Component.literal("\u2713 Completada").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                    } else if (completed) {
                        lore.add(Component.literal("\u00a1Click para reclamar!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                    } else {
                        lore.add(Component.literal("Click para ver detalles").withStyle(ChatFormatting.GRAY));
                    }

                    displayItem.set(DataComponents.LORE, new ItemLore(lore));
                    if (claimed) {
                        displayItem.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                    }
                    inventory.setItem(index++, displayItem);
                }

                ItemStack closeStack = new ItemStack(Items.BARRIER);
                closeStack.set(DataComponents.ITEM_MODEL, LOCKED_SLOT);
                closeStack.set(DataComponents.CUSTOM_NAME, Component.literal("Salir").withStyle(ChatFormatting.RED));
                inventory.setItem(53, closeStack);
            } catch (Exception e) {
                Extremo.LOGGER.error("Error in QuestListGui.setupSlots: {}", e.getMessage(), e);
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
            int entryIndex = slotIndex;
            if (entryIndex >= 0 && entryIndex < entries.size()) {
                MissionEntry entry = entries.get(entryIndex);
                ((ServerPlayer) player).closeContainer();
                player.openMenu(new QuestDetailGui(entry.id));
            }
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

        @Override
        public boolean stillValid(Player player) { return true; }
    }
}
