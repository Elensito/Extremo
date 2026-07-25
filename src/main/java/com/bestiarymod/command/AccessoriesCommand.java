package com.bestiarymod.command;

import com.bestiarymod.gui.AccessoriesGui;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

public class AccessoriesCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("accesorios")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    player.openMenu(new SimpleMenuProvider(
                        (syncId, inv, p) -> new AccessoriesGui().createMenu(syncId, inv, p),
                        Component.literal("Accesorios")
                    ));
                    return 1;
                })
            );
        });
    }
}
