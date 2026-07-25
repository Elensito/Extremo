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

public class QuestDetailGui implements MenuProvider {
    private final String questId;

    public QuestDetailGui(String questId) {
        this.questId = questId;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Detalle de Misi\u00f3n");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new Handler(syncId, inv, player, questId);
    }

    private static class Handler extends AbstractContainerMenu {
        private final SimpleContainer inventory = new SimpleContainer(54);
        private final Player player;
        private final String questId;
        private final MissionEntry entry;

        Handler(int syncId, Inventory playerInventory, Player player, String questId) {
            super(null, syncId);
            this.player = player;
            this.questId = questId;
            this.entry = MissionManager.getEntry(questId);

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
                if (entry == null) return;

                int progress = MissionState.getProgress(player.getUUID(), questId);
                boolean claimed = MissionState.isClaimed(player.getUUID(), questId);
                boolean completed = progress >= entry.amount;
                int percent = entry.amount > 0 ? (int) Math.floor((double) progress / entry.amount * 100) : 0;
                if (percent > 100) percent = 100;

                ItemStack infoStack;
                if (entry.iconItem != null && !entry.iconItem.isEmpty()) {
                    Identifier iconId = Identifier.tryParse(entry.iconItem);
                    var iconItem = iconId != null ? BuiltInRegistries.ITEM.get(iconId).map(h -> h.value()).orElse(Items.BOOK) : Items.BOOK;
                    infoStack = new ItemStack(iconItem);
                } else if (entry.type.equals("kill")) {
                    Identifier entityId = Identifier.tryParse(entry.target);
                    if (entityId != null) {
                        var eggId = Identifier.fromNamespaceAndPath(entityId.getNamespace(), entityId.getPath() + "_spawn_egg");
                        var eggItem = BuiltInRegistries.ITEM.get(eggId).map(h -> h.value()).orElse(Items.BOOK);
                        infoStack = new ItemStack(eggItem);
                    } else {
                        infoStack = new ItemStack(Items.BOOK);
                    }
                } else {
                    Identifier itemId = Identifier.tryParse(entry.target);
                    var item = itemId != null ? BuiltInRegistries.ITEM.get(itemId).map(h -> h.value()).orElse(Items.APPLE) : Items.APPLE;
                    infoStack = new ItemStack(item);
                }

                infoStack.set(DataComponents.CUSTOM_NAME, Component.literal(entry.getDisplayName()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.literal(""));
                lore.add(Component.literal("\u00a77Tipo: \u00a7f" + (entry.type.equals("kill") ? "Asesinato" : "Recolecci\u00f3n")));
                lore.add(Component.literal("\u00a77Objetivo: \u00a7f" + entry.getTargetDisplay()));
                if (entry.expiresAt > 0) {
                    long remaining = entry.expiresAt - System.currentTimeMillis();
                    if (remaining > 0) {
                        lore.add(Component.literal("\u00a77Expira en: \u00a7e" + (remaining / 3600000) + "h " + ((remaining % 3600000) / 60000) + "m"));
                    } else {
                        lore.add(Component.literal("\u00a77Expira en: \u00a7cExpirada"));
                    }
                }
                if (entry.maxClaims > 0) {
                    int claims = MissionState.getClaimCount(entry.id);
                    lore.add(Component.literal("\u00a77Reclamaciones: \u00a7d" + claims + "/" + entry.maxClaims));
                }
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

                if (claimed) {
                    lore.add(Component.literal(""));
                    lore.add(Component.literal("\u2713 Reclamado").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                } else if (completed) {
                    lore.add(Component.literal(""));
                    lore.add(Component.literal("\u00a1Reclama tu recompensa!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                } else {
                    int remaining = entry.amount - progress;
                    lore.add(Component.literal(""));
                    lore.add(Component.literal("Faltan " + remaining + " para completar").withStyle(ChatFormatting.RED));
                }

                infoStack.set(DataComponents.LORE, new ItemLore(lore));
                inventory.setItem(4, infoStack);

                // Reward items in slots 18-26
                int rewardSlot = 18;
                if (entry.xpReward > 0) {
                    ItemStack xpStack = new ItemStack(Items.EXPERIENCE_BOTTLE);
                    xpStack.set(DataComponents.CUSTOM_NAME, Component.literal(entry.xpReward + " XP").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
                    inventory.setItem(rewardSlot++, xpStack);
                }
                if (entry.coinReward > 0) {
                    ItemStack coinStack = new ItemStack(Items.GOLD_NUGGET);
                    coinStack.set(DataComponents.CUSTOM_NAME, Component.literal(entry.coinReward + " Monedas").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                    inventory.setItem(rewardSlot++, coinStack);
                }
                for (var ri : entry.itemRewards) {
                    Identifier itemId = Identifier.tryParse(ri.item);
                    var item = itemId != null ? BuiltInRegistries.ITEM.get(itemId).map(h -> h.value()).orElse(Items.BARRIER) : Items.BARRIER;
                    ItemStack rewardStack = new ItemStack(item, Math.min(ri.count, 64));
                    String name = ri.item.contains(":") ? ri.item.substring(ri.item.indexOf(":") + 1) : ri.item;
                    rewardStack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
                    List<Component> rewardLore = new ArrayList<>();
                    rewardLore.add(Component.literal("Cantidad: " + ri.count).withStyle(ChatFormatting.GRAY));
                    if (ri.chance < 1.0) {
                        rewardLore.add(Component.literal("Probabilidad: " + (int)(ri.chance * 100) + "%").withStyle(ChatFormatting.GRAY));
                    }
                    rewardStack.set(DataComponents.LORE, new ItemLore(rewardLore));
                    inventory.setItem(rewardSlot++, rewardStack);
                }

                boolean claimFull = entry.maxClaims > 0 && MissionState.getClaimCount(entry.id) >= entry.maxClaims;
                ItemStack claimStack = new ItemStack(Items.EMERALD);
                if (claimed) {
                    claimStack = new ItemStack(Items.EMERALD_BLOCK);
                    claimStack.set(DataComponents.CUSTOM_NAME, Component.literal("Reclamado").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                } else if (claimFull) {
                    claimStack = new ItemStack(Items.REDSTONE_BLOCK);
                    claimStack.set(DataComponents.CUSTOM_NAME, Component.literal("Ya no disponible").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                } else if (completed) {
                    claimStack = new ItemStack(Items.EMERALD);
                    claimStack.set(DataComponents.CUSTOM_NAME, Component.literal("\u00a1Reclamar Recompensa!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD, ChatFormatting.UNDERLINE));
                    claimStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                } else {
                    claimStack = new ItemStack(Items.BARRIER);
                    claimStack.set(DataComponents.CUSTOM_NAME, Component.literal("Bloqueado").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD));
                }
                inventory.setItem(40, claimStack);

                ItemStack backStack = new ItemStack(Items.ARROW);
                backStack.set(DataComponents.CUSTOM_NAME, Component.literal("Atr\u00e1s").withStyle(ChatFormatting.GREEN));
                inventory.setItem(45, backStack);

                ItemStack exitStack = new ItemStack(Items.BARRIER);
                exitStack.set(DataComponents.CUSTOM_NAME, Component.literal("Salir").withStyle(ChatFormatting.RED));
                inventory.setItem(53, exitStack);

            } catch (Exception e) {
                Extremo.LOGGER.error("Error in QuestDetailGui.setupSlots: {}", e.getMessage(), e);
            }
        }

        private void handleClaim() {
            if (entry == null) return;
            if (!(player instanceof ServerPlayer sp)) return;

            if (!MissionState.canClaim(player.getUUID(), questId)) return;
            boolean completed = MissionState.getProgress(player.getUUID(), questId) >= entry.amount;
            if (!completed) return;

            MissionState.claimReward(player.getUUID(), questId);

            if (entry.xpReward > 0) {
                sp.giveExperiencePoints(entry.xpReward);
            }
            if (entry.coinReward > 0) {
                for (int i = 0; i < entry.coinReward; i++) {
                    String coinId = "extremo:coin_copper";
                    Identifier coinIdentifier = Identifier.tryParse(coinId);
                    if (coinIdentifier != null) {
                        var coinItem = BuiltInRegistries.ITEM.get(coinIdentifier).map(h -> h.value()).orElse(Items.AIR);
                        if (coinItem != Items.AIR) {
                            player.getInventory().placeItemBackInInventory(new ItemStack(coinItem));
                        }
                    }
                }
            }
            for (var ri : entry.itemRewards) {
                if (ri.chance < 1.0 && Math.random() > ri.chance) continue;
                Identifier itemId = Identifier.tryParse(ri.item);
                if (itemId != null) {
                    var item = BuiltInRegistries.ITEM.get(itemId).map(h -> h.value()).orElse(Items.AIR);
                    if (item != Items.AIR) {
                        player.getInventory().placeItemBackInInventory(new ItemStack(item, ri.count));
                    }
                }
            }

            sp.sendSystemMessage(Component.literal("\u00a7a[Misiones] \u00a1Recompensa reclamada: " + entry.getDisplayName() + "!"));
            ((ServerPlayer) player).closeContainer();

            boolean shouldDelete = false;
            if (entry.maxClaims > 0 && MissionState.getClaimCount(questId) >= entry.maxClaims) {
                shouldDelete = true;
                List<String> claimers = MissionState.getClaimedPlayerNames(com.bestiarymod.Extremo.currentServer, questId);
                String playersStr = String.join("\u00a77, \u00a7f", claimers);
                com.bestiarymod.Extremo.currentServer.getPlayerList().broadcastSystemMessage(
                    Component.literal("\u00a7d\u26a0 \u00a7e" + entry.getDisplayName() + "\u00a7d completada por \u00a7f" + playersStr + "\u00a7d. \u00a7cYa no se puede completar."),
                    false
                );
            } else if (entry.isExpired() && !MissionState.hasUnclaimedCompletions(questId)) {
                shouldDelete = true;
            }
            if (shouldDelete) {
                MissionManager.autoDelete(questId);
                player.openMenu(new QuestListGui(0));
            } else {
                player.openMenu(new QuestDetailGui(questId));
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
            if (slotIndex == 45) {
                ((ServerPlayer) player).closeContainer();
                player.openMenu(new QuestListGui(0));
                return;
            }
            if (slotIndex == 40) {
                handleClaim();
                return;
            }
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

        @Override
        public boolean stillValid(Player player) { return true; }
    }
}
