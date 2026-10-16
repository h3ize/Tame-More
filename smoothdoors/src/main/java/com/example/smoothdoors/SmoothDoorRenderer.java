package com.example.smoothdoors;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;

public class SmoothDoorRenderer implements BlockEntityRenderer<SmoothDoorBlockEntity> {
    private static final double HINGE_INSET = 1.5 / 16.0; // centre of the 3px-thick slab

    private final BlockRenderDispatcher dispatcher;

    public SmoothDoorRenderer(BlockEntityRendererProvider.Context ctx) {
        this.dispatcher = ctx.getBlockRenderDispatcher();
    }

    @Override
    public void render(SmoothDoorBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof DoorBlock) || be.getLevel() == null) return;

        float t = be.updateProgress(state.getValue(DoorBlock.OPEN));
        float eased = easeInOutCubic(t);

        Direction facing = state.getValue(DoorBlock.FACING);
        boolean hingeRight = state.getValue(DoorBlock.HINGE) == DoorHingeSide.RIGHT;

        // The closed slab sits on the side opposite `facing`; when open it swings to the
        // hinge-dependent side wall (same rule vanilla's collision shape uses).
        Direction closedSide = facing.getOpposite();
        Direction openSide = hingeRight ? facing.getCounterClockWise() : facing.getClockWise();

        double off = 0.5 - HINGE_INSET;
        double px = 0.5 + closedSide.getStepX() * off + openSide.getStepX() * off;
        double pz = 0.5 + closedSide.getStepZ() * off + openSide.getStepZ() * off;
        float angle = (hingeRight ? 90f : -90f) * eased;

        // Always render the CLOSED model and rotate it about the hinge post.
        BlockState closed = state.setValue(DoorBlock.OPEN, false);
        BakedModel model = dispatcher.getBlockModel(closed);
        BlockPos pos = be.getBlockPos();
        RandomSource rand = RandomSource.create(closed.getSeed(pos));
        ModelData data = model.getModelData(be.getLevel(), pos, closed, ModelData.EMPTY);

        pose.pushPose();
        pose.translate(px, 0, pz);
        pose.mulPose(Axis.YP.rotationDegrees(angle));
        pose.translate(-px, 0, -pz);

        for (RenderType type : model.getRenderTypes(closed, rand, data)) {
            VertexConsumer vc = buffers.getBuffer(RenderTypeHelper.getEntityRenderType(type, false));
            dispatcher.getModelRenderer().renderModel(pose.last(), vc, closed, model,
                    1f, 1f, 1f, light, overlay, data, type);
        }
        pose.popPose();
    }

    /** Swap this out for a different feel (e.g. ease-out-back for a little overshoot). */
    private static float easeInOutCubic(float x) {
        return x < 0.5f ? 4f * x * x * x : 1f - (float) Math.pow(-2f * x + 2f, 3) / 2f;
    }
}
