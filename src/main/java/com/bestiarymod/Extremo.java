package com.bestiarymod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bestiarymod.command.BestiaryCommand;
import com.bestiarymod.command.BestiaryConfigCommand;
import com.bestiarymod.command.ExtremoCommand;
import com.bestiarymod.config.BestiaryConfigManager;
import com.bestiarymod.data.BestiaryState;
import com.bestiarymod.entity.ModEntities;
import com.bestiarymod.handler.MobKillHandler;
import com.bestiarymod.item.ModItems;
import com.bestiarymod.item.TpWandItem;
import com.bestiarymod.access.HeartDataAccessor;
import com.bestiarymod.network.HeartSyncPayload;
import com.bestiarymod.network.ItemActivationPayload;
import com.bestiarymod.spawn.CustomSpawner;
import com.bestiarymod.spawn.SpawnConfigManager;
import com.bestiarymod.spawn.SpawnerRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public class Extremo implements ModInitializer {
    public static final String MOD_ID = "extremo";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static MinecraftServer currentServer;
    public static final ResourceKey<DamageType> MOB_ATTACK_NO_SCALE = ResourceKey.create(
        net.minecraft.core.registries.Registries.DAMAGE_TYPE,
        Identifier.fromNamespaceAndPath(MOD_ID, "mob_attack_no_scale")
    );

    @Override
    public void onInitialize() {
        ModItems.register();
        ModEntities.register();
        BestiaryCommand.register();
        ExtremoCommand.register();
        MobKillHandler.register();
        TpWandItem.registerTickHandler();
        SpawnConfigManager.init();
        SpawnerRegistry.register();
        CustomSpawner.init();

        PayloadTypeRegistry.clientboundPlay().register(HeartSyncPayload.TYPE, HeartSyncPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ItemActivationPayload.TYPE, ItemActivationPayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register((ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) -> {
            ServerPlayer player = handler.getPlayer();
            HeartDataAccessor accessor = (HeartDataAccessor) player;
            ServerPlayNetworking.send(player, new HeartSyncPayload(accessor.getExtremoHearts()));
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((livingEntity, damageSource) -> {
            if (livingEntity instanceof ServerPlayer player) {
                HeartDataAccessor accessor = (HeartDataAccessor) livingEntity;
                int currentHearts = accessor.getExtremoHearts();
                if (currentHearts > 0) {
                    accessor.setExtremoHearts(currentHearts - 1);
                    ServerPlayNetworking.send(player, new HeartSyncPayload(currentHearts - 1));
                    if (currentHearts == 1) {
                        MinecraftServer server = player.level().getServer();
                        if (server != null) {
                            Component banMessage = Component.literal("\u00a7c\u00a1Has perdido todas tus vidas!\n\u00a77Tu alma ha sido consumida por el coraz\u00f3n del mundo.");
                            server.getPlayerList().getBans().add(new UserBanListEntry(
                                new NameAndId(player.getGameProfile()),
                                new Date(),
                                "Extremo",
                                null,
                                "\u00a7cHas perdido todas tus vidas. Tu alma ha sido consumida."
                            ));
                            player.connection.disconnect(banMessage);
                        }
                    }
                }
            }
        });

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            currentServer = server;
            BestiaryConfigManager.loadAll(FabricLoader.getInstance().getConfigDir());
            BestiaryState.load(server);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            BestiaryState.save(server);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ItemStack mainHand = player.getMainHandItem();
                if (mainHand.getItem() instanceof TpWandItem wand && player.getCooldowns().isOnCooldown(mainHand)) {
                    float remaining = player.getCooldowns().getCooldownPercent(mainHand, 0);
                    int seconds = (int) Math.ceil(remaining * wand.getCooldownTicks() / 20);
                    player.connection.send(new ClientboundSetActionBarTextPacket(
                        Component.literal("\u00a7d\u2728 Cetro Dimensional \u00a77recargando \u00a7f" + seconds + "s")
                    ));
                }
            }
        });

        LOGGER.info("Extremo initialized!");
    }
}
