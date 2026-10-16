package com.smoothdoors;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(SmoothDoors.MOD_ID)
public class SmoothDoors {
    public static final String MOD_ID = "smoothdoors";

    public SmoothDoors(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, SmoothDoorsCommonConfig.SPEC);
    }
}
