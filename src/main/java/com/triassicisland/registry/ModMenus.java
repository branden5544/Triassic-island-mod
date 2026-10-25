package com.triassicisland.registry;

import com.triassicisland.TriassicIsland;
import com.triassicisland.machine.MachineMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, TriassicIsland.MODID);

    public static final RegistryObject<MenuType<MachineMenu>> MACHINE = MENUS.register("machine",
            () -> IForgeMenuType.create(MachineMenu::fromNetwork));

    private ModMenus() {}
}
