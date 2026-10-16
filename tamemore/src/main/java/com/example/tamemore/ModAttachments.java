package com.example.tamemore;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TameMore.MODID);

    public static final Supplier<AttachmentType<TameData>> TAME_DATA = ATTACHMENTS.register("tame_data",
            () -> AttachmentType.builder(() -> TameData.EMPTY)
                    // If your NeoForge version wants a Codec instead of a MapCodec here,
                    // use TameData.MAP_CODEC.codec()
                    .serialize(TameData.MAP_CODEC)
                    .build());
}
