package com.vescagaming.guardianbanner;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class GuardianEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, GuardianBannerMod.MODID);

    public static final EnchantmentCategory GUARDIAN_SCROLL = EnchantmentCategory.create(
            "guardian_scroll",
            item -> item == GuardianBannerMod.GUARDIAN_SCROLL.get()
    );

    public static final RegistryObject<Enchantment> BARRIER_DAMAGE =
            register("barrier_damage", 3);
    public static final RegistryObject<Enchantment> BARRIER_REGEN =
            register("barrier_regeneration", 3);
    public static final RegistryObject<Enchantment> FIRE =
            register("barrier_fire", 2);
    public static final RegistryObject<Enchantment> SLOW =
            register("barrier_slowness", 3);
    public static final RegistryObject<Enchantment> PLAYER_REGEN =
            register("player_regeneration", 2);
    public static final RegistryObject<Enchantment> PLAYER_DAMAGE =
            register("player_damage", 3);
    public static final RegistryObject<Enchantment> PLAYER_PROTECTION =
            register("player_protection", 3);

    private GuardianEnchantments() {
    }

    private static RegistryObject<Enchantment> register(String name, int maxLevel) {
        return ENCHANTMENTS.register(name, () -> new GuardianEnchantment(GUARDIAN_SCROLL, maxLevel));
    }

    private static final class GuardianEnchantment extends Enchantment {
        private final int maxLevel;

        private GuardianEnchantment(EnchantmentCategory category, int maxLevel) {
            super(Rarity.RARE, category, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
            this.maxLevel = maxLevel;
        }

        @Override
        public int getMaxLevel() {
            return maxLevel;
        }

        @Override
        public boolean isTreasureOnly() {
            return false;
        }

        @Override
        public boolean isTradeable() {
            return false;
        }
    }
}
