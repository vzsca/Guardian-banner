package com.vescagaming.guardianbanner.client.compat;

import net.minecraftforge.fml.ModList;

/** Optional hook for future Xaero World Map integration. */
public final class XaeroCompat {
    private XaeroCompat() {
    }

    public static boolean isWorldMapLoaded() {
        return ModList.get().isLoaded("xaeroworldmap");
    }
}
