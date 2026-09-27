package com.vescagaming.guardianbanner;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

/** Optional entity tags for modpack compatibility. */
public final class GuardianBannerTags {
    public static final TagKey<EntityType<?>> IGNORE_ATTRACTION = TagKey.create(
            ForgeRegistries.ENTITY_TYPES.getRegistryKey(),
            new ResourceLocation(GuardianBannerMod.MODID, "ignore_attraction"));

    private GuardianBannerTags() {}
}
