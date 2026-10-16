package com.example.tamemore;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server -> client: tells clients whether a pet is sitting so the renderer can pose it. */
public record SitSyncPayload(int entityId, boolean sitting) implements CustomPacketPayload {
    public static final Type<SitSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TameMore.MODID, "sit_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SitSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SitSyncPayload::entityId,
            ByteBufCodecs.BOOL, SitSyncPayload::sitting,
            SitSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
