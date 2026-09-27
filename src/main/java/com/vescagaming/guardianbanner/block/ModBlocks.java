package com.vescagaming.guardianbanner.block;

import com.vescagaming.guardianbanner.GuardianBannerMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, GuardianBannerMod.MODID);

    public static final RegistryObject<Block> GUARDIAN_BANNER = BLOCKS.register(
            "guardian_banner",
            () -> new GuardianBannerBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(2.0f, 6.0f)
                            .noOcclusion()
            )
    );

    private ModBlocks() {
    }
}
