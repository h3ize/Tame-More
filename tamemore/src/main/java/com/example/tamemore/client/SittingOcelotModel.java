package com.example.tamemore.client;

import com.example.tamemore.PetHelper;
import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.Ocelot;

/**
 * Vanilla ocelot model plus a cat-style sitting pose. The sitting flag comes from
 * Tame More's synced attachment, so it works on the client.
 *
 * Pose values match the vanilla cat sit.
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
        // Same values the vanilla cat uses for its sitting pose.
        body.xRot = 0.7853982F;
        body.y += -4.0F;
        body.z += 5.0F;

        head.y += -3.3F;
        head.z += 1.0F;

        tail1.y += 8.0F;
        tail1.z += -2.0F;
        tail1.xRot = 1.7278761F;
        tail2.y += 2.0F;
        tail2.z += -0.8F;
        tail2.xRot = 2.670354F;

        leftFrontLeg.xRot = -0.15707964F;
        leftFrontLeg.y = 16.1F;
        leftFrontLeg.z = -7.0F;
        rightFrontLeg.xRot = -0.15707964F;
        rightFrontLeg.y = 16.1F;
        rightFrontLeg.z = -7.0F;

        leftHindLeg.xRot = -1.5707964F;
        leftHindLeg.y = 21.0F;
        leftHindLeg.z = 1.0F;
        rightHindLeg.xRot = -1.5707964F;
        rightHindLeg.y = 21.0F;
        rightHindLeg.z = 1.0F;
    }
}
