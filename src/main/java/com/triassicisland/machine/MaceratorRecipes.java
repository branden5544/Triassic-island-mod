package com.triassicisland.machine;

import com.triassicisland.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** What the macerator grinds: ores into double raw ore, and the usual crushing chain. */
public final class MaceratorRecipes {
    private record Recipe(Predicate<ItemStack> input, Supplier<ItemStack> output) {}

    private static TagKey<Item> forge(String path) {
        return ItemTags.create(new ResourceLocation("forge", path));
    }

    private static Predicate<ItemStack> tag(String path) {
        TagKey<Item> tag = forge(path);
        return stack -> stack.is(tag);
    }

    private static Predicate<ItemStack> item(Item item) {
        return stack -> stack.is(item);
    }

    private static final List<Recipe> RECIPES = List.of(
            new Recipe(tag("ores/iron"), () -> new ItemStack(Items.RAW_IRON, 2)),
            new Recipe(tag("ores/gold"), () -> new ItemStack(Items.RAW_GOLD, 2)),
            new Recipe(tag("ores/copper"), () -> new ItemStack(Items.RAW_COPPER, 6)),
            new Recipe(tag("ores/coal"), () -> new ItemStack(Items.COAL, 3)),
            new Recipe(tag("ores/redstone"), () -> new ItemStack(Items.REDSTONE, 8)),
            new Recipe(tag("ores/lapis"), () -> new ItemStack(Items.LAPIS_LAZULI, 8)),
            new Recipe(tag("ores/diamond"), () -> new ItemStack(Items.DIAMOND, 2)),
            new Recipe(tag("ores/emerald"), () -> new ItemStack(Items.EMERALD, 2)),
            new Recipe(tag("ores/quartz"), () -> new ItemStack(Items.QUARTZ, 3)),
            new Recipe(stack -> stack.is(ModItems.FOSSIL_ORE.get()) || stack.is(ModItems.DEEPSLATE_FOSSIL_ORE.get()),
                    () -> new ItemStack(ModItems.FOSSIL_FRAGMENT.get(), 3)),
            new Recipe(item(ModItems.AMBER_ORE.get()), () -> new ItemStack(ModItems.AMBER.get(), 3)),
            new Recipe(item(Items.STONE), () -> new ItemStack(Items.COBBLESTONE)),
            new Recipe(item(Items.COBBLESTONE), () -> new ItemStack(Items.GRAVEL)),
            new Recipe(item(Items.GRAVEL), () -> new ItemStack(Items.SAND)),
            new Recipe(item(Items.BONE), () -> new ItemStack(Items.BONE_MEAL, 6)),
            new Recipe(stack -> stack.is(ItemTags.WOOL), () -> new ItemStack(Items.STRING, 4)),
            new Recipe(item(Items.GLOWSTONE), () -> new ItemStack(Items.GLOWSTONE_DUST, 4)),
            new Recipe(item(Items.BLAZE_ROD), () -> new ItemStack(Items.BLAZE_POWDER, 4)),
            new Recipe(item(Items.SUGAR_CANE), () -> new ItemStack(Items.SUGAR, 2)));

    public static boolean canProcess(ItemStack stack) {
        return !result(stack).isEmpty();
    }

    public static ItemStack result(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        for (Recipe recipe : RECIPES) {
            if (recipe.input.test(stack)) return recipe.output.get();
        }
        return ItemStack.EMPTY;
    }

    private MaceratorRecipes() {}
}
