package com.vescagaming.guardianbanner.client;

import com.vescagaming.guardianbanner.GuardianBannerMod;
import com.vescagaming.guardianbanner.client.screen.GuardianBannerScreen;
import com.vescagaming.guardianbanner.menu.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(
        modid = GuardianBannerMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class ClientSetup {
    private ClientSetup() {
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                MenuScreens.register(ModMenus.GUARDIAN_BANNER.get(), GuardianBannerScreen::new)
        );
    }
}
