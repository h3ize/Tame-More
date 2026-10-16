package com.example.tamemore.goal;

import com.example.tamemore.PetHelper;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class PetSitGoal extends Goal {
    private final Mob mob;

    public PetSitGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return PetHelper.isSitting(mob) && !mob.isInWaterOrBubble() && mob.onGround();
    }

    @Override
    public void start() {
        mob.getNavigation().stop();
        mob.setTarget(null);
        PetHelper.applyPoseVisual(mob, true);
    }

    @Override
    public void stop() {
        PetHelper.applyPoseVisual(mob, false);
    }
}
