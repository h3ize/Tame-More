package com.smoothdoors;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class SmoothDoorsConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.IntValue DURATION_TICKS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        ENABLED = builder
                .comment("Turns the door, trapdoor and fence gate animations on or off.")
                .translation("smoothdoors.configuration.enabled")
                .define("enabled", true);

        DURATION_TICKS = builder
                .comment("How long a full swing takes, in ticks. 20 ticks is one second.")
                .translation("smoothdoors.configuration.durationTicks")
                .defineInRange("durationTicks", 6, 1, 40);

        SPEC = builder.build();
    }

    private SmoothDoorsConfig() {
    }
}
