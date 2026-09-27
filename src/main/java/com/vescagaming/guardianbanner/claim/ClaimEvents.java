package com.vescagaming.guardianbanner.claim;

import com.vescagaming.guardianbanner.GuardianBannerConfig;
import com.vescagaming.guardianbanner.GuardianBannerMod;
import com.vescagaming.guardianbanner.block.ModBlocks;
import com.vescagaming.guardianbanner.blockentity.GuardianBannerBlockEntity;
import com.vescagaming.guardianbanner.network.ClaimSyncPacket;
import com.vescagaming.guardianbanner.network.ModNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = GuardianBannerMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClaimEvents {
    private ClaimEvents() {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % 40 != 0) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            ClaimData data = ClaimData.get(level);
            for (ServerPlayer player : level.players()) {
                ChunkPos center = new ChunkPos(player.blockPosition());
                ArrayList<ClaimSyncPacket.Entry> entries = new ArrayList<>();
                for (var entry : data.claimsWithin(center, GuardianBannerConfig.CLIENT_BORDER_CHUNK_RADIUS.get()).entrySet()) {
                    if (!(level.getBlockEntity(entry.getValue()) instanceof GuardianBannerBlockEntity banner)) continue;
                    BlockPos marker = data.markerAt(new ChunkPos(entry.getKey()));
                    if (marker != null) {
                        entries.add(new ClaimSyncPacket.Entry(entry.getKey(), marker.asLong(),
                                banner.hp(), banner.maxHp(), banner.active()));
                    }
                }
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new ClaimSyncPacket(entries));
            }
        }
    }

    @SubscribeEvent
    public static void onMobSpawn(net.minecraftforge.event.entity.living.MobSpawnEvent.PositionCheck event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof Monster)) return;
        if (!GuardianBannerConfig.PREVENT_HOSTILE_SPAWNS.get()) return;
        ChunkPos chunk = new ChunkPos(BlockPos.containing(event.getX(), 0.0, event.getZ()));
        if (isActive(level, ClaimData.get(level).coreAt(chunk))) event.setResult(Event.Result.DENY);
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof Player player)) return;
        ClaimData data = ClaimData.get(level);
        ChunkPos chunk = new ChunkPos(event.getPos());

        if (event.getPlacedBlock().is(ModBlocks.GUARDIAN_BANNER.get())) {
            if (data.hasBannerWithin(level, event.getPos(), GuardianBannerConfig.MIN_BANNER_DISTANCE.get())) {
                data.removeCore(event.getPos());
                event.setCanceled(true);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§cImpossible de placer une Guardian Banner à moins de "
                                + GuardianBannerConfig.MIN_BANNER_DISTANCE.get() + " blocs d'une autre."));
                return;
            }
            if (data.isClaimed(chunk) && !data.isAllowed(chunk, player.getUUID())) {
                event.setCanceled(true);
                return;
            }
            data.setOwner(event.getPos(), player.getUUID());
            return;
        }

        if (data.isClaimed(chunk) && !data.isAllowed(chunk, player.getUUID())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        ClaimData data = ClaimData.get(level);
        ChunkPos chunk = new ChunkPos(event.getPos());
        if (!data.isClaimed(chunk)) return;

        if (event.getState().is(ModBlocks.GUARDIAN_BANNER.get())) {
            if (!data.isOwner(chunk, event.getPlayer().getUUID())) event.setCanceled(true);
        } else if (!data.isAllowed(chunk, event.getPlayer().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        ClaimData data = ClaimData.get(level);
        ChunkPos chunk = new ChunkPos(event.getPos());
        if (data.isClaimed(chunk) && !data.isAllowed(chunk, event.getEntity().getUUID())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player) || !(player.level() instanceof ServerLevel level)
                || level.getGameTime() % 20 != 0) return;
        BlockPos core = ClaimData.get(level).coreAt(new ChunkPos(player.blockPosition()));
        if (!(level.getBlockEntity(core) instanceof GuardianBannerBlockEntity banner) || !banner.active()) return;

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, true, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 0, true, false));
        if (banner.playerRegenLevel() > 0)
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40,
                    Math.min(3, banner.playerRegenLevel() - 1), true, false));
        if (banner.playerDamageLevel() > 0)
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40,
                    Math.min(4, banner.playerDamageLevel() - 1), true, false));
        if (banner.playerProtectionLevel() > 0)
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40,
                    Math.min(3, banner.playerProtectionLevel()), true, false));
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player) || !(player.level() instanceof ServerLevel level)) return;
        DamageSource source = event.getSource();
        boolean mobDamage = source.getEntity() instanceof Monster || source.getDirectEntity() instanceof Monster;
        boolean explosion = source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.PLAYER_EXPLOSION);
        if ((mobDamage && GuardianBannerConfig.PROTECT_MOB_DAMAGE.get()
                || explosion && GuardianBannerConfig.PROTECT_EXPLOSIONS.get())
                && isProtected(level, player.blockPosition())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level) || !(event.getEntity() instanceof Monster)) return;
        BlockPos core = ClaimData.get(level).coreAt(new ChunkPos(event.getEntity().blockPosition()));
        if (core != null && level.getBlockEntity(core) instanceof GuardianBannerBlockEntity banner) banner.addKill();
    }

    @SubscribeEvent
    public static void onMobTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Monster mob) || !(mob.level() instanceof ServerLevel level)) return;
        if (mob.tickCount % GuardianBannerConfig.MOB_TICK_INTERVAL_TICKS.get() != 0) return;
        ClaimData data = ClaimData.get(level);
        ChunkPos currentChunk = new ChunkPos(mob.blockPosition());
        BlockPos core = data.coreAt(currentChunk);
        if (isActive(level, core)) {
            handleInsideClaim(mob, level, currentChunk, core);
        } else {
            handleClaimBoundary(mob, level, data, currentChunk);
        }
    }

    private static void handleInsideClaim(Monster mob, ServerLevel level, ChunkPos chunk, BlockPos corePos) {
        if (!(level.getBlockEntity(corePos) instanceof GuardianBannerBlockEntity banner)) return;
        double dx = mob.getX() - corePos.getX() - 0.5, dz = mob.getZ() - corePos.getZ() - 0.5;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist <= 3.0) {
            double len = Math.max(0.001, dist);
            mob.push(dx / len * 0.45, 0, dz / len * 0.45);
            damageBarrierFromMob(mob, level, banner);
            return;
        }
        double minX = chunk.getMinBlockX(), maxX = chunk.getMaxBlockX() + 1.0;
        double minZ = chunk.getMinBlockZ(), maxZ = chunk.getMaxBlockZ() + 1.0;
        double west = Math.abs(mob.getX() - minX), east = Math.abs(mob.getX() - maxX);
        double north = Math.abs(mob.getZ() - minZ), south = Math.abs(mob.getZ() - maxZ);
        double nearest = Math.min(Math.min(west, east), Math.min(north, south));
        if (nearest >= 1.5) return;
        double px = nearest == west ? -0.45 : nearest == east ? 0.45 : 0;
        double pz = nearest == north ? -0.45 : nearest == south ? 0.45 : 0;
        mob.push(px, 0, pz);
        damageBarrierFromMob(mob, level, banner);
    }

    private static void handleClaimBoundary(Monster mob, ServerLevel level, ClaimData data, ChunkPos currentChunk) {
        double x = mob.getX(), z = mob.getZ();
        for (ChunkPos neighbor : neighbors(currentChunk)) {
            BlockPos corePos = data.coreAt(neighbor);
            if (!isActive(level, corePos)) continue;
            double minX = neighbor.getMinBlockX(), maxX = neighbor.getMaxBlockX() + 1.0;
            double minZ = neighbor.getMinBlockZ(), maxZ = neighbor.getMaxBlockZ() + 1.0;
            boolean touching = x >= minX - 1 && x <= maxX + 1 && z >= minZ - 1 && z <= maxZ + 1
                    && (Math.abs(x - minX) < 1.2 || Math.abs(x - maxX) < 1.2
                    || Math.abs(z - minZ) < 1.2 || Math.abs(z - maxZ) < 1.2);
            if (!touching) continue;
            double px = x - (minX + maxX) / 2.0, pz = z - (minZ + maxZ) / 2.0;
            double len = Math.max(0.001, Math.sqrt(px * px + pz * pz));
            mob.push(px / len * 0.35, 0, pz / len * 0.35);
            if (level.getBlockEntity(corePos) instanceof GuardianBannerBlockEntity banner) damageBarrierFromMob(mob, level, banner);
            return;
        }
    }

    private static void damageBarrierFromMob(Monster mob, ServerLevel level, GuardianBannerBlockEntity banner) {
        if (mob.tickCount % 10 != 0) return;
        banner.damage(5.0f * (1 + banner.barrierDamageLevel()), level.getGameTime());
        mob.hurt(level.damageSources().magic(), 2.0f + banner.level());
        if (banner.fireLevel() > 0) mob.setSecondsOnFire(2 + banner.fireLevel());
        if (banner.slowLevel() > 0) mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40,
                Math.min(4, banner.slowLevel() - 1)));
    }

    @SubscribeEvent
    public static void onMobDestroy(LivingDestroyBlockEvent event) {
        if (!(event.getEntity() instanceof Monster) || !(event.getEntity().level() instanceof ServerLevel level)) return;
        if (isActive(level, ClaimData.get(level).coreAt(new ChunkPos(event.getPos())))) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        ClaimData data = ClaimData.get(level);
        Set<BlockPos> hitCores = new HashSet<>();
        if (GuardianBannerConfig.PROTECT_BLOCKS_FROM_EXPLOSIONS.get()) {
            event.getAffectedBlocks().removeIf(pos -> {
                BlockPos core = data.coreAt(new ChunkPos(pos));
                if (!isActive(level, core)) return false;
                hitCores.add(core);
                return true;
            });
        }
        if (GuardianBannerConfig.PROTECT_EXPLOSIONS.get()) {
            event.getAffectedEntities().removeIf(entity -> entity instanceof Player player
                    && isProtected(level, player.blockPosition()));
        }
        for (BlockPos core : hitCores) {
            if (level.getBlockEntity(core) instanceof GuardianBannerBlockEntity banner) {
                banner.damage(25.0f, level.getGameTime());
                banner.recordExplosionBlocked();
            }
        }
    }

    private static boolean isProtected(ServerLevel level, BlockPos pos) {
        return isActive(level, ClaimData.get(level).coreAt(new ChunkPos(pos)));
    }

    private static boolean isActive(ServerLevel level, BlockPos corePos) {
        return corePos != null && level.getBlockEntity(corePos) instanceof GuardianBannerBlockEntity banner && banner.active();
    }

    private static ChunkPos[] neighbors(ChunkPos chunk) {
        return new ChunkPos[]{new ChunkPos(chunk.x + 1, chunk.z), new ChunkPos(chunk.x - 1, chunk.z),
                new ChunkPos(chunk.x, chunk.z + 1), new ChunkPos(chunk.x, chunk.z - 1)};
    }
}
