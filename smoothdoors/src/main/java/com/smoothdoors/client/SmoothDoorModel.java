package com.smoothdoors.client;

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
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

public class SmoothDoorModel extends BakedModelWrapper<BakedModel> {
    public static final ModelProperty<Boolean> HIDDEN = new ModelProperty<>();

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
            if (block instanceof DoorBlock) {
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
        return super.getQuads(state, side, rand, data, renderType);
    }
}
