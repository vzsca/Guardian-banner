package com.vescagaming.guardianbanner.blockentity;

import com.vescagaming.guardianbanner.GuardianBannerMod;
import com.vescagaming.guardianbanner.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, GuardianBannerMod.MODID);

    public static final RegistryObject<BlockEntityType<GuardianBannerBlockEntity>> GUARDIAN_BANNER =
            BLOCK_ENTITIES.register(
                    "guardian_banner",
                    () -> BlockEntityType.Builder.of(
                            GuardianBannerBlockEntity::new,
                            ModBlocks.GUARDIAN_BANNER.get()
                    ).build(null)
            );

    private ModBlockEntities() {
    }
}
