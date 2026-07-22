package com.bestiarymod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ItemActivationPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ItemActivationPayload> TYPE = new CustomPacketPayload.Type<>(
        Identifier.fromNamespaceAndPath("extremo", "item_activation")
    );

    public static final StreamCodec<FriendlyByteBuf, ItemActivationPayload> CODEC = StreamCodec.unit(new ItemActivationPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
