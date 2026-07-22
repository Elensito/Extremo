package com.bestiarymod.command;

import com.bestiarymod.Extremo;
import com.bestiarymod.gui.BestiaryMainGui;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class BestiaryCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("bestiary")
                    .executes(context -> {
                        CommandSourceStack source = context.getSource();
                        if (source.getEntity() instanceof ServerPlayer player) {
                            try {
                                player.openMenu(new SimpleMenuProvider(
                                        (syncId, inv, p) -> new BestiaryMainGui(0).createMenu(syncId, inv, p),
                                        Component.literal("Bestiario")
                                ));
                                return 1;
                            } catch (Exception e) {
                                Extremo.LOGGER.error("Error opening bestiary GUI", e);
                                source.sendFailure(Component.literal("\u00a7cError al abrir bestiario: " + e.getMessage()));
                                return 0;
                            }
                        }
                        source.sendFailure(Component.literal("Solo los jugadores pueden usar este comando"));
                        return 0;
                    })
            );
            dispatcher.register(Commands.literal("b")
                    .executes(context -> {
                        CommandSourceStack source = context.getSource();
                        if (source.getEntity() instanceof ServerPlayer player) {
                            try {
                                player.openMenu(new SimpleMenuProvider(
                                        (syncId, inv, p) -> new BestiaryMainGui(0).createMenu(syncId, inv, p),
                                        Component.literal("Bestiario")
                                ));
                                return 1;
                            } catch (Exception e) {
                                Extremo.LOGGER.error("Error opening bestiary GUI", e);
                                source.sendFailure(Component.literal("\u00a7cError al abrir bestiario: " + e.getMessage()));
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
