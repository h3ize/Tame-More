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
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Quaternionf;

public final class DoorAnimations {
    public static final int NO_PART = -1;
    public static final int PART_LEFT = 0;
    public static final int PART_RIGHT = 1;

    private static final ConcurrentHashMap<Long, Animation> ANIMATIONS = new ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> DRAWING = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final ThreadLocal<Integer> PART = ThreadLocal.withInitial(() -> NO_PART);

    private static final long HOLD_MILLIS = 300L;
    private static final long MIN_DURATION_MILLIS = 40L;
    private static final double FULL_SWING = 90.0D;
    private static final double HINGE_INSET = 1.5D / 16.0D;
    private static final double GATE_POST_INSET = 1.0D / 16.0D;
    private static final int VERTEX_INTS = 8;
    private static final Direction[] QUAD_SIDES = {
            null, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };

    private DoorAnimations() {
    }

    public static boolean isSupported(Block block) {
        return block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof FenceGateBlock;
    }

    public static boolean isHidden(BlockPos pos) {
        if (DRAWING.get()) {
            return false;
        }
        Animation animation = ANIMATIONS.get(pos.asLong());
        return animation != null && !animation.finished;
    }

    public static int currentPart() {
        return PART.get();
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ANIMATIONS.clear();
    }

    public static void onBlockChanged(ClientLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
        if (!SmoothDoorsConfig.ENABLED.get()) {
            return;
        }
        Block block = newState.getBlock();
        if (!isSupported(block) || oldState.getBlock() != block) {
            return;
        }

        boolean wasOpen = oldState.getValue(BlockStateProperties.OPEN);
        boolean isOpen = newState.getValue(BlockStateProperties.OPEN);
        if (wasOpen == isOpen) {
            return;
        }

        BlockState normalized = oldState
                .setValue(BlockStateProperties.OPEN, isOpen)
                .setValue(BlockStateProperties.POWERED, newState.getValue(BlockStateProperties.POWERED));
        if (!normalized.equals(newState)) {
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
            fromFrac = wasOpen ? 1.0D : 0.0D;
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

            double frac = animation.fracAt(now);
            Block block = state.getBlock();

            if (animation.finished) {
                draw(minecraft, level, state, pos, poseStack, buffers, random, camera, 0.0D, 0.0D, 0.0D, new Quaternionf(), NO_PART);
            } else if (block instanceof DoorBlock) {
                drawDoor(minecraft, level, state, pos, poseStack, buffers, random, camera, (frac - animation.toFrac) * doorSwing(state));
            } else if (block instanceof TrapDoorBlock) {
                drawTrapDoor(minecraft, level, state, pos, poseStack, buffers, random, camera, (frac - animation.toFrac));
            } else if (block instanceof FenceGateBlock) {
                drawGate(minecraft, level, state, pos, poseStack, buffers, random, camera, frac);
            }
        }
    }

    private static void drawDoor(Minecraft minecraft, ClientLevel level, BlockState state, BlockPos pos, PoseStack poseStack,
                                 MultiBufferSource.BufferSource buffers, RandomSource random, Vec3 camera, double angle) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction hinge = doorHinge(state);
        Direction slab = facing.getOpposite();
        double reach = 0.5D - HINGE_INSET;
        double pivotX = 0.5D + (slab.getStepX() + hinge.getStepX()) * reach;
        double pivotZ = 0.5D + (slab.getStepZ() + hinge.getStepZ()) * reach;

        draw(minecraft, level, state, pos, poseStack, buffers, random, camera, pivotX, 0.0D, pivotZ,
                Axis.YP.rotationDegrees((float) angle), NO_PART);
    }

    /**
     * A trapdoor hinges on the edge opposite its facing direction. Its closed slab extends from that edge
     * towards the facing direction, and swings up (bottom half) or down (top half) into the open slab.
     */
    private static void drawTrapDoor(Minecraft minecraft, ClientLevel level, BlockState state, BlockPos pos, PoseStack poseStack,
                                     MultiBufferSource.BufferSource buffers, RandomSource random, Vec3 camera, double progress) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction back = facing.getOpposite();
        boolean top = state.getValue(BlockStateProperties.HALF) == Half.TOP;

        double reach = 0.5D - HINGE_INSET;
        double pivotX = 0.5D + back.getStepX() * reach;
        double pivotY = top ? 1.0D - HINGE_INSET : HINGE_INSET;
        double pivotZ = 0.5D + back.getStepZ() * reach;

        double swing = top ? -FULL_SWING : FULL_SWING;
        double radians = Math.toRadians(progress * swing);
        // axis = facing x up, so a positive angle lifts the front edge upwards
        Quaternionf rotation = new Quaternionf().rotationAxis((float) radians, (float) -facing.getStepZ(), 0.0F, (float) facing.getStepX());

        draw(minecraft, level, state, pos, poseStack, buffers, random, camera, pivotX, pivotY, pivotZ, rotation, NO_PART);
    }

    /**
     * A fence gate has two leaves, each hinged on its own post. The open model is always the one drawn while
     * animating (its two leaves are cleanly separated), and each leaf is rotated back towards the closed pose.
     */
    private static void drawGate(Minecraft minecraft, ClientLevel level, BlockState state, BlockPos pos, PoseStack poseStack,
                                 MultiBufferSource.BufferSource buffers, RandomSource random, Vec3 camera, double frac) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction lateral = facing.getClockWise();
        BlockState openState = state.setValue(BlockStateProperties.OPEN, Boolean.TRUE);
        BakedModel openModel = minecraft.getBlockRenderer().getBlockModel(openState);
        double reach = 0.5D - GATE_POST_INSET;

        for (int part = PART_LEFT; part <= PART_RIGHT; part++) {
            boolean left = part == PART_LEFT;
            int sign = left ? -1 : 1;

            double pivotX = 0.5D + lateral.getStepX() * reach * sign;
            double pivotZ = 0.5D + lateral.getStepZ() * reach * sign;

            // direction the leaf points in when closed (towards the middle) and when open (read from the model)
            int closedX = -sign * lateral.getStepX();
            int closedZ = -sign * lateral.getStepZ();
            int side = leafSide(openModel, openState, facing, left);
            int openX = side * facing.getStepX();
            int openZ = side * facing.getStepZ();

            double swing = openX == closedZ && openZ == -closedX ? FULL_SWING : -FULL_SWING;
            double angle = (frac - 1.0D) * swing;

            draw(minecraft, level, openState, pos, poseStack, buffers, random, camera, pivotX, 0.0D, pivotZ,
                    Axis.YP.rotationDegrees((float) angle), part);
        }
    }

    private static int leafSide(BakedModel model, BlockState state, Direction facing, boolean left) {
        Direction lateral = facing.getClockWise();
        RandomSource random = RandomSource.create();
        double best = 0.0D;

        for (Direction side : QUAD_SIDES) {
            random.setSeed(42L);
            for (BakedQuad quad : model.getQuads(state, side, random, ModelData.EMPTY, null)) {
                int[] vertices = quad.getVertices();
                for (int i = 0; i + 2 < vertices.length; i += VERTEX_INTS) {
                    double x = Float.intBitsToFloat(vertices[i]) - 0.5D;
                    double z = Float.intBitsToFloat(vertices[i + 2]) - 0.5D;
                    double across = x * lateral.getStepX() + z * lateral.getStepZ();
                    if ((across < 0.0D) != left) {
                        continue;
                    }
                    double depth = x * facing.getStepX() + z * facing.getStepZ();
                    if (Math.abs(depth) > Math.abs(best)) {
                        best = depth;
                    }
                }
            }
        }
        return best < 0.0D ? -1 : 1;
    }

    private static void draw(Minecraft minecraft, ClientLevel level, BlockState state, BlockPos pos, PoseStack poseStack,
                             MultiBufferSource.BufferSource buffers, RandomSource random, Vec3 camera,
                             double pivotX, double pivotY, double pivotZ, Quaternionf rotation, int part) {
        RenderType renderType = ItemBlockRenderTypes.getChunkRenderType(state);
        poseStack.pushPose();
        poseStack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
        poseStack.translate(pivotX, pivotY, pivotZ);
        poseStack.mulPose(rotation);
        poseStack.translate(-pivotX, -pivotY, -pivotZ);

        DRAWING.set(Boolean.TRUE);
        PART.set(part);
        try {
            minecraft.getBlockRenderer().renderBatched(state, pos, level, poseStack, buffers.getBuffer(renderType), false, random);
        } finally {
            DRAWING.set(Boolean.FALSE);
            PART.set(NO_PART);
        }

        poseStack.popPose();
        buffers.endBatch(renderType);
    }

    private static void refresh(ClientLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Minecraft.getInstance().levelRenderer.blockChanged(level, pos, state, state, 8);
    }

    private static Direction doorHinge(BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return state.getValue(BlockStateProperties.DOOR_HINGE) == DoorHingeSide.LEFT ? facing.getCounterClockWise() : facing.getClockWise();
    }

    private static double doorSwing(BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction hinge = doorHinge(state);
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
            return state.getBlock() == block && state.getValue(BlockStateProperties.OPEN) == opening;
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
