package com.example.smoothdoors;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;

/** A normal door in every gameplay respect; only the rendering is handed to a BlockEntityRenderer. */
public class SmoothDoorBlock extends DoorBlock implements EntityBlock {
    public SmoothDoorBlock(BlockSetType type, Properties properties) {
        super(type, properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // Don't bake into the chunk mesh; the renderer draws the (animated) model instead.
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SmoothDoorBlockEntity(pos, state);
    }
}
