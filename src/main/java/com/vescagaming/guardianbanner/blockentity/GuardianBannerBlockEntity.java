package com.vescagaming.guardianbanner.blockentity;

import com.vescagaming.guardianbanner.GuardianBannerConfig;
import com.vescagaming.guardianbanner.GuardianEnchantments;
import com.vescagaming.guardianbanner.GuardianBannerTags;
import com.vescagaming.guardianbanner.menu.GuardianBannerMenu;
import com.vescagaming.guardianbanner.claim.ClaimData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public final class GuardianBannerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MAX_LEVEL = 10;



    private int bannerLevel;
    private int kills;
    private float barrierHp = GuardianBannerConfig.BASE_BARRIER_HP.get();
    private long lastDamageTick = Long.MIN_VALUE;
    private long reactivationTick = Long.MIN_VALUE;
    private boolean active = true;
    private long lastAttractionTick = Long.MIN_VALUE;

    private int barrierDamage;
    private int barrierRegen;
    private int fire;
    private int slow;
    private int playerRegen;
    private int playerDamage;
    private int playerProtection;
    private long mobsAttracted;
    private long explosionsBlocked;
    private long damageTaken;
    private long activeTicks;

    public GuardianBannerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GUARDIAN_BANNER.get(), pos, state);
    }

    public int level() {
        return bannerLevel;
    }

    public int kills() {
        return kills;
    }

    public float hp() {
        return barrierHp;
    }

    public boolean active() {
        return active;
    }

    /** Toggles the barrier manually. Disabling it stops attraction, protection and siege pressure. */
    public boolean toggleActive() {
        if (active) {
            active = false;
            reactivationTick = Long.MIN_VALUE;
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,
                        worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5,
                        12, 0.45, 0.6, 0.45, 0.02);
                serverLevel.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE,
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.7f, 1.0f);
            }
        } else {
            // Manual reactivation always restores a destroyed barrier before restarting protection.
            barrierHp = Math.max(0.0f, Math.min(maxHp(), barrierHp));
            if (barrierHp <= 0.0f) barrierHp = maxHp();
            active = true;
            reactivationTick = Long.MIN_VALUE;
            lastDamageTick = level != null ? level.getGameTime() : 0L;
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                        worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5,
                        14, 0.45, 0.7, 0.45, 0.03);
                serverLevel.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE,
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.7f, 1.1f);
            }
        }
        setChanged();
        return active;
    }

    public float maxHp() {
        return GuardianBannerConfig.BASE_BARRIER_HP.get() + bannerLevel * GuardianBannerConfig.HP_PER_LEVEL.get();
    }

    public int maxExtraClaims() {
        return bannerLevel * GuardianBannerConfig.CLAIMS_PER_LEVEL.get();
    }

    public int enchantSlots() {
        return bannerLevel / 2;
    }

    public int enchantCount() {
        return barrierDamage
                + barrierRegen
                + fire
                + slow
                + playerRegen
                + playerDamage
                + playerProtection;
    }

    public boolean applyEnchant(Enchantment enchantment, int level) {
        if (level <= 0) return false;

        int oldLevel;
        if (enchantment == GuardianEnchantments.BARRIER_DAMAGE.get()) oldLevel = barrierDamage;
        else if (enchantment == GuardianEnchantments.BARRIER_REGEN.get()) oldLevel = barrierRegen;
        else if (enchantment == GuardianEnchantments.FIRE.get()) oldLevel = fire;
        else if (enchantment == GuardianEnchantments.SLOW.get()) oldLevel = slow;
        else if (enchantment == GuardianEnchantments.PLAYER_REGEN.get()) oldLevel = playerRegen;
        else if (enchantment == GuardianEnchantments.PLAYER_DAMAGE.get()) oldLevel = playerDamage;
        else if (enchantment == GuardianEnchantments.PLAYER_PROTECTION.get()) oldLevel = playerProtection;
        else return false;

        int newLevel = Math.max(oldLevel, level);
        int additionalSlots = newLevel - oldLevel;
        if (additionalSlots <= 0 || enchantCount() + additionalSlots > enchantSlots()) return false;

        if (enchantment == GuardianEnchantments.BARRIER_DAMAGE.get()) barrierDamage = newLevel;
        else if (enchantment == GuardianEnchantments.BARRIER_REGEN.get()) barrierRegen = newLevel;
        else if (enchantment == GuardianEnchantments.FIRE.get()) fire = newLevel;
        else if (enchantment == GuardianEnchantments.SLOW.get()) slow = newLevel;
        else if (enchantment == GuardianEnchantments.PLAYER_REGEN.get()) playerRegen = newLevel;
        else if (enchantment == GuardianEnchantments.PLAYER_DAMAGE.get()) playerDamage = newLevel;
        else playerProtection = newLevel;

        setChanged();
        return true;
    }

    public int barrierDamageLevel() {
        return barrierDamage;
    }

    public int barrierRegenLevel() {
        return barrierRegen;
    }

    public int fireLevel() {
        return fire;
    }

    public int slowLevel() {
        return slow;
    }

    public int playerRegenLevel() {
        return playerRegen;
    }

    public int playerDamageLevel() {
        return playerDamage;
    }

    public int playerProtectionLevel() {
        return playerProtection;
    }

    public void damage(float amount, long gameTime) {
        if (!active || amount <= 0.0f) {
            return;
        }

        barrierHp = Math.max(0.0f, barrierHp - amount);
        damageTaken += Math.round(amount);
        lastDamageTick = gameTime;
        reactivationTick = Long.MIN_VALUE;

        if (barrierHp <= 0.0f) {
            active = false;
            reactivationTick = Long.MIN_VALUE;
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,
                        worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5,
                        25, 0.6, 0.8, 0.6, 0.02);
                serverLevel.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE,
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.9f, 0.8f);
            }
        }

        setChanged();
    }

    /** Reactivates a destroyed barrier at full configured HP. */
    public void reactivate() {
        if (active && barrierHp > 0.0f) {
            return;
        }

        active = true;
        barrierHp = maxHp();
        reactivationTick = Long.MIN_VALUE;
        lastDamageTick = level != null ? level.getGameTime() : 0L;
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5,
                    20, 0.45, 0.7, 0.45, 0.03);
            serverLevel.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE,
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 1.15f);
        }
        setChanged();
    }

    public void addKill() {
        int oldLevel = bannerLevel;
        kills++;
        while (bannerLevel < MAX_LEVEL && kills >= GuardianBannerConfig.KILLS_PER_LEVEL.get() * (bannerLevel + 1)) {
            bannerLevel++;
        }

        if (bannerLevel != oldLevel && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            float oldMaxHp = GuardianBannerConfig.BASE_BARRIER_HP.get() + oldLevel * GuardianBannerConfig.HP_PER_LEVEL.get();
            float newMaxHp = maxHp();
            barrierHp = Math.min(newMaxHp, barrierHp + Math.max(0.0f, newMaxHp - oldMaxHp));
            ClaimData.get(serverLevel).expandForCore(
                    serverLevel, worldPosition, maxExtraClaims()
            );
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5,
                    18, 0.6, 0.8, 0.6, 0.04);
            serverLevel.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP,
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.7f, 1.0f);
        }
        setChanged();
    }

    public void tickServer() {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        long now = serverLevel.getGameTime();

        if (!active) return;
        activeTicks++;

        if (GuardianBannerConfig.ATTRACT_HOSTILES.get()
                && now - lastAttractionTick >= GuardianBannerConfig.ATTRACTION_INTERVAL_TICKS.get()) {
            lastAttractionTick = now;
            attractHostiles(serverLevel);
        }

        if (lastDamageTick != Long.MIN_VALUE
                && now - lastDamageTick >= GuardianBannerConfig.REGEN_INTERVAL_SECONDS.get() * 20L
                && barrierHp < maxHp()) {
            barrierHp = Math.min(maxHp(), barrierHp + GuardianBannerConfig.REGEN_AMOUNT.get() * (1 + bannerLevel + barrierRegenLevel()));
            lastDamageTick = now;
            setChanged();
        }
    }

    private void attractHostiles(net.minecraft.server.level.ServerLevel serverLevel) {
        AABB area = new AABB(worldPosition).inflate(GuardianBannerConfig.ATTRACTION_RADIUS.get());
        int processed = 0;
        for (Monster monster : serverLevel.getEntitiesOfClass(Monster.class, area)) {
            if (processed >= GuardianBannerConfig.MAX_ATTRACTED_MOBS.get()) break;
            if (!monster.isAlive() || monster.getType().is(GuardianBannerTags.IGNORE_ATTRACTION)
                    || monster.distanceToSqr(
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5
            ) < 9.0) {
                continue;
            }

            mobsAttracted++;
            processed++;
            monster.getNavigation().moveTo(
                    worldPosition.getX() + 0.5,
                    worldPosition.getY(),
                    worldPosition.getZ() + 0.5,
                    1.15
            );

            double dx = worldPosition.getX() + 0.5 - monster.getX();
            double dz = worldPosition.getZ() + 0.5 - monster.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > 6.0) {
                double pull = Math.min(0.08, distance * 0.01);
                monster.setDeltaMovement(
                        monster.getDeltaMovement().x + dx / distance * pull,
                        monster.getDeltaMovement().y,
                        monster.getDeltaMovement().z + dz / distance * pull
                );
            }
        }
    }

    public void recordExplosionBlocked() {
        explosionsBlocked++;
        setChanged();
    }

    public long mobsAttracted() { return mobsAttracted; }
    public long explosionsBlocked() { return explosionsBlocked; }
    public long damageTaken() { return damageTaken; }
    public long activeTicks() { return activeTicks; }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Level", bannerLevel);
        tag.putInt("Kills", kills);
        tag.putFloat("BarrierHP", barrierHp);
        tag.putLong("LastDamage", lastDamageTick);
        tag.putLong("ReactivationTick", reactivationTick);
        tag.putBoolean("Active", active);
        tag.putInt("BarrierDamage", barrierDamage);
        tag.putInt("BarrierRegen", barrierRegen);
        tag.putInt("Fire", fire);
        tag.putInt("Slow", slow);
        tag.putInt("PlayerRegen", playerRegen);
        tag.putInt("PlayerDamage", playerDamage);
        tag.putInt("PlayerProtection", playerProtection);
        tag.putLong("MobsAttracted", mobsAttracted);
        tag.putLong("ExplosionsBlocked", explosionsBlocked);
        tag.putLong("DamageTaken", damageTaken);
        tag.putLong("ActiveTicks", activeTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        bannerLevel = Math.max(0, Math.min(MAX_LEVEL, tag.getInt("Level")));
        kills = Math.max(0, tag.getInt("Kills"));
        barrierHp = Math.max(0.0f, Math.min(maxHp(), tag.getFloat("BarrierHP")));
        lastDamageTick = tag.getLong("LastDamage");
        reactivationTick = tag.contains("ReactivationTick")
                ? tag.getLong("ReactivationTick")
                : Long.MIN_VALUE;
        active = tag.getBoolean("Active");
        barrierDamage = Math.max(0, tag.getInt("BarrierDamage"));
        barrierRegen = Math.max(0, tag.getInt("BarrierRegen"));
        fire = Math.max(0, tag.getInt("Fire"));
        slow = Math.max(0, tag.getInt("Slow"));
        playerRegen = Math.max(0, tag.getInt("PlayerRegen"));
        playerDamage = Math.max(0, tag.getInt("PlayerDamage"));
        playerProtection = Math.max(0, tag.getInt("PlayerProtection"));
        mobsAttracted = Math.max(0L, tag.getLong("MobsAttracted"));
        explosionsBlocked = Math.max(0L, tag.getLong("ExplosionsBlocked"));
        damageTaken = Math.max(0L, tag.getLong("DamageTaken"));
        activeTicks = Math.max(0L, tag.getLong("ActiveTicks"));
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Guardian Banner");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GuardianBannerMenu(id, inventory, worldPosition);
    }
}
