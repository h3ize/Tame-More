package com.example.smoothdoors;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Holds the purely client-side animation progress (0 = closed, 1 = open). */
public class SmoothDoorBlockEntity extends BlockEntity {
    /** Seconds a full open/close takes. Tweak to taste. */
    public static final float DURATION_SECONDS = 0.35f;

    private float progress;
    private long lastNanos = 0L;

    public SmoothDoorBlockEntity(BlockPos pos, BlockState state) {
        super(SmoothDoors.DOOR_BE.get(), pos, state);
        // Start in the correct pose so doors already open when loaded don't swing.
        this.progress = state.getValue(DoorBlock.OPEN) ? 1f : 0f;
    }

    /** Advances the animation toward the target and returns the linear progress. */
    public float updateProgress(boolean open) {
        long now = System.nanoTime();
        float dt = lastNanos == 0L ? 0f : (now - lastNanos) / 1_000_000_000f;
        lastNanos = now;

        float target = open ? 1f : 0f;
        float step = dt / DURATION_SECONDS;
        if (progress < target) {
            progress = Math.min(target, progress + step);
        } else if (progress > target) {
            progress = Math.max(target, progress - step);
        }
        return progress;
    }
}
