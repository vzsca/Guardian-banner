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

    private static int syncInt(long value) {
        return (int) Math.max(0L, Math.min(32767L, value));
    }

    private DataSlot synced(java.util.function.ToIntFunction<GuardianBannerBlockEntity> getter) {
        return addDataSlot(new DataSlot() {
            @Override public int get() {
                GuardianBannerBlockEntity banner = banner();
                return banner == null ? 0 : syncInt(getter.applyAsInt(banner));
            }
            @Override public void set(int value) { }
        });
    }

    private final DataSlot activeState = synced(b -> b.active() ? 1 : 0);
    private final DataSlot hpTenths = synced(b -> Math.round(b.hp() * 10.0f));
    private final DataSlot maxHpTenths = synced(b -> Math.round(b.maxHp() * 10.0f));
    private final DataSlot levelState = synced(GuardianBannerBlockEntity::level);
    private final DataSlot killsState = synced(GuardianBannerBlockEntity::kills);
    private final DataSlot claimsState = synced(b -> b.maxExtraClaims() + 1);
    private final DataSlot enchantCountState = synced(GuardianBannerBlockEntity::enchantCount);
    private final DataSlot enchantSlotsState = synced(GuardianBannerBlockEntity::enchantSlots);
    private final DataSlot mobsAttractedState = synced(b -> syncInt(b.mobsAttracted()));
    private final DataSlot explosionsBlockedState = synced(b -> syncInt(b.explosionsBlocked()));

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
    public float barrierHp() { return hpTenths.get() / 10.0f; }
    public float barrierMaxHp() { return maxHpTenths.get() / 10.0f; }
    public int bannerLevel() { return levelState.get(); }
    public int kills() { return killsState.get(); }
    public int claimedChunks() { return claimsState.get(); }
    public int enchantCount() { return enchantCountState.get(); }
    public int enchantSlots() { return enchantSlotsState.get(); }
    public int mobsAttracted() { return mobsAttractedState.get(); }
    public int explosionsBlocked() { return explosionsBlockedState.get(); }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockEntity(pos) instanceof GuardianBannerBlockEntity
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64.0;
    }
}
