package com.smoothdoors.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

public class SmoothDoorModel extends BakedModelWrapper<BakedModel> {
    public static final ModelProperty<Boolean> HIDDEN = new ModelProperty<>();

    private static final int VERTEX_INTS = 8;

    public SmoothDoorModel(BakedModel originalModel) {
        super(originalModel);
    }

    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        Map<ModelResourceLocation, BakedModel> replacements = new HashMap<>();

        for (Map.Entry<ModelResourceLocation, BakedModel> entry : models.entrySet()) {
            ModelResourceLocation key = entry.getKey();
            if ("inventory".equals(key.variant())) {
                continue;
            }
            if (entry.getValue() instanceof SmoothDoorModel) {
                continue;
            }
            Block block = BuiltInRegistries.BLOCK.get(key.id());
            if (DoorAnimations.isSupported(block)) {
                replacements.put(key, new SmoothDoorModel(entry.getValue()));
            }
        }

        models.putAll(replacements);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        ModelData data = super.getModelData(level, pos, state, modelData);
        if (DoorAnimations.isHidden(pos)) {
            return data.derive().with(HIDDEN, Boolean.TRUE).build();
        }
        return data;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        if (Boolean.TRUE.equals(data.get(HIDDEN))) {
            return List.of();
        }

        List<BakedQuad> quads = super.getQuads(state, side, rand, data, renderType);

        // While a fence gate is animating, each leaf is drawn separately: keep only the quads of one half.
        int part = DoorAnimations.currentPart();
        if (part == DoorAnimations.NO_PART || state == null || quads.isEmpty()
                || !state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return quads;
        }

        Direction lateral = state.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise();
        boolean wantLeft = part == DoorAnimations.PART_LEFT;
        List<BakedQuad> filtered = new ArrayList<>();
        for (BakedQuad quad : quads) {
            if (isLeft(quad, lateral) == wantLeft) {
                filtered.add(quad);
            }
        }
        return filtered;
    }

    private static boolean isLeft(BakedQuad quad, Direction lateral) {
        int[] vertices = quad.getVertices();
        double sum = 0.0D;
        int count = 0;
        for (int i = 0; i + 2 < vertices.length; i += VERTEX_INTS) {
            double x = Float.intBitsToFloat(vertices[i]) - 0.5D;
            double z = Float.intBitsToFloat(vertices[i + 2]) - 0.5D;
            sum += x * lateral.getStepX() + z * lateral.getStepZ();
            count++;
        }
        return count == 0 || sum / count <= 0.0D;
    }
}
