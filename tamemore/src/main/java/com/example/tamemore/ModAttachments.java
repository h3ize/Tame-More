package com.example.tamemore;

import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TameMore.MODID);

    public static final Supplier<AttachmentType<TameData>> TAME_DATA = ATTACHMENTS.register("tame_data",
            () -> AttachmentType.builder(() -> TameData.EMPTY)
                    .serialize(TameData.MAP_CODEC.codec())
                    // Sync to clients so the renderer knows when an ocelot is sitting
                    .sync(ByteBufCodecs.fromCodec(TameData.MAP_CODEC.codec()))
                    .build());
}
