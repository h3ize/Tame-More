package com.example.tamemore.client;

import com.example.tamemore.TameMore;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = TameMore.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TameMoreClient {

    /** Swap the vanilla ocelot renderer for one that can show the sitting pose. */
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityType.OCELOT, SittingOcelotRenderer::new);
    }
}
