package com.example.tamemore;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import com.example.tamemore.client.ClientSitSync;

@Mod(TameMore.MODID)
public class TameMore {
    public static final String MODID = "tamemore";

    public TameMore(IEventBus modBus) {
        ModAttachments.ATTACHMENTS.register(modBus);
        modBus.addListener(TameMore::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(SitSyncPayload.TYPE, SitSyncPayload.STREAM_CODEC,
                (payload, ctx) -> ClientSitSync.handle(payload));
    }
}
