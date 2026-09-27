package com.vescagaming.guardianbanner.menu;

import com.vescagaming.guardianbanner.blockentity.GuardianBannerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class GuardianBannerMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final Level level;
    private final DataSlot activeState = addDataSlot(new DataSlot() {
        @Override public int get() { GuardianBannerBlockEntity b = banner(); return b != null && b.active() ? 1 : 0; }
        @Override public void set(int value) { }
    });

    public GuardianBannerMenu(int id, Inventory inventory, BlockPos pos) {
        super(ModMenus.GUARDIAN_BANNER.get(), id);
        this.pos = pos;
        this.level = inventory.player.level();
    }

    public GuardianBannerBlockEntity banner() {
        return level.getBlockEntity(pos) instanceof GuardianBannerBlockEntity banner ? banner : null;
    }

    public BlockPos pos() { return pos; }
    public boolean barrierActive() { return activeState.get() != 0; }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockEntity(pos) instanceof GuardianBannerBlockEntity
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64.0;
    }
}
