package com.triassicisland.power;

import net.minecraft.world.item.ItemStack;

/** An item with a battery that Battery Chargers can fill (flashlights, phones). */
public interface Chargeable {
    int getCharge(ItemStack stack);

    int getMaxCharge(ItemStack stack);

    void setCharge(ItemStack stack, int charge);
}
