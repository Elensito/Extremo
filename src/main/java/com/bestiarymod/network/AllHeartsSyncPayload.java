package com.bestiarymod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record AllHeartsSyncPayload(Map<UUID, Integer> heartsMap) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AllHeartsSyncPayload> TYPE = new CustomPacketPayload.Type<>(
        Identifier.fromNamespaceAndPath("extremo", "all_hearts_sync")
    );

    public static final StreamCodec<FriendlyByteBuf, AllHeartsSyncPayload> CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeVarInt(payload.heartsMap.size());
            for (Map.Entry<UUID, Integer> entry : payload.heartsMap.entrySet()) {
                buf.writeUUID(entry.getKey());
                buf.writeVarInt(entry.getValue());
            }
        },
        buf -> {
            int size = buf.readVarInt();
            Map<UUID, Integer> map = new HashMap<>();
            for (int i = 0; i < size; i++) {
                UUID uuid = buf.readUUID();
                int hearts = buf.readVarInt();
                map.put(uuid, hearts);
            }
            return new AllHeartsSyncPayload(map);
        }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
