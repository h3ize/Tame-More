package com.smoothdoors.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.smoothdoors.SmoothDoorsConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public final class DoorAnimations {
    private static final ConcurrentHashMap<Long, Animation> ANIMATIONS = new ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> DRAWING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private static final long HOLD_MILLIS = 300L;
    private static final long MIN_DURATION_MILLIS = 40L;
    private static final double FULL_SWING = 90.0D;
    private static final double HINGE_INSET = 1.5D / 16.0D;

    private DoorAnimations() {
    }

    public static boolean isHidden(BlockPos pos) {
        if (DRAWING.get()) {
            return false;
        }
        Animation animation = ANIMATIONS.get(pos.asLong());
        return animation != null && !animation.finished;
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ANIMATIONS.clear();
    }

    public static void onBlockChanged(ClientLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
        if (!SmoothDoorsConfig.ENABLED.get()) {
            return;
        }
        Block block = newState.getBlock();
        if (!(block instanceof DoorBlock) || oldState.getBlock() != block) {
            return;
        }

        boolean wasOpen = oldState.getValue(DoorBlock.OPEN);
        boolean isOpen = newState.getValue(DoorBlock.OPEN);
        if (wasOpen == isOpen) {
            return;
        }

        BlockState normalized = oldState
                .setValue(DoorBlock.OPEN, isOpen)
                .setValue(DoorBlock.POWERED, newState.getValue(DoorBlock.POWERED));
        boolean sameShape = normalized.equals(newState);
        if (!sameShape && !(isOpen && oldState.getValue(DoorBlock.HALF) == newState.getValue(DoorBlock.HALF))) {
            return;
        }

        long key = pos.asLong();
        long now = Util.getMillis();
        Animation previous = ANIMATIONS.get(key);

        double toFrac = isOpen ? 1.0D : 0.0D;
        double fromFrac;
        if (previous != null && previous.level == level) {
            fromFrac = previous.fracAt(now);
        } else {
            fromFrac = wasOpen && sameShape ? 1.0D : 0.0D;
        }

        double distance = Math.abs(toFrac - fromFrac);
        if (distance < 0.02D) {
            if (previous != null) {
                ANIMATIONS.remove(key, previous);
                refresh(level, pos);
            }
            return;
        }

        long fullDuration = SmoothDoorsConfig.DURATION_TICKS.get() * 50L;
        long duration = Math.max(MIN_DURATION_MILLIS, (long) (fullDuration * distance));

        Animation animation = new Animation(level, pos.immutable(), block, isOpen, fromFrac, toFrac, now, duration);
        ANIMATIONS.put(key, animation);
        refresh(level, pos);
    }

    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES || ANIMATIONS.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            ANIMATIONS.clear();
            return;
        }

        long now = Util.getMillis();
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        RandomSource random = RandomSource.create();

        for (Map.Entry<Long, Animation> entry : ANIMATIONS.entrySet()) {
            Animation animation = entry.getValue();
            BlockPos pos = animation.pos;

            if (animation.level != level) {
                ANIMATIONS.remove(entry.getKey(), animation);
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (!animation.matches(state)) {
                ANIMATIONS.remove(entry.getKey(), animation);
                refresh(level, pos);
                continue;
            }

            if (animation.finished) {
                if (now - animation.finishedMillis > HOLD_MILLIS) {
                    ANIMATIONS.remove(entry.getKey(), animation);
                    continue;
                }
            } else if (now - animation.startMillis >= animation.durationMillis) {
                animation.finishedMillis = now;
                animation.finished = true;
                refresh(level, pos);
            }

            double angle = (animation.fracAt(now) - animation.toFrac) * swingAngle(state);

            Direction facing = state.getValue(DoorBlock.FACING);
            Direction hinge = hingeDirection(state);
            Direction slab = facing.getOpposite();
            double reach = 0.5D - HINGE_INSET;
            double pivotX = 0.5D + (slab.getStepX() + hinge.getStepX()) * reach;
            double pivotZ = 0.5D + (slab.getStepZ() + hinge.getStepZ()) * reach;

            RenderType renderType = ItemBlockRenderTypes.getChunkRenderType(state);
            poseStack.pushPose();
            poseStack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
            poseStack.translate(pivotX, 0.0D, pivotZ);
            poseStack.mulPose(Axis.YP.rotationDegrees((float) angle));
            poseStack.translate(-pivotX, 0.0D, -pivotZ);

            DRAWING.set(Boolean.TRUE);
            try {
                minecraft.getBlockRenderer().renderBatched(state, pos, level, poseStack, buffers.getBuffer(renderType), false, random);
            } finally {
                DRAWING.set(Boolean.FALSE);
            }

            poseStack.popPose();
            buffers.endBatch(renderType);
        }
    }

    private static void refresh(ClientLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Minecraft.getInstance().levelRenderer.blockChanged(level, pos, state, state, 8);
    }

    private static Direction hingeDirection(BlockState state) {
        Direction facing = state.getValue(DoorBlock.FACING);
        return state.getValue(DoorBlock.HINGE) == DoorHingeSide.LEFT ? facing.getCounterClockWise() : facing.getClockWise();
    }

    private static double swingAngle(BlockState state) {
        Direction facing = state.getValue(DoorBlock.FACING);
        Direction hinge = hingeDirection(state);
        boolean positive = -hinge.getStepZ() == facing.getStepX() && hinge.getStepX() == facing.getStepZ();
        return positive ? FULL_SWING : -FULL_SWING;
    }

    private static double ease(double t) {
        if (t <= 0.0D) {
            return 0.0D;
        }
        if (t >= 1.0D) {
            return 1.0D;
        }
        return t < 0.5D ? 4.0D * t * t * t : 1.0D - Math.pow(-2.0D * t + 2.0D, 3.0D) / 2.0D;
    }

    private static final class Animation {
        final ClientLevel level;
        final BlockPos pos;
        final Block block;
        final boolean opening;
        final double fromFrac;
        final double toFrac;
        final long startMillis;
        final long durationMillis;
        volatile boolean finished;
        long finishedMillis;

        Animation(ClientLevel level, BlockPos pos, Block block, boolean opening, double fromFrac, double toFrac, long startMillis, long durationMillis) {
            this.level = level;
            this.pos = pos;
            this.block = block;
            this.opening = opening;
            this.fromFrac = fromFrac;
            this.toFrac = toFrac;
            this.startMillis = startMillis;
            this.durationMillis = durationMillis;
        }

        boolean matches(BlockState state) {
            return state.getBlock() == block && state.getValue(DoorBlock.OPEN) == opening;
        }

        double fracAt(long now) {
            if (finished) {
                return toFrac;
            }
            double t = (now - startMillis) / (double) durationMillis;
            return fromFrac + (toFrac - fromFrac) * ease(t);
        }
    }
}
