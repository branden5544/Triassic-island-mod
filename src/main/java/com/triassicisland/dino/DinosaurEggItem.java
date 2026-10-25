package com.triassicisland.dino;

import com.triassicisland.dna.GeneticItem;
import com.triassicisland.dna.Species;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/** Place an incubated egg on the ground (or in water, for marine reptiles) to hatch it. */
public class DinosaurEggItem extends GeneticItem {
    public DinosaurEggItem(Properties properties) {
        super(properties, true);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        var player = context.getPlayer();
        Species species = Species.of(stack).orElse(null);
        if (species == null) return InteractionResult.PASS;
        if (!isIncubated(stack)) {
            if (player != null && !context.getLevel().isClientSide) {
                player.displayClientMessage(Component.translatable("message.triassicisland.egg.not_incubated"), true);
            }
            return InteractionResult.FAIL;
        }
        var type = DinoEntities.type(species);
        if (type == null) {
            if (player != null && !context.getLevel().isClientSide) {
                player.displayClientMessage(Component.translatable("message.triassicisland.egg.coming_soon", species.displayName()), true);
            }
            return InteractionResult.FAIL;
        }
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        if (DinoSpecs.get(species).swims() && !context.getLevel().getFluidState(pos).is(FluidTags.WATER)) {
            if (player != null && !context.getLevel().isClientSide) {
                player.displayClientMessage(Component.translatable("message.triassicisland.egg.needs_water"), true);
            }
            return InteractionResult.FAIL;
        }
        return hatch(context.getLevel(), pos, stack, species, player);
    }

    /** Marine eggs: right-click while aiming at water. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                                       InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Species species = Species.of(stack).orElse(null);
        if (species == null || DinoSpecs.get(species) == null || !DinoSpecs.get(species).swims() || !isIncubated(stack)) {
            return InteractionResultHolder.pass(stack);
        }
        var hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK
                || !level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER)) {
            return InteractionResultHolder.pass(stack);
        }
        InteractionResult result = hatch(level, hit.getBlockPos(), stack, species, player);
        return new InteractionResultHolder<>(result, stack);
    }

    private InteractionResult hatch(Level world, BlockPos pos, ItemStack stack, Species species,
                                    @Nullable Player player) {
        var type = DinoEntities.type(species);
        if (type == null) return InteractionResult.FAIL;
        if (world instanceof ServerLevel level) {
            DinosaurEntity baby = type.spawn(level, pos, MobSpawnType.SPAWN_EGG);
            if (baby != null) {
                baby.setGrowth(0);
                baby.setQuality(Species.quality(stack));
                baby.setPersistenceRequired();
                level.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.NEUTRAL, 1, 1);
                if (player == null || !player.getAbilities().instabuild) stack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }
}
