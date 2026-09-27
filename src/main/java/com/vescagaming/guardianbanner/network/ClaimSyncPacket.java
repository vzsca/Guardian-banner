package com.vescagaming.guardianbanner.network;

import com.vescagaming.guardianbanner.client.ClientClaims;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record ClaimSyncPacket(List<Entry> entries) {
    public record Entry(long chunk, long marker, float hp, float maxHp, boolean active) {
    }

    public static void encode(ClaimSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entries.size());
        for (Entry entry : packet.entries) {
            buffer.writeLong(entry.chunk);
            buffer.writeLong(entry.marker);
            buffer.writeFloat(entry.hp);
            buffer.writeFloat(entry.maxHp);
            buffer.writeBoolean(entry.active);
        }
    }

    public static ClaimSyncPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<Entry> entries = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            entries.add(new Entry(
                    buffer.readLong(),
                    buffer.readLong(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readBoolean()
            ));
        }

        return new ClaimSyncPacket(entries);
    }

    public static void handle(ClaimSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientClaims.set(packet.entries)
        ));
        context.setPacketHandled(true);
    }
}
