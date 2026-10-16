package com.example.tamemore.client;

import com.example.tamemore.ModAttachments;
import com.example.tamemore.PetHelper;
import com.example.tamemore.SitSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

/** Client-side handler: stores the sitting flag locally on the entity. */
public final class ClientSitSync {
    private ClientSitSync() {}

    public static void handle(SitSyncPayload payload) {
        if (Minecraft.getInstance().level == null) return;
        Entity entity = Minecraft.getInstance().level.getEntity(payload.entityId());
        if (entity instanceof Mob mob && PetHelper.isSupported(mob)) {
            mob.setData(ModAttachments.TAME_DATA, PetHelper.data(mob).withSitting(payload.sitting()));
        }
    }
}
