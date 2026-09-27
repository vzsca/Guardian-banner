package com.vescagaming.guardianbanner;

import com.vescagaming.guardianbanner.block.ModBlocks;
import com.vescagaming.guardianbanner.blockentity.ModBlockEntities;
import com.vescagaming.guardianbanner.menu.ModMenus;
import com.vescagaming.guardianbanner.network.ModNetwork;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(GuardianBannerMod.MODID)
public final class GuardianBannerMod {
    public static final String MODID = "guardianbanner";

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> GUARDIAN_SCROLL =
            ITEMS.register("guardian_scroll", () -> new GuardianScrollItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> GUARDIAN_BANNER_ITEM =
            ITEMS.register("guardian_banner", () ->
                    new net.minecraft.world.item.BlockItem(
                            ModBlocks.GUARDIAN_BANNER.get(),
                            new Item.Properties()
                    )
            );

    public GuardianBannerMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        GuardianEnchantments.ENCHANTMENTS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenus.MENUS.register(modBus);
        ITEMS.register(modBus);

        ModNetwork.init();
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, GuardianBannerConfig.SPEC, "guardianbanner-server.toml");
    }
}
