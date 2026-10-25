package com.triassicisland.registry;

import com.triassicisland.TriassicIsland;
import com.triassicisland.dino.DinoSpec;
import com.triassicisland.dino.DinoSpecs;
import com.triassicisland.dna.GeneticItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TriassicIsland.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.triassicisland.main"))
            .icon(() -> ModItems.MOSQUITO_IN_AMBER.get().getDefaultInstance())
            // Every registered item shows up automatically, so new content never gets forgotten.
            .displayItems((params, output) -> {
                ModItems.ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                // Ready-to-hatch eggs of every species that exists so far, for creative testing.
                for (DinoSpec spec : DinoSpecs.all()) {
                    output.accept(GeneticItem.markIncubated(spec.species().apply(new ItemStack(ModItems.DINOSAUR_EGG.get()), 100)));
                }
            })
            .build());

    private ModCreativeTabs() {}
}
