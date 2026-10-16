package com.example.tamemore.goal;

import com.example.tamemore.PetHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

import java.util.EnumSet;

/** Mirrors vanilla FollowOwnerGoal, but works on any Mob using our attachment data. */
public class FollowOwnerPetGoal extends Goal {
    private final Mob mob;
    private final double speed;
    private final float startDistance;
    private final float stopDistance;
    private Player owner;
    private int timeToRecalcPath;
    private float oldWaterCost;

    public FollowOwnerPetGoal(Mob mob, double speed, float startDistance, float stopDistance) {
        this.mob = mob;
        this.speed = speed;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        Player player = PetHelper.getOwner(mob);
        if (player == null || player.isSpectator()) return false;
        if (PetHelper.isSitting(mob) || unableToMove()) return false;
        if (mob.distanceToSqr(player) < (double) (startDistance * startDistance)) return false;
        this.owner = player;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (owner == null || mob.getNavigation().isDone()) return false;
        if (PetHelper.isSitting(mob) || unableToMove()) return false;
        return mob.distanceToSqr(owner) > (double) (stopDistance * stopDistance);
    }

    private boolean unableToMove() {
        return mob.isPassenger() || mob.isLeashed();
    }

    @Override
    public void start() {
        timeToRecalcPath = 0;
        oldWaterCost = mob.getPathfindingMalus(PathType.WATER);
        mob.setPathfindingMalus(PathType.WATER, 0.0F);
        if (mob instanceof Fox fox && fox.isSleeping()) {
            fox.setSleeping(false); // wake up a dozing fox when the owner walks off
        }
    }

    @Override
    public void stop() {
        owner = null;
        mob.getNavigation().stop();
        mob.setPathfindingMalus(PathType.WATER, oldWaterCost);
    }

    @Override
    public void tick() {
        mob.getLookControl().setLookAt(owner, 10.0F, (float) mob.getMaxHeadXRot());
        if (--timeToRecalcPath <= 0) {
            timeToRecalcPath = this.adjustedTickDelay(10);
            if (mob.distanceToSqr(owner) >= 144.0) {
                teleportToOwner();
            } else {
                mob.getNavigation().moveTo(owner, speed);
            }
        }
    }

    private void teleportToOwner() {
        BlockPos base = owner.blockPosition();
        for (int i = 0; i < 10; i++) {
            int x = base.getX() + mob.getRandom().nextIntBetweenInclusive(-3, 3);
            int y = base.getY() + mob.getRandom().nextIntBetweenInclusive(-1, 1);
            int z = base.getZ() + mob.getRandom().nextIntBetweenInclusive(-3, 3);
            if (tryTeleport(x, y, z)) return;
        }
    }

    private boolean tryTeleport(int x, int y, int z) {
        if (Math.abs(x - owner.getX()) < 2.0 && Math.abs(z - owner.getZ()) < 2.0) return false;
        BlockPos pos = new BlockPos(x, y, z);
        if (!canTeleportTo(pos)) return false;
        mob.moveTo(x + 0.5, y, z + 0.5, mob.getYRot(), mob.getXRot());
        mob.getNavigation().stop();
        return true;
    }

    private boolean canTeleportTo(BlockPos pos) {
        if (WalkNodeEvaluator.getPathTypeStatic(mob, pos) != PathType.WALKABLE) return false;
        if (mob.level().getBlockState(pos.below()).getBlock() instanceof LeavesBlock) return false;
        BlockPos diff = pos.subtract(mob.blockPosition());
        return mob.level().noCollision(mob, mob.getBoundingBox().move(diff));
    }
}
