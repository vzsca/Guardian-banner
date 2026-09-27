package com.vescagaming.guardianbanner.menu;

import com.vescagaming.guardianbanner.GuardianBannerMod;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, GuardianBannerMod.MODID);

    public static final RegistryObject<MenuType<GuardianBannerMenu>> GUARDIAN_BANNER =
            MENUS.register(
                    "guardian_banner",
                    () -> IForgeMenuType.create(
                            (id, inventory, buffer) ->
                                    new GuardianBannerMenu(id, inventory, buffer.readBlockPos())
                    )
            );

    private ModMenus() {
    }
}
