package com.smoothdoors.common;

import com.smoothdoors.SmoothDoorsCommonConfig;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.fml.ModList;

public final class DirectionalDoors {
    private static final ThreadLocal<Context> CONTEXT = new ThreadLocal<>();
    private static final int MAX_DOORS = 64;
    private static final int MAX_DISTANCE = 16;

    private DirectionalDoors() {
    }

    public static void begin(BlockState state, Level level, BlockPos pos, Player player) {
        CONTEXT.remove();

        if (!SmoothDoorsCommonConfig.DIRECTIONAL.get()) {
            return;
        }
        if (!(state.getBlock() instanceof DoorBlock door) || !door.type().canOpenByHand()) {
            return;
        }
        if (state.getValue(DoorBlock.OPEN)) {
            return;
        }

        Direction facing = state.getValue(DoorBlock.FACING);
        double offset = (player.getX() - (pos.getX() + 0.5D)) * facing.getStepX()
                + (player.getZ() - (pos.getZ() + 0.5D)) * facing.getStepZ();
        if (offset <= 0.0D) {
            return;
        }

        BlockPos lower = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos.immutable() : pos.below();
        List<BlockPos> doors = new ArrayList<>();
        doors.add(lower);

        if (ModList.get().isLoaded("doubledoors")) {
            collectConnected(level, state.getBlock(), facing, lower, doors);
        }

        CONTEXT.set(new Context(level, state.getBlock(), facing, doors));
    }

    public static void finish() {
        Context context = CONTEXT.get();
        if (context == null) {
            return;
        }
        CONTEXT.remove();

        for (BlockPos lowerPos : context.doors) {
            BlockPos upperPos = lowerPos.above();
            BlockState lower = context.level.getBlockState(lowerPos);
            BlockState upper = context.level.getBlockState(upperPos);

            if (lower.getBlock() != context.block || upper.getBlock() != context.block) {
                continue;
            }
            if (!lower.getValue(DoorBlock.OPEN)) {
                continue;
            }
            if (lower.getValue(DoorBlock.FACING) != context.facing || upper.getValue(DoorBlock.FACING) != context.facing) {
                continue;
            }

            context.level.setBlock(lowerPos, flip(lower), 18);
            context.level.setBlock(upperPos, flip(upper), 18);
        }
    }

    private static BlockState flip(BlockState state) {
        DoorHingeSide hinge = state.getValue(DoorBlock.HINGE) == DoorHingeSide.LEFT ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
        return state
                .setValue(DoorBlock.FACING, state.getValue(DoorBlock.FACING).getOpposite())
                .setValue(DoorBlock.HINGE, hinge);
    }

    private static void collectConnected(Level level, Block block, Direction facing, BlockPos origin, List<BlockPos> doors) {
        Set<BlockPos> seen = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        seen.add(origin);
        queue.add(origin);

        while (!queue.isEmpty() && doors.size() < MAX_DOORS) {
            BlockPos current = queue.poll();
            BlockPos[] neighbours = {
                    current.relative(facing.getClockWise()),
                    current.relative(facing.getCounterClockWise()),
                    current.above(2),
                    current.below(2)
            };

            for (BlockPos neighbour : neighbours) {
                if (doors.size() >= MAX_DOORS) {
                    break;
                }
                if (neighbour.distManhattan(origin) > MAX_DISTANCE || !seen.add(neighbour)) {
                    continue;
                }

                BlockState lower = level.getBlockState(neighbour);
                if (lower.getBlock() != block
                        || lower.getValue(DoorBlock.FACING) != facing
                        || lower.getValue(DoorBlock.OPEN)
                        || lower.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) {
                    continue;
                }
                if (level.getBlockState(neighbour.above()).getBlock() != block) {
                    continue;
                }

                BlockPos immutable = neighbour.immutable();
                doors.add(immutable);
                queue.add(immutable);
            }
        }
    }

    private record Context(Level level, Block block, Direction facing, List<BlockPos> doors) {
    }
}
