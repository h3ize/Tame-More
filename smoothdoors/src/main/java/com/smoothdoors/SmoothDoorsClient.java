package com.smoothdoors;

import com.smoothdoors.client.DoorAnimations;
import com.smoothdoors.client.SmoothDoorModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = SmoothDoors.MOD_ID, dist = Dist.CLIENT)
public class SmoothDoorsClient {
    public SmoothDoorsClient(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SmoothDoorsConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modEventBus.addListener(SmoothDoorModel::onModifyBakingResult);

        NeoForge.EVENT_BUS.addListener(DoorAnimations::onRenderLevel);
        NeoForge.EVENT_BUS.addListener(DoorAnimations::onLoggingOut);
    }
}
