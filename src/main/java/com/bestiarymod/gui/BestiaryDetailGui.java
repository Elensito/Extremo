package com.bestiarymod.gui;

import com.bestiarymod.Extremo;
import com.bestiarymod.config.BestiaryConfigManager;
import com.bestiarymod.config.BestiaryEntry;
import com.bestiarymod.data.BestiaryState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.component.ItemLore;
import java.util.ArrayList;
import java.util.List;

public class BestiaryDetailGui implements MenuProvider {
    private final String mobId;

    public BestiaryDetailGui(String mobId) {
        this.mobId = mobId;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Detalle del Bestiario");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new Handler(syncId, inv, player, mobId);
    }

    private static class Handler extends AbstractContainerMenu {
        private final SimpleContainer inventory = new SimpleContainer(54);
        private final Player player;
        private final String mobId;
        private final BestiaryEntry entry;

        Handler(int syncId, Inventory playerInventory, Player player, String mobId) {
            super(null, syncId);
            this.player = player;
            this.mobId = mobId;
            this.entry = BestiaryConfigManager.getEntry(mobId);

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

                int totalKills = BestiaryState.getKills(player.getUUID(), mobId);

                ItemStack infoStack = new ItemStack(Items.BOOK);
                infoStack.set(DataComponents.CUSTOM_NAME, Component.literal(formatName(mobId)).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                var infoLore = new ArrayList<Component>();
                infoLore.add(Component.literal(""));
                infoLore.add(Component.literal("Asesinatos totales: " + totalKills).withStyle(ChatFormatting.WHITE));
                infoLore.add(Component.literal("Niveles: " + entry.levels.size()).withStyle(ChatFormatting.YELLOW));
                infoStack.set(DataComponents.LORE, new ItemLore(infoLore));
                inventory.setItem(4, infoStack);

                int maxLevels = Math.min(entry.levels.size(), 9);
                for (int i = 0; i < maxLevels; i++) {
                    var level = entry.levels.get(i);
                    if (level == null) continue;
                    int slotIndex = 18 + i;

                    boolean claimed = BestiaryState.getClaimedLevels(player.getUUID(), mobId).contains(i);
                    boolean canClaim = totalKills >= level.killCount && !claimed;

                    ItemStack slotStack = entry.getDisplayItem().copy();
                    if (claimed) {
                        slotStack.set(DataComponents.CUSTOM_NAME, Component.literal("Nivel " + (i + 1) + " - COMPLETADO").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                        slotStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                    } else {
                        slotStack.set(DataComponents.CUSTOM_NAME, Component.literal("Nivel " + (i + 1)).withStyle(
                                canClaim ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD));
                    }

                    int required = level.killCount;
                    int percent = required > 0 ? (int) Math.floor((double) totalKills / required * 100) : 0;
                    if (percent > 100) percent = 100;

                    var lore = new ArrayList<Component>();
                    lore.add(Component.literal(""));
                    lore.add(Component.literal("Asesinatos: " + totalKills + "/" + required).withStyle(ChatFormatting.GRAY));
                    lore.add(Component.literal("Progreso: " + percent + "%").withStyle(
                            percent >= 100 ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
                    if (level.xpReward > 0) {
                        lore.add(Component.literal("Recompensa de XP: " + level.xpReward).withStyle(ChatFormatting.AQUA));
                    }
                    if (!level.itemRewards.isEmpty()) {
                        lore.add(Component.literal("Recompensas de objetos:").withStyle(ChatFormatting.LIGHT_PURPLE));
                        for (var ri : level.itemRewards) {
                            if (ri == null) continue;
                            String itemName = ri.itemId.contains(":") ? ri.itemId.substring(ri.itemId.indexOf(":") + 1) : ri.itemId;
                            lore.add(Component.literal("  - " + ri.count + "x " + itemName + (ri.enchanted ? " (Encantado)" : "")).withStyle(ChatFormatting.LIGHT_PURPLE));
                        }
                    }
                    if (claimed) {
                        lore.add(Component.literal(""));
                        lore.add(Component.literal("\u2713 Reclamado").withStyle(ChatFormatting.GREEN));
                    } else if (canClaim) {
                        lore.add(Component.literal(""));
                        lore.add(Component.literal("\u00a1Click para reclamar!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                    } else {
                        int remaining = required - totalKills;
                        lore.add(Component.literal(""));
                        lore.add(Component.literal(remaining + " asesinatos m\u00e1s necesarios").withStyle(ChatFormatting.RED));
                    }

                    slotStack.set(DataComponents.LORE, new ItemLore(lore));
                    inventory.setItem(slotIndex, slotStack);
                }

                ItemStack backStack = new ItemStack(Items.ARROW);
                backStack.set(DataComponents.CUSTOM_NAME, Component.literal("Atras").withStyle(ChatFormatting.GREEN));
                inventory.setItem(45, backStack);

                ItemStack exitStack = new ItemStack(Items.BARRIER);
                exitStack.set(DataComponents.CUSTOM_NAME, Component.literal("Salir").withStyle(ChatFormatting.RED));
                inventory.setItem(53, exitStack);
            } catch (Exception e) {
                Extremo.LOGGER.error("Error in BestiaryDetailGui.setupSlots: {}", e.getMessage(), e);
            }
        }

        private void handleSlotClick(int slot) {
            if (entry == null) return;
            int levelIndex = slot - 18;
            if (levelIndex < 0 || levelIndex >= entry.levels.size()) return;

            int totalKills = BestiaryState.getKills(player.getUUID(), mobId);
            boolean claimed = BestiaryState.getClaimedLevels(player.getUUID(), mobId).contains(levelIndex);
            boolean canClaim = totalKills >= entry.levels.get(levelIndex).killCount && !claimed;

            if (canClaim && player instanceof ServerPlayer sp) {
                BestiaryState.claimReward(player.getUUID(), mobId, levelIndex);

                var level = entry.levels.get(levelIndex);
                if (level.xpReward > 0) {
                    sp.giveExperiencePoints(level.xpReward);
                }
                for (var ri : level.itemRewards) {
                    var itemId = Identifier.tryParse(ri.itemId);
                    if (itemId != null) {
                        var item = BuiltInRegistries.ITEM.get(itemId).map(ref -> ref.value()).orElse(Items.AIR);
                        if (item != Items.AIR) {
                            var rewardStack = new ItemStack(item, ri.count);
                            if (ri.enchanted) {
                                rewardStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                            }
                            player.getInventory().placeItemBackInInventory(rewardStack);
                        }
                    }
                }

                sp.sendSystemMessage(Component.literal("\u00a7a[Bestiario] Recompensa reclamada para el nivel " + (levelIndex + 1) + " de " + formatName(mobId) + "!"));

                ((ServerPlayer) player).closeContainer();
                player.openMenu(new BestiaryDetailGui(mobId));
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
                player.openMenu(new BestiaryMainGui(0));
                return;
            }
            handleSlotClick(slotIndex);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

        @Override
        public boolean stillValid(Player player) { return true; }

        private String formatName(String id) {
            String name = id.contains(":") ? id.substring(id.indexOf(":") + 1) : id;
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
