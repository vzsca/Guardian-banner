package com.vescagaming.guardianbanner;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

/** Compatibility access point for registered Guardian Banner items. */
public final class GuardianItems {
    public static final RegistryObject<Item> GUARDIAN_SCROLL = GuardianBannerMod.GUARDIAN_SCROLL;
    public static final RegistryObject<Item> GUARDIAN_BANNER = GuardianBannerMod.GUARDIAN_BANNER_ITEM;

    private GuardianItems() {
    }
}
