package com.bestiarymod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HeartSyncPayload(int hearts) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<HeartSyncPayload> TYPE = new CustomPacketPayload.Type<>(
        Identifier.fromNamespaceAndPath("extremo", "heart_sync")
    );

    public static final StreamCodec<FriendlyByteBuf, HeartSyncPayload> CODEC = StreamCodec.ofMember(
        (payload, buf) -> buf.writeInt(payload.hearts()),
        buf -> new HeartSyncPayload(buf.readInt())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
