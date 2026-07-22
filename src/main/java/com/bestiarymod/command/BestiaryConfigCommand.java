package com.bestiarymod.command;

import com.bestiarymod.Extremo;
import com.bestiarymod.config.BestiaryConfigManager;
import com.bestiarymod.config.BestiaryEntry;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.world.item.ItemStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandBuildContext;
import java.util.Collection;

public class BestiaryConfigCommand {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ALL_ENTITIES = (ctx, builder) -> {
        for (Identifier id : BuiltInRegistries.ENTITY_TYPE.keySet()) {
            builder.suggest(id.toString());
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_BESTIARY_ENTRIES = (ctx, builder) -> {
        for (BestiaryEntry entry : BestiaryConfigManager.getAllEntries()) {
            builder.suggest(entry.mobId);
        }
        return builder.buildFuture();
    };

    public static LiteralArgumentBuilder<CommandSourceStack> buildBestiaryNode(CommandBuildContext registryAccess) {
        return Commands.literal("bestiary")
                .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                .then(Commands.literal("list")
                        .executes(context -> {
                            CommandSourceStack src = context.getSource();
                            Collection<BestiaryEntry> entries = BestiaryConfigManager.getAllEntries();
                            src.sendSuccess(() -> Component.literal("\u00a76[Bestiary] Mobs configurados (" + entries.size() + "):"), false);
                            for (BestiaryEntry e : entries) {
                                src.sendSuccess(() -> Component.literal(" \u00a77- \u00a7f" + e.mobId + " \u00a77(" + e.levels.size() + " niveles)"), false);
                            }
                            return 1;
                        }))
                .then(Commands.literal("add")
                        .then(Commands.argument("entity", StringArgumentType.greedyString())
                                .suggests(SUGGEST_ALL_ENTITIES)
                                .executes(context -> {
                                    CommandSourceStack src = context.getSource();
                                    String mobId = context.getArgument("entity", String.class);
                                    Identifier id = Identifier.tryParse(mobId);
                                    if (id == null) { src.sendFailure(Component.literal("\u00a7cIdentificador de entidad inv\u00e1lido")); return 0; }
                                    if (BestiaryConfigManager.hasEntry(mobId)) {
                                        src.sendFailure(Component.literal("\u00a7cYa existe una entrada para " + mobId));
                                        return 0;
                                    }
                                    if (!BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
                                        src.sendFailure(Component.literal("\u00a7cEntidad desconocida: " + mobId));
                                        return 0;
                                    }
                                    BestiaryConfigManager.addEntry(mobId);
                                    src.sendSuccess(() -> Component.literal("\u00a7aEntrada de bestiario a\u00f1adida para " + mobId), false);
                                    return 1;
                                })))
                .then(Commands.literal("remove")
                        .then(Commands.argument("entity", StringArgumentType.greedyString())
                                .suggests(SUGGEST_BESTIARY_ENTRIES)
                                .executes(context -> {
                                    CommandSourceStack src = context.getSource();
                                    String mobId = context.getArgument("entity", String.class);
                                    Identifier id = Identifier.tryParse(mobId);
                                    if (id == null) { src.sendFailure(Component.literal("\u00a7cIdentificador de entidad inv\u00e1lido")); return 0; }
                                    if (!BestiaryConfigManager.hasEntry(mobId)) {
                                        src.sendFailure(Component.literal("\u00a7cNo hay entrada para " + mobId));
                                        return 0;
                                    }
                                    BestiaryConfigManager.deleteEntry(mobId);
                                    src.sendSuccess(() -> Component.literal("\u00a7aEntrada de bestiario eliminada para " + mobId), false);
                                    return 1;
                                })))
                .then(Commands.literal("setlevel")
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 9))
                                .then(Commands.argument("killCount", IntegerArgumentType.integer(1))
                                        .then(Commands.argument("entity", StringArgumentType.greedyString())
                                                .suggests(SUGGEST_BESTIARY_ENTRIES)
                                                .executes(context -> {
                                                    CommandSourceStack src = context.getSource();
                                                    int levelIdx = context.getArgument("level", Integer.class) - 1;
                                                    int killCount = context.getArgument("killCount", Integer.class);
                                                    String mobId = context.getArgument("entity", String.class);
                                                    Identifier id = Identifier.tryParse(mobId);
                                                    if (id == null) { src.sendFailure(Component.literal("\u00a7cIdentificador de entidad inv\u00e1lido")); return 0; }

                                                    BestiaryEntry entry = BestiaryConfigManager.getEntry(mobId);
                                                    if (entry == null) {
                                                        src.sendFailure(Component.literal("\u00a7cNo hay entrada para " + mobId + ". Usa /extremo bestiary add primero."));
                                                        return 0;
                                                    }
                                                    while (entry.levels.size() <= levelIdx) {
                                                        entry.levels.add(new BestiaryEntry.BestiaryLevel(0, 0));
                                                    }
                                                    entry.levels.get(levelIdx).killCount = killCount;
                                                    BestiaryConfigManager.saveEntry(entry);
                                                    src.sendSuccess(() -> Component.literal("\u00a7aNivel " + (levelIdx + 1) + " para " + mobId + " establecido a " + killCount + " asesinatos"), false);
                                                    return 1;
                                                })))))
                .then(Commands.literal("setxp")
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 9))
                                .then(Commands.argument("xp", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("entity", StringArgumentType.greedyString())
                                                .suggests(SUGGEST_BESTIARY_ENTRIES)
                                                .executes(context -> {
                                                    CommandSourceStack src = context.getSource();
                                                    int levelIdx = context.getArgument("level", Integer.class) - 1;
                                                    int xp = context.getArgument("xp", Integer.class);
                                                    String mobId = context.getArgument("entity", String.class);
                                                    Identifier id = Identifier.tryParse(mobId);
                                                    if (id == null) { src.sendFailure(Component.literal("\u00a7cIdentificador de entidad inv\u00e1lido")); return 0; }

                                                    BestiaryEntry entry = BestiaryConfigManager.getEntry(mobId);
                                                    if (entry == null) {
                                                        src.sendFailure(Component.literal("\u00a7cNo hay entrada para " + mobId));
                                                        return 0;
                                                    }
                                                    if (levelIdx >= entry.levels.size()) {
                                                        src.sendFailure(Component.literal("\u00a7cNivel " + (levelIdx + 1) + " no configurado. Establece asesinatos primero."));
                                                        return 0;
                                                    }
                                                    entry.levels.get(levelIdx).xpReward = xp;
                                                    BestiaryConfigManager.saveEntry(entry);
                                                    src.sendSuccess(() -> Component.literal("\u00a7aRecompensa de XP del nivel " + (levelIdx + 1) + " establecida a " + xp), false);
                                                    return 1;
                                                })))))
                .then(Commands.literal("additem")
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 9))
                                .then(Commands.argument("item", ItemArgument.item(registryAccess))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                                .then(Commands.argument("entity", StringArgumentType.greedyString())
                                                        .suggests(SUGGEST_BESTIARY_ENTRIES)
                                                        .executes(context -> {
                                                            CommandSourceStack src = context.getSource();
                                                            int levelIdx = context.getArgument("level", Integer.class) - 1;
                                                            ItemStack itemStack = ItemArgument.getItem(context, "item").createItemStack(1);
                                                            int count = context.getArgument("count", Integer.class);
                                                            String mobId = context.getArgument("entity", String.class);
                                                            Identifier id = Identifier.tryParse(mobId);
                                                            if (id == null) { src.sendFailure(Component.literal("\u00a7cIdentificador de entidad inv\u00e1lido")); return 0; }

                                                            BestiaryEntry entry = BestiaryConfigManager.getEntry(mobId);
                                                            if (entry == null) {
                                                                src.sendFailure(Component.literal("\u00a7cNo hay entrada para " + mobId));
                                                                return 0;
                                                            }
                                                            if (levelIdx >= entry.levels.size()) {
                                                                src.sendFailure(Component.literal("\u00a7cNivel " + (levelIdx + 1) + " no configurado."));
                                                                return 0;
                                                            }
                                                            String itemId = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();
                                                            entry.levels.get(levelIdx).itemRewards.add(new BestiaryEntry.RewardItem(itemId, count, false));
                                                            BestiaryConfigManager.saveEntry(entry);
                                                            src.sendSuccess(() -> Component.literal("\u00a7aA\u00f1adido " + count + "x " + itemId + " a recompensas del nivel " + (levelIdx + 1)), false);
                                                            return 1;
                                                        }))))))
                .then(Commands.literal("reload")
                        .executes(context -> {
                            CommandSourceStack src = context.getSource();
                            BestiaryConfigManager.reload(net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir());
                            src.sendSuccess(() -> Component.literal("\u00a7a\u00a1Configuraci\u00f3n del bestiario recargada!"), false);
                            return 1;
                        }))
                .then(Commands.literal("help")
                        .executes(context -> {
                            CommandSourceStack src = context.getSource();
                            src.sendSuccess(() -> Component.literal("\u00a76\u00a7lComandos de configuraci\u00f3n del bestiario:"), false);
                            src.sendSuccess(() -> Component.literal(" \u00a7e/list \u00a77- Lista mobs configurados"), false);
                            src.sendSuccess(() -> Component.literal(" \u00a7e/add <entidad> \u00a77- A\u00f1ade un mob"), false);
                            src.sendSuccess(() -> Component.literal(" \u00a7e/remove <entidad> \u00a77- Elimina un mob"), false);
                            src.sendSuccess(() -> Component.literal(" \u00a7e/setlevel <1-9> <asesinatos> <entidad> \u00a77- Establece asesinatos para nivel"), false);
                            src.sendSuccess(() -> Component.literal(" \u00a7e/setxp <1-9> <xp> <entidad> \u00a77- Establece recompensa de XP"), false);
                            src.sendSuccess(() -> Component.literal(" \u00a7e/additem <1-9> <item> <cantidad> <entidad> \u00a77- A\u00f1ade recompensa de item"), false);
                            src.sendSuccess(() -> Component.literal(" \u00a7e/reload \u00a77- Recarga configuraci\u00f3n desde disco"), false);
                            return 1;
                        }));
    }
}
