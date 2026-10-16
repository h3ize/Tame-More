package com.example.tamemore;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(TameMore.MODID)
public class TameMore {
    public static final String MODID = "tamemore";

    public TameMore(IEventBus modBus) {
        ModAttachments.ATTACHMENTS.register(modBus);
    }
}
