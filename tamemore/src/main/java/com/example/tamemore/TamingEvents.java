package com.example.tamemore;

import com.example.tamemore.goal.FollowOwnerPetGoal;
import com.example.tamemore.goal.PetSitGoal;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = TameMore.MODID)
public class TamingEvents {

    /** Add our AI goals to every ocelot and fox as it enters the world. */
    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Mob mob) || !PetHelper.isSupported(mob)) return;

        boolean alreadyAdded = mob.goalSelector.getAvailableGoals().stream()
                .anyMatch(w -> w.getGoal() instanceof PetSitGoal);
        if (alreadyAdded) return;

        mob.goalSelector.addGoal(1, new PetSitGoal(mob));
        mob.goalSelector.addGoal(5, new FollowOwnerPetGoal(mob, 1.0D, 10.0F, 5.0F));

        // Ocelots already have a tempt goal for fish; give foxes one for chicken/rabbit
        // so they can actually be lured close enough to tame.
        if (mob instanceof Fox fox) {
            mob.goalSelector.addGoal(2, new TemptGoal((PathfinderMob) fox, 1.1D,
                    Ingredient.of(Items.CHICKEN, Items.RABBIT), false));
        }
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getTarget() instanceof Mob mob) || !PetHelper.isSupported(mob)) return;

        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        TameData data = PetHelper.data(mob);
        boolean isFood = PetHelper.isTamingFood(mob, stack);

        // --- Untamed: try to tame (1 in 3 chance per item, same as cats) ---
        if (!data.isTamed()) {
            if (!isFood) return;
            if (!player.getAbilities().instabuild) stack.shrink(1);
            if (mob.getRandom().nextInt(3) == 0) {
                PetHelper.tame(mob, player);
            } else {
                PetHelper.particles(mob, false);
            }
            consume(event);
            return;
        }

        // --- Tamed: only the owner can interact ---
        if (!data.isOwnedBy(player)) return;

        if (isFood) {
            if (mob.getHealth() < mob.getMaxHealth()) {
                FoodProperties food = stack.get(DataComponents.FOOD);
                mob.heal(food != null ? food.nutrition() : 2);
                if (!player.getAbilities().instabuild) stack.shrink(1);
                consume(event);
            }
            return;
        }

        // Empty main hand toggles sit / follow
        if (event.getHand() == InteractionHand.MAIN_HAND && stack.isEmpty()) {
            boolean sit = !data.sitting();
            PetHelper.setSitting(mob, sit);
            mob.getNavigation().stop();
            mob.setJumping(false);
            if (sit) mob.setTarget(null);
            player.displayClientMessage(Component.translatable(
                    sit ? "message.tamemore.sitting" : "message.tamemore.following", mob.getName()), true);
            consume(event);
        }
    }

    /** Like cats: a sitting pet gets back up when it takes damage. */
    @SubscribeEvent
    public static void onDamaged(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof Mob mob && PetHelper.isSupported(mob)
                && !mob.level().isClientSide() && PetHelper.isSitting(mob)) {
            PetHelper.setSitting(mob, false);
        }
    }

    private static void consume(PlayerInteractEvent.EntityInteract event) {
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
