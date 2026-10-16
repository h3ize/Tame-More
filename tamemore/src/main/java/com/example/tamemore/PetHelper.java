package com.example.tamemore;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;

public final class PetHelper {
    private PetHelper() {}

    public static boolean isSupported(Entity e) {
        return e instanceof Ocelot || e instanceof Fox;
    }

    /** Ocelots are tamed with raw fish (like cats); foxes with raw chicken / rabbit. */
    public static boolean isTamingFood(Mob mob, ItemStack stack) {
        if (mob instanceof Ocelot) return stack.is(Items.COD) || stack.is(Items.SALMON);
        if (mob instanceof Fox) return stack.is(Items.CHICKEN) || stack.is(Items.RABBIT);
        return false;
    }

    public static TameData data(Mob mob) {
        return mob.getData(ModAttachments.TAME_DATA);
    }

    public static boolean isSitting(Mob mob) {
        return data(mob).sitting();
    }

    public static void setSitting(Mob mob, boolean sitting) {
        mob.setData(ModAttachments.TAME_DATA, data(mob).withSitting(sitting));
    }

    /** Returns the owner if they are online and in the same dimension, otherwise null. */
    public static Player getOwner(Mob mob) {
        Optional<java.util.UUID> id = data(mob).owner();
        return id.map(uuid -> mob.level().getPlayerByUUID(uuid)).orElse(null);
    }

    /** Fox has a real sitting pose; ocelots have none, so they just stand still. */
    public static void applyPoseVisual(Mob mob, boolean sitting) {
        if (mob instanceof Fox fox && fox.isSitting() != sitting) {
            fox.setSitting(sitting);
        }
    }

    public static void tame(Mob mob, Player player) {
        mob.setData(ModAttachments.TAME_DATA, new TameData(Optional.of(player.getUUID()), true));
        mob.getNavigation().stop();
        mob.setTarget(null);

        if (mob instanceof Ocelot ocelot) {
            ocelot.setTrusting(true); // stops it fleeing from players
        }
        if (mob instanceof Fox fox) {
            fox.addTrustedUUID(player.getUUID()); // stops it fleeing from the owner
        }

        particles(mob, true);

        if (player instanceof ServerPlayer sp && mob instanceof Animal animal) {
            // Fires the vanilla tame_animal trigger -> our advancements listen for it
            CriteriaTriggers.TAME_ANIMAL.trigger(sp, animal);
        }
        player.displayClientMessage(Component.translatable("message.tamemore.tamed", mob.getName()), true);
    }

    public static void particles(Mob mob, boolean success) {
        if (mob.level() instanceof ServerLevel level) {
            level.sendParticles(success ? ParticleTypes.HEART : ParticleTypes.SMOKE,
                    mob.getX(), mob.getY() + mob.getBbHeight() * 0.8, mob.getZ(),
                    7, 0.4, 0.3, 0.4, 0.02);
        }
    }
}
