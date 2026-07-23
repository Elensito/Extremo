package com.bestiarymod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record AllHeartsSyncPayload(Map<UUID, Integer> hearts) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AllHeartsSyncPayload> TYPE = new CustomPacketPayload.Type<>(
        Identifier.fromNamespaceAndPath("extremo", "all_hearts_sync")
    );

    public static final StreamCodec<FriendlyByteBuf, AllHeartsSyncPayload> CODEC = StreamCodec.ofMember(
        (payload, buf) -> {
            buf.writeInt(payload.hearts().size());
            for (Map.Entry<UUID, Integer> entry : payload.hearts().entrySet()) {
                buf.writeUUID(entry.getKey());
                buf.writeInt(entry.getValue());
            }
        },
        buf -> {
            int size = buf.readInt();
            Map<UUID, Integer> hearts = new HashMap<>(size);
            for (int i = 0; i < size; i++) {
                hearts.put(buf.readUUID(), buf.readInt());
            }
            return new AllHeartsSyncPayload(hearts);
        }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
