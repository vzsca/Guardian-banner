package com.vescagaming.guardianbanner;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class GuardianScrollItem extends Item {
    public GuardianScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }
}
