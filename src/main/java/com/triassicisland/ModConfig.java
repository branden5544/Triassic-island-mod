package com.triassicisland;

import net.minecraftforge.common.ForgeConfigSpec;

/** Server settings (per world, in serverconfig/triassicisland-server.toml). */
public final class ModConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue COMPUTERS_ENABLED;
    public static final ForgeConfigSpec.IntValue LINES_PER_TICK;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("computers");
        COMPUTERS_ENABLED = builder
                .comment("Allow computers to run Python. Scripts are sandboxed, but on public servers you may want this off.")
                .define("enabled", true);
        LINES_PER_TICK = builder
                .comment("Python lines each computer may run per game tick before it waits for the next tick.")
                .defineInRange("linesPerTick", 1000, 50, 100_000);
        builder.pop();
        SPEC = builder.build();
    }

    private ModConfig() {}
}
