package com.bestiarymod.command;

import com.bestiarymod.Extremo;
import com.bestiarymod.mission.MissionEntry;
import com.bestiarymod.mission.MissionManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import java.util.concurrent.CompletableFuture;

public class QuestConfigCommand {

    public static SuggestionProvider<CommandSourceStack> suggestQuestIds = (ctx, builder) -> {
        for (String id : MissionManager.getAllEntries().stream().map(e -> e.id).toList()) {
            builder.suggest(id);
        }
        return builder.buildFuture();
    };

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildMissionNode(CommandBuildContext buildContext) {
        return Commands.literal("misiones")
            .then(Commands.literal("create")
                .then(Commands.argument("id", StringArgumentType.word())
                    .then(Commands.argument("type", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            builder.suggest("kill");
                            builder.suggest("collect");
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("target", StringArgumentType.greedyString())
                            .suggests(QuestConfigCommand::suggestTargets)
                            .executes(ctx -> executeCreate(ctx, 1))
                        )
                    )
                )
                .executes(ctx -> {
                    ctx.getSource().sendFailure(Component.literal("\u00a7cUso: /extremo misiones create <id> <kill|collect> <target> [amount]"));
                    return 0;
                })
            )
            .then(Commands.literal("additem")
                .then(Commands.argument("id", StringArgumentType.word())
                    .suggests(suggestQuestIds)
                    .then(Commands.argument("item", ItemArgument.item(buildContext))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                            .executes(ctx -> executeAddItem(ctx, 1.0))
                            .then(Commands.argument("chance", IntegerArgumentType.integer(1, 100))
                                .executes(ctx -> executeAddItem(ctx, ctx.getArgument("chance", Integer.class) / 100.0))
                            )
                        )
                    )
                )
                .executes(ctx -> {
                    ctx.getSource().sendFailure(Component.literal("\u00a7cUso: /extremo misiones additem <id> <item> <count> [chance]"));
                    return 0;
                })
            )
            .then(Commands.literal("removeitem")
                .then(Commands.argument("id", StringArgumentType.word())
                    .suggests(suggestQuestIds)
                    .then(Commands.argument("index", IntegerArgumentType.integer(0))
                        .executes(ctx -> executeRemoveItem(ctx))
                    )
                )
                .executes(ctx -> {
                    ctx.getSource().sendFailure(Component.literal("\u00a7cUso: /extremo misiones removeitem <id> <index>"));
                    return 0;
                })
            )
            .then(Commands.literal("delete")
                .then(Commands.argument("id", StringArgumentType.word())
                    .suggests(suggestQuestIds)
                    .executes(ctx -> executeDelete(ctx))
                )
                .executes(ctx -> {
                    ctx.getSource().sendFailure(Component.literal("\u00a7cUso: /extremo misiones delete <id>"));
                    return 0;
                })
            )
            .then(Commands.literal("setxp")
                .then(Commands.argument("id", StringArgumentType.word())
                    .suggests(suggestQuestIds)
                    .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                        .executes(ctx -> executeSetXp(ctx))
                    )
                )
                .executes(ctx -> {
                    ctx.getSource().sendFailure(Component.literal("\u00a7cUso: /extremo misiones setxp <id> <cantidad>"));
                    return 0;
                })
            )
            .then(Commands.literal("settime")
                .then(Commands.argument("id", StringArgumentType.word())
                    .suggests(suggestQuestIds)
                    .then(Commands.argument("minutes", IntegerArgumentType.integer(0))
                        .executes(ctx -> executeSetTime(ctx))
                    )
                )
                .executes(ctx -> {
                    ctx.getSource().sendFailure(Component.literal("\u00a7cUso: /extremo misiones settime <id> <minutos>"));
                    return 0;
                })
            )
            .then(Commands.literal("setmaxclaims")
                .then(Commands.argument("id", StringArgumentType.word())
                    .suggests(suggestQuestIds)
                    .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                        .executes(ctx -> executeSetMaxClaims(ctx))
                    )
                )
                .executes(ctx -> {
                    ctx.getSource().sendFailure(Component.literal("\u00a7cUso: /extremo misiones setmaxclaims <id> <cantidad>"));
                    return 0;
                })
            )
            .then(Commands.literal("list")
                .executes(ctx -> executeList(ctx))
            )
            .then(Commands.literal("info")
                .then(Commands.argument("id", StringArgumentType.word())
                    .suggests(suggestQuestIds)
                    .executes(ctx -> executeInfo(ctx))
                )
                .executes(ctx -> {
                    ctx.getSource().sendFailure(Component.literal("\u00a7cUso: /extremo misiones info <id>"));
                    return 0;
                })
            )
            .then(Commands.literal("reload")
                .executes(ctx -> {
                    MissionManager.reload(FabricLoader.getInstance().getConfigDir());
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a7aMisiones recargadas!"), false);
                    return 1;
                })
            )
            .then(Commands.literal("help")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a7e--- Comandos de Misiones (/extremo misiones) ---"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a7a  create <id> <kill|collect> <target> [amount]"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a77    Crea una nueva misi\u00f3n"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a7a  setxp <id> <cantidad>"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a77    Asigna XP como recompensa"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a7a  settime <id> <minutos>"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a77    Hace la misi\u00f3n temporal (se autoborra al vencer)"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a7a  setmaxclaims <id> <cantidad>"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a77    L\u00edmite de jugadores que pueden reclamar la misi\u00f3n"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a7a  delete <id>"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a77    Elimina una misi\u00f3n"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a7a  list"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a77    Lista todas las misiones"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a7a  info <id>"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a77    Muestra detalle de una misi\u00f3n"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a7a  additem <id> <item> <count> [chance]"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a77    A\u00f1ade recompensa de item"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a7a  removeitem <id> <index>"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a77    Elimina recompensa de item"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a7a  reload"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a77    Recarga config de misiones"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal("\u00a7e---"), false);
                    return 1;
                })
            );
    }

    private static CompletableFuture<Suggestions> suggestTargets(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String type = ctx.getArgument("type", String.class);
        String current = builder.getInput().substring(builder.getStart());
        boolean alreadyComplete = current.contains(" ");
        if (!alreadyComplete) {
            Component tooltip = Component.literal("\u00a77[amount]");
            if (type.equals("kill")) {
                for (Identifier id : BuiltInRegistries.ENTITY_TYPE.keySet()) {
                    builder.suggest(id.toString() + " 1", tooltip);
                }
            } else if (type.equals("collect")) {
                for (Identifier id : BuiltInRegistries.ITEM.keySet()) {
                    builder.suggest(id.toString() + " 1", tooltip);
                }
            }
        }
        return builder.buildFuture();
    }

    private static int executeCreate(CommandContext<CommandSourceStack> ctx, int defaultAmount) {
        CommandSourceStack src = ctx.getSource();
        String id = ctx.getArgument("id", String.class);
        String type = ctx.getArgument("type", String.class);

        String[] tokens = ctx.getArgument("target", String.class).split(" ");
        String target = tokens[0];
        int amount = defaultAmount;
        if (tokens.length > 1) {
            try { amount = Integer.parseInt(tokens[1]); } catch (NumberFormatException ignored) {}
        }

        if (!type.equals("kill") && !type.equals("collect")) {
            src.sendFailure(Component.literal("\u00a7cTipo inv\u00e1lido. Usa 'kill' o 'collect'"));
            return 0;
        }

        if (MissionManager.hasEntry(id)) {
            src.sendFailure(Component.literal("\u00a7cYa existe una misi\u00f3n con id: " + id));
            return 0;
        }

        if (target.contains(":")) {
            if (type.equals("kill")) {
                if (BuiltInRegistries.ENTITY_TYPE.get(Identifier.tryParse(target)).isEmpty()) {
                    src.sendFailure(Component.literal("\u00a7cEntidad no encontrada: " + target));
                    return 0;
                }
            } else {
                if (BuiltInRegistries.ITEM.get(Identifier.tryParse(target)).isEmpty()) {
                    src.sendFailure(Component.literal("\u00a7cItem no encontrado: " + target));
                    return 0;
                }
            }
        } else {
            src.sendFailure(Component.literal("\u00a7cEl target debe ser un ID con namespace (ej: minecraft:zombie)"));
            return 0;
        }

        MissionEntry entry = new MissionEntry();
        entry.id = id;
        entry.type = type;
        entry.target = target;
        entry.amount = Math.max(1, amount);

        MissionManager.saveEntry(entry);
        src.sendSuccess(() -> Component.literal("\u00a7aMisi\u00f3n creada: " + id), false);
        return 1;
    }

    private static int executeSetXp(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        String id = ctx.getArgument("id", String.class);
        MissionEntry entry = MissionManager.getEntry(id);
        if (entry == null) {
            src.sendFailure(Component.literal("\u00a7cMisi\u00f3n no encontrada: " + id));
            return 0;
        }
        entry.xpReward = ctx.getArgument("amount", Integer.class);
        MissionManager.saveEntry(entry);
        src.sendSuccess(() -> Component.literal("\u00a7aXP recompensa de " + id + " actualizado a " + entry.xpReward), false);
        return 1;
    }

    private static int executeSetTime(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        String id = ctx.getArgument("id", String.class);
        MissionEntry entry = MissionManager.getEntry(id);
        if (entry == null) {
            src.sendFailure(Component.literal("\u00a7cMisi\u00f3n no encontrada: " + id));
            return 0;
        }
        int minutes = ctx.getArgument("minutes", Integer.class);
        if (minutes > 0) {
            entry.expiresAt = System.currentTimeMillis() + minutes * 60000L;
        } else {
            entry.expiresAt = 0;
        }
        MissionManager.saveEntry(entry);
        if (minutes > 0) {
            src.sendSuccess(() -> Component.literal("\u00a7aMisi\u00f3n " + id + " expirar\u00e1 en " + minutes + " minutos"), false);
        } else {
            src.sendSuccess(() -> Component.literal("\u00a7aMisi\u00f3n " + id + " ya no expira"), false);
        }
        return 1;
    }

    private static int executeSetMaxClaims(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        String id = ctx.getArgument("id", String.class);
        MissionEntry entry = MissionManager.getEntry(id);
        if (entry == null) {
            src.sendFailure(Component.literal("\u00a7cMisi\u00f3n no encontrada: " + id));
            return 0;
        }
        entry.maxClaims = ctx.getArgument("amount", Integer.class);
        MissionManager.saveEntry(entry);
        src.sendSuccess(() -> Component.literal("\u00a7aL\u00edmite de jugadores de " + id + " actualizado a " + entry.maxClaims), false);
        return 1;
    }

    private static int executeAddItem(CommandContext<CommandSourceStack> ctx, double chance) {
        CommandSourceStack src = ctx.getSource();
        String id = ctx.getArgument("id", String.class);
        MissionEntry entry = MissionManager.getEntry(id);
        if (entry == null) {
            src.sendFailure(Component.literal("\u00a7cMisi\u00f3n no encontrada: " + id));
            return 0;
        }

        ItemInput itemInput = ctx.getArgument("item", ItemInput.class);
        int count = ctx.getArgument("count", Integer.class);
        Identifier itemId = BuiltInRegistries.ITEM.getKey(itemInput.item().value());

        MissionEntry.ItemReward reward = new MissionEntry.ItemReward();
        reward.item = itemId.toString();
        reward.count = count;
        reward.chance = chance;
        entry.itemRewards.add(reward);

        MissionManager.saveEntry(entry);
        src.sendSuccess(() -> Component.literal("\u00a7aItem a\u00f1adido a " + id + ": " + itemId.getPath() + " x" + count), false);
        return 1;
    }

    private static int executeRemoveItem(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        String id = ctx.getArgument("id", String.class);
        MissionEntry entry = MissionManager.getEntry(id);
        if (entry == null) {
            src.sendFailure(Component.literal("\u00a7cMisi\u00f3n no encontrada: " + id));
            return 0;
        }

        int index = ctx.getArgument("index", Integer.class);
        if (index < 0 || index >= entry.itemRewards.size()) {
            src.sendFailure(Component.literal("\u00a7c\u00cdndice inv\u00e1lido. Hay " + entry.itemRewards.size() + " items."));
            return 0;
        }

        MissionEntry.ItemReward removed = entry.itemRewards.remove(index);
        MissionManager.saveEntry(entry);
        src.sendSuccess(() -> Component.literal("\u00a7aItem eliminado: " + removed.item), false);
        return 1;
    }

    private static int executeDelete(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        String id = ctx.getArgument("id", String.class);
        if (!MissionManager.hasEntry(id)) {
            src.sendFailure(Component.literal("\u00a7cMisi\u00f3n no encontrada: " + id));
            return 0;
        }
        MissionManager.deleteEntry(id);
        src.sendSuccess(() -> Component.literal("\u00a7aMisi\u00f3n eliminada: " + id), false);
        return 1;
    }

    private static int executeList(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        var entries = MissionManager.getAllEntries();
        if (entries.isEmpty()) {
            src.sendSuccess(() -> Component.literal("\u00a7eNo hay misiones configuradas. Usa /extremo misiones create para crear una."), false);
            return 1;
        }
        src.sendSuccess(() -> Component.literal("\u00a7e--- Misiones (" + entries.size() + ") ---"), false);
        for (MissionEntry e : entries) {
            src.sendSuccess(() -> Component.literal("\u00a7a  " + e.id + "\u00a77 [\u00a7f" + e.type + "\u00a77] \u00a7f" + e.getDisplayName()), false);
        }
        return 1;
    }

    private static int executeInfo(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        String id = ctx.getArgument("id", String.class);
        MissionEntry entry = MissionManager.getEntry(id);
        if (entry == null) {
            src.sendFailure(Component.literal("\u00a7cMisi\u00f3n no encontrada: " + id));
            return 0;
        }
        src.sendSuccess(() -> Component.literal("\u00a7e--- " + entry.id + " ---"), false);
        src.sendSuccess(() -> Component.literal("\u00a77Tipo: \u00a7f" + entry.type), false);
        src.sendSuccess(() -> Component.literal("\u00a77Target: \u00a7f" + entry.target), false);
        src.sendSuccess(() -> Component.literal("\u00a77Cantidad: \u00a7f" + entry.amount), false);
        src.sendSuccess(() -> Component.literal("\u00a77Recompensas:"), false);
        if (entry.xpReward > 0) src.sendSuccess(() -> Component.literal("\u00a7a  - " + entry.xpReward + " XP"), false);
        if (entry.coinReward > 0) src.sendSuccess(() -> Component.literal("\u00a76  - " + entry.coinReward + " monedas"), false);
        for (int i = 0; i < entry.itemRewards.size(); i++) {
            var ri = entry.itemRewards.get(i);
            final int idx = i;
            final var reward = ri;
            src.sendSuccess(() -> Component.literal("\u00a7d  [" + idx + "] " + reward.item + " x" + reward.count + (reward.chance < 1.0 ? " (" + (int)(reward.chance*100) + "%)" : "")), false);
        }
        if (entry.expiresAt > 0) {
            long remaining = entry.expiresAt - System.currentTimeMillis();
            if (remaining > 0) {
                src.sendSuccess(() -> Component.literal("\u00a7eExpira en " + (remaining / 60000) + " minutos"), false);
            } else {
                src.sendSuccess(() -> Component.literal("\u00a7cExpirada"), false);
            }
        }
        if (entry.maxClaims > 0) {
            src.sendSuccess(() -> Component.literal("\u00a7dMax jugadores: " + entry.maxClaims), false);
        }
        return 1;
    }
}
