package com.example.tamemore.client;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Ocelot;

/** Same look as the vanilla ocelot renderer, but uses the model that can sit. */
public class SittingOcelotRenderer extends MobRenderer<Ocelot, SittingOcelotModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/cat/ocelot.png");

    public SittingOcelotRenderer(EntityRendererProvider.Context context) {
        super(context, new SittingOcelotModel(context.bakeLayer(ModelLayers.OCELOT)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(Ocelot ocelot) {
        return TEXTURE;
    }
}
