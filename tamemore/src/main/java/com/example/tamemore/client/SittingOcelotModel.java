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
        leftFrontLeg.xRot = -0.15707964F;
        leftFrontLeg.y += -3.0F;
        leftFrontLeg.z += 1.0F;
        rightFrontLeg.xRot = -0.15707964F;
        rightFrontLeg.y += -3.0F;
        rightFrontLeg.z += 1.0F;

        // Back legs folded forward under the body
        leftHindLeg.xRot = -1.5707964F;
        leftHindLeg.y += 3.0F;
        leftHindLeg.z += -4.0F;
        rightHindLeg.xRot = -1.5707964F;
        rightHindLeg.y += 3.0F;
        rightHindLeg.z += -4.0F;
    }
}
