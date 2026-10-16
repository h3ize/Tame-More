package com.smoothdoors;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class SmoothDoorsCommonConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue DIRECTIONAL;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        DIRECTIONAL = builder
                .comment("Doors opened from the side they would swing toward are moved to that edge of the block and swing away from the player instead. Needs to be installed on the server to take effect.")
                .translation("smoothdoors.configuration.directionalDoors")
                .define("directionalDoors", true);

        SPEC = builder.build();
    }

    private SmoothDoorsCommonConfig() {
    }
}
