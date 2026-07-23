package com.bestiarymod.command;

import com.bestiarymod.Extremo;
import com.bestiarymod.access.ConsumableDataAccessor;
import com.bestiarymod.config.BestiaryConfigManager;
import com.bestiarymod.data.BestiaryState;
import com.bestiarymod.spawn.SpawnConfigManager;
import com.bestiarymod.entity.ModEntities;
import com.bestiarymod.entity.SkeletonDasher;
import com.bestiarymod.item.ModItems;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerLevel;
import net.fabricmc.loader.api.FabricLoader;
import java.util.concurrent.CompletableFuture;

public class ExtremoCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) -> {
            dispatcher.register(Commands.literal("extremo")
                .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                .then(Commands.literal("spawn")
                    .then(Commands.argument("mob", StringArgumentType.word())
                        .suggests(ExtremoCommand::suggestCustomMobs)
                        .executes(ctx -> executeSpawn(ctx, buildContext))
                    )
                    .executes(ctx -> {
                        ctx.getSource().sendFailure(Component.literal("\u00a7cUso: /extremo spawn <mob>"));
                        return 0;
                    })
                )
                .then(Commands.literal("give")
                    .then(Commands.argument("item", StringArgumentType.word())
                        .suggests(ExtremoCommand::suggestCustomItems)
                        .executes(ctx -> executeGive(ctx))
                    )
                    .executes(ctx -> {
                        ctx.getSource().sendFailure(Component.literal("\u00a7cUso: /extremo give <item>"));
                        return 0;
                    })
                )
                .then(BestiaryConfigCommand.buildBestiaryNode(buildContext))
                .then(Commands.literal("resetconsumables")
                    .then(Commands.argument("player", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (ServerPlayer p : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                                builder.suggest(p.getScoreboardName());
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            CommandSourceStack src = ctx.getSource();
                            String targetName = ctx.getArgument("player", String.class);
                            ServerPlayer target = src.getServer().getPlayerList().getPlayerByName(targetName);
                            if (target == null) {
                                src.sendFailure(Component.literal("\u00a7cJugador no encontrado: " + targetName));
                                return 0;
                            }
                            ConsumableDataAccessor accessor = (ConsumableDataAccessor) target;
                            java.util.Set<String> consumed = accessor.getConsumedItems();

                            if (consumed.contains("enchanted_iron_ingot")) {
                                var attr = target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);
                                if (attr != null) {
                                    attr.setBaseValue(attr.getBaseValue() - 1.0);
                                }
                            }

                            accessor.setConsumedItems(new java.util.HashSet<>());
                            src.sendSuccess(() -> Component.literal("\u00a7aConsumibles reestablecidos para " + targetName), false);
                            target.sendSystemMessage(Component.literal("\u00a7aTus objetos consumibles han sido reestablecidos por un administrador."));
                            return 1;
                        })
                    )
                    .executes(ctx -> {
                        ctx.getSource().sendFailure(Component.literal("\u00a7cUso: /extremo resetconsumables <jugador>"));
                        return 0;
                    })
                )
                .then(Commands.literal("reload")
                        .executes(context -> {
                            CommandSourceStack src = context.getSource();
                            BestiaryConfigManager.reload(FabricLoader.getInstance().getConfigDir());
                            BestiaryState.load(Extremo.currentServer);
                            SpawnConfigManager.reload();
                            src.sendSuccess(() -> Component.literal("\u00a7a\u00a1Mod Extremo recargado completamente!"), false);
                            return 1;
                        }))
                .executes(ctx -> {
                    ctx.getSource().sendFailure(Component.literal("\u00a7cUso: /extremo spawn <mob> | /extremo give <item> | /extremo bestiary help | /extremo resetconsumables <jugador> | /extremo reload"));
                    return 0;
                })
            );
        });
    }

    private static int executeSpawn(
        com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
        HolderLookup.Provider lookupProvider
    ) {
        CommandSourceStack src = context.getSource();
        if (!(src.getEntity() instanceof ServerPlayer player)) {
            src.sendFailure(Component.literal("\u00a7cSolo los jugadores pueden usar este comando"));
            return 0;
        }
        String mobName = context.getArgument("mob", String.class);
        try {
            if (mobName.equals("dasher")) {
                spawnDasher(player, lookupProvider);
                src.sendSuccess(() -> Component.literal("\u00a7aDasher invocado"), false);
                return 1;
            }
            ServerLevel level = (ServerLevel) player.level();
            Mob mob = com.bestiarymod.spawn.SpawnerRegistry.create("extremo:" + mobName, level);
            if (mob == null) {
                src.sendFailure(Component.literal("\u00a7cMob desconocido: " + mobName + ". Disponibles: dasher, hechizera, cave_brute"));
                return 0;
            }
            mob.setPos(player.getX(), player.getY() + 2, player.getZ());
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(player.blockPosition()), net.minecraft.world.entity.EntitySpawnReason.COMMAND, null);
            level.addFreshEntity(mob);
            src.sendSuccess(() -> Component.literal("\u00a7a" + mobName + " invocado"), false);
            return 1;
        } catch (Exception e) {
            Extremo.LOGGER.error("Error al invocar mob", e);
            src.sendFailure(Component.literal("\u00a7cError al invocar mob: " + e.getMessage()));
            return 0;
        }
    }

    private static CompletableFuture<Suggestions> suggestCustomMobs(
        CommandContext<CommandSourceStack> context, SuggestionsBuilder builder
    ) {
        for (Identifier id : BuiltInRegistries.ENTITY_TYPE.keySet()) {
            if (id.getNamespace().equals(Extremo.MOD_ID)) {
                builder.suggest(id.getPath());
            }
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestCustomItems(
        CommandContext<CommandSourceStack> context, SuggestionsBuilder builder
    ) {
        for (Identifier id : BuiltInRegistries.ITEM.keySet()) {
            if (id.getNamespace().equals(Extremo.MOD_ID)) {
                builder.suggest(id.getPath());
            }
        }
        return builder.buildFuture();
    }

    private static int executeGive(CommandContext<CommandSourceStack> context) {
        CommandSourceStack src = context.getSource();
        if (!(src.getEntity() instanceof ServerPlayer player)) {
            src.sendFailure(Component.literal("\u00a7cSolo los jugadores pueden usar este comando"));
            return 0;
        }
        String itemName = context.getArgument("item", String.class);
        Identifier id = Identifier.fromNamespaceAndPath(Extremo.MOD_ID, itemName);
        net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.get(id).map(holder -> holder.value()).orElse(null);
        if (item == null || item == net.minecraft.world.item.Items.AIR) {
            src.sendFailure(Component.literal("\u00a7cItem desconocido: " + itemName));
            return 0;
        }
        player.getInventory().add(new ItemStack(item));
        src.sendSuccess(() -> Component.literal("\u00a7a" + itemName + " recibido"), false);
        return 1;
    }

    private static void spawnDasher(ServerPlayer player, HolderLookup.Provider lookupProvider) {
        SkeletonDasher dasher = new SkeletonDasher(ModEntities.DASHER, player.level());
        dasher.setCustomName(Component.literal("\u00a76Dasher"));
        dasher.setCustomNameVisible(true);
        dasher.setPos(player.getX(), player.getY() + 2, player.getZ());
        player.level().addFreshEntity(dasher);

        ItemStack crossbow = new ItemStack(Items.CROSSBOW);
        var enchantRegistry = lookupProvider.lookup(Registries.ENCHANTMENT).orElse(null);
        if (enchantRegistry != null) {
            var multishot = enchantRegistry.get(
                net.minecraft.world.item.enchantment.Enchantments.MULTISHOT
            ).orElse(null);
            if (multishot != null) {
                crossbow.enchant(multishot, 1);
            }
        }
        crossbow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.ofNonEmpty(java.util.List.of(new ItemStack(Items.ARROW))));
        dasher.setItemInHand(InteractionHand.MAIN_HAND, crossbow);
        dasher.setDropChance(EquipmentSlot.MAINHAND, 0.0f);

        ItemStack boots = new ItemStack(Items.DIAMOND_BOOTS);
        if (enchantRegistry != null) {
            var protection = enchantRegistry.get(
                net.minecraft.world.item.enchantment.Enchantments.PROTECTION
            ).orElse(null);
            if (protection != null) {
                boots.enchant(protection, 5);
            }
        }
        dasher.setItemSlot(EquipmentSlot.FEET, boots);
        dasher.setDropChance(EquipmentSlot.FEET, 0.0f);

        ItemStack tippedArrow = new ItemStack(Items.TIPPED_ARROW, 64);
        tippedArrow.set(DataComponents.POTION_CONTENTS, new PotionContents(
            java.util.Optional.empty(),
            java.util.Optional.empty(),
            java.util.List.of(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOWNESS, 200, 0)),
            java.util.Optional.empty()
        ));
        dasher.setItemInHand(InteractionHand.OFF_HAND, tippedArrow);
        dasher.setDropChance(EquipmentSlot.OFFHAND, 0.0f);
    }

}
