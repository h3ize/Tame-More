package com.example.tamemore.client;

import com.example.tamemore.PetHelper;
import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.Ocelot;

/**
 * Vanilla ocelot model plus a cat-style sitting pose. The sitting flag comes from
 * Tame More's synced attachment, so it works on the client.
 *
 * The numbers below are the usual cat sit pose; if a limb looks off, nudge them here.
 */
public class SittingOcelotModel extends OcelotModel<Ocelot> {

    public SittingOcelotModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(Ocelot ocelot, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(ocelot, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        if (PetHelper.isSitting(ocelot)) {
            applySitPose();
        }
    }

    private void applySitPose() {
        // Body tilts up so the cat rests on its haunches
        body.xRot = 0.7853982F;
        body.y += -4.0F;
        body.z += -2.0F;

        // Tail hangs down behind
        tail1.xRot = 0.7853982F;
        tail1.y += -9.0F;
        tail1.z += 1.0F;
        tail2.xRot = 1.5707964F;
        tail2.y += -4.0F;
        tail2.z += -2.0F;

        // Head up, looking forward
        head.y += -2.0F;
        head.z += -1.0F;

        // Front legs straight down
        frontLegL.xRot = -0.15707964F;
        frontLegL.y += -3.0F;
        frontLegL.z += 1.0F;
        frontLegR.xRot = -0.15707964F;
        frontLegR.y += -3.0F;
        frontLegR.z += 1.0F;

        // Back legs folded forward under the body
        backLegL.xRot = -1.5707964F;
        backLegL.y += 3.0F;
        backLegL.z += -4.0F;
        backLegR.xRot = -1.5707964F;
        backLegR.y += 3.0F;
        backLegR.z += -4.0F;
    }
}
