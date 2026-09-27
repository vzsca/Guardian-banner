package com.vescagaming.guardianbanner.claim;

import com.vescagaming.guardianbanner.GuardianBannerConfig;
import com.vescagaming.guardianbanner.blockentity.GuardianBannerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Persistent server-side state for Guardian Banner claims. */
public final class ClaimData extends SavedData {
    private static final String DATA_NAME = "guardian_banner_claims";
    private static final String CLAIMS_TAG = "Claims";
    private static final String CHUNK_TAG = "Chunk";
    private static final String CORE_TAG = "Core";
    private static final String MARKER_TAG = "Marker";

    private final Map<Long, BlockPos> claims = new HashMap<>();
    private final Map<Long, BlockPos> markers = new HashMap<>();
    private final Map<Long, UUID> owners = new HashMap<>();
    private final Map<Long, Set<UUID>> trusted = new HashMap<>();

    public static ClaimData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ClaimData::load, ClaimData::new, DATA_NAME);
    }

    public static ClaimData load(CompoundTag tag) {
        ClaimData data = new ClaimData();
        ListTag list = tag.getList(CLAIMS_TAG, 10);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            long chunk = entry.getLong(CHUNK_TAG);
            BlockPos core = NbtUtils.readBlockPos(entry.getCompound(CORE_TAG));
            data.claims.put(chunk, core);

            if (entry.contains(MARKER_TAG)) {
                data.markers.put(chunk, NbtUtils.readBlockPos(entry.getCompound(MARKER_TAG)));
            }

            if (entry.contains("Owner")) {
                try {
                    data.owners.put(core.asLong(), UUID.fromString(entry.getString("Owner")));
                } catch (IllegalArgumentException ignored) {
                    // Ignore malformed legacy owner data.
                }
            }

            if (entry.contains("Trusted")) {
                ListTag trustedList = entry.getList("Trusted", 8);
                Set<UUID> players = new HashSet<>();
                for (int j = 0; j < trustedList.size(); j++) {
                    try {
                        players.add(UUID.fromString(trustedList.getString(j)));
                    } catch (IllegalArgumentException ignored) {
                        // Ignore malformed legacy entries.
                    }
                }
                if (!players.isEmpty()) {
                    data.trusted.put(core.asLong(), players);
                }
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();

        for (Map.Entry<Long, BlockPos> entry : claims.entrySet()) {
            CompoundTag claim = new CompoundTag();
            claim.putLong(CHUNK_TAG, entry.getKey());
            claim.put(CORE_TAG, NbtUtils.writeBlockPos(entry.getValue()));

            UUID owner = owners.get(entry.getValue().asLong());
            if (owner != null) {
                claim.putString("Owner", owner.toString());
            }

            Set<UUID> trustedPlayers = trusted.get(entry.getValue().asLong());
            if (trustedPlayers != null && !trustedPlayers.isEmpty()) {
                ListTag trustedList = new ListTag();
                for (UUID uuid : trustedPlayers) {
                    trustedList.add(net.minecraft.nbt.StringTag.valueOf(uuid.toString()));
                }
                claim.put("Trusted", trustedList);
            }

            BlockPos marker = markers.get(entry.getKey());
            if (marker != null) {
                claim.put(MARKER_TAG, NbtUtils.writeBlockPos(marker));
            }

            list.add(claim);
        }

        tag.put(CLAIMS_TAG, list);
        return tag;
    }

    public boolean hasBannerWithin(ServerLevel level, BlockPos pos, int distance) {
        long distanceSq = (long) distance * distance;
        for (BlockPos core : new HashSet<>(claims.values())) {
            if (core == null || core.equals(pos)) continue;
            long dx = (long) core.getX() - pos.getX();
            long dz = (long) core.getZ() - pos.getZ();
            if (dx * dx + dz * dz <= distanceSq) return true;
        }
        return false;
    }

    public int claimCountForCore(BlockPos corePos) {
        int count = 0;
        for (BlockPos core : claims.values()) if (core.equals(corePos)) count++;
        return count;
    }

    public boolean isClaimed(ChunkPos chunk) {
        return claims.containsKey(chunk.toLong());
    }

    public BlockPos coreAt(ChunkPos chunk) {
        return claims.get(chunk.toLong());
    }

    public BlockPos markerAt(ChunkPos chunk) {
        return markers.get(chunk.toLong());
    }

    public UUID ownerAt(ChunkPos chunk) {
        BlockPos core = coreAt(chunk);
        return core == null ? null : owners.get(core.asLong());
    }

    public boolean isOwner(ChunkPos chunk, UUID player) {
        UUID owner = ownerAt(chunk);
        return owner != null && owner.equals(player);
    }

    public boolean isAllowed(ChunkPos chunk, UUID player) {
        UUID owner = ownerAt(chunk);
        if (owner == null || owner.equals(player)) {
            return true;
        }
        BlockPos core = coreAt(chunk);
        return core != null && trusted.getOrDefault(core.asLong(), Set.of()).contains(player);
    }

    public boolean trust(ChunkPos chunk, UUID player) {
        BlockPos core = coreAt(chunk);
        if (core == null || player == null || isOwner(chunk, player)) {
            return false;
        }
        boolean changed = trusted.computeIfAbsent(core.asLong(), ignored -> new HashSet<>()).add(player);
        if (changed) setDirty();
        return changed;
    }

    public boolean untrust(ChunkPos chunk, UUID player) {
        BlockPos core = coreAt(chunk);
        if (core == null || player == null) {
            return false;
        }
        Set<UUID> set = trusted.get(core.asLong());
        if (set == null) return false;
        boolean changed = set.remove(player);
        if (changed) setDirty();
        return changed;
    }

    /** Creates the protected core chunk. */
    public void createCore(BlockPos pos, ChunkPos chunk) {
        createCore(pos, chunk, null);
    }

    public void createCore(BlockPos pos, ChunkPos chunk, UUID owner) {
        BlockPos immutable = pos.immutable();
        claims.put(chunk.toLong(), immutable);
        markers.put(chunk.toLong(), immutable);
        if (owner != null) owners.put(immutable.asLong(), owner);
        setDirty();
    }

    public void setOwner(BlockPos corePos, UUID owner) {
        if (corePos != null && owner != null) {
            owners.put(corePos.asLong(), owner);
            setDirty();
        }
    }

    public void removeCore(BlockPos pos) {
        claims.entrySet().removeIf(entry -> {
            boolean matches = entry.getValue().equals(pos);
            if (matches) markers.remove(entry.getKey());
            return matches;
        });
        owners.remove(pos.asLong());
        trusted.remove(pos.asLong());
        setDirty();
    }

    /** Expands the core claim until its current level capacity is reached. */
    public void expandForCore(ServerLevel level, BlockPos corePos, int maxExtraClaims) {
        if (maxExtraClaims <= 0) return;

        ChunkPos coreChunk = new ChunkPos(corePos);
        long current = claims.entrySet().stream()
                .filter(entry -> entry.getValue().equals(corePos))
                .count() - 1;
        if (current >= maxExtraClaims) return;

        int radius = 1;
        while (current < maxExtraClaims && radius <= 16) {
            for (int dx = -radius; dx <= radius && current < maxExtraClaims; dx++) {
                for (int dz = -radius; dz <= radius && current < maxExtraClaims; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    ChunkPos target = new ChunkPos(coreChunk.x + dx, coreChunk.z + dz);
                    if (isClaimed(target)) continue;
                    claims.put(target.toLong(), corePos.immutable());
                    markers.put(target.toLong(), corePos.immutable());
                    current++;
                }
            }
            radius++;
        }
        setDirty();
    }


    /** Returns only claims around a chunk, avoiding a full-world scan for client sync. */
    public Map<Long, BlockPos> claimsWithin(ChunkPos center, int radius) {
        Map<Long, BlockPos> result = new HashMap<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                long key = new ChunkPos(center.x + dx, center.z + dz).toLong();
                BlockPos core = claims.get(key);
                if (core != null) result.put(key, core);
            }
        }
        return result;
    }

    public Map<Long, BlockPos> claims() {
        return Map.copyOf(claims);
    }
}
