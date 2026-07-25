package com.bestiarymod.command;

import com.bestiarymod.Extremo;
import com.bestiarymod.gui.QuestListGui;
import com.bestiarymod.mission.MissionState;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

public class QuestCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("misiones")
                    .executes(context -> {
                        CommandSourceStack source = context.getSource();
                        if (source.getEntity() instanceof ServerPlayer player) {
                            MissionState.refreshCollectProgress(player);
                            try {
                                player.openMenu(new SimpleMenuProvider(
                                        (syncId, inv, p) -> new QuestListGui(0).createMenu(syncId, inv, p),
                                        Component.literal("Misiones")
                                ));
                                return 1;
                            } catch (Exception e) {
                                Extremo.LOGGER.error("Error opening quest GUI", e);
                                source.sendFailure(Component.literal("\u00a7cError al abrir misiones: " + e.getMessage()));
                                return 0;
                            }
                        }
                        source.sendFailure(Component.literal("Solo los jugadores pueden usar este comando"));
                        return 0;
                    })
            );
        });
    }
}
