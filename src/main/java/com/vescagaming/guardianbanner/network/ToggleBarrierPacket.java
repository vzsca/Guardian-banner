package com.vescagaming.guardianbanner.network;

import com.vescagaming.guardianbanner.blockentity.GuardianBannerBlockEntity;
import com.vescagaming.guardianbanner.claim.ClaimData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ToggleBarrierPacket {
    private final BlockPos pos;

    public ToggleBarrierPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(ToggleBarrierPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
    }

    public static ToggleBarrierPacket decode(FriendlyByteBuf buffer) {
        return new ToggleBarrierPacket(buffer.readBlockPos());
    }

    public static void handle(ToggleBarrierPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || player.distanceToSqr(packet.pos.getX() + 0.5, packet.pos.getY() + 0.5, packet.pos.getZ() + 0.5) > 64.0) {
                return;
            }
            if (!(player.level().getBlockEntity(packet.pos) instanceof GuardianBannerBlockEntity banner)) {
                return;
            }

            ClaimData claims = ClaimData.get(player.serverLevel());
            ChunkPos chunk = new ChunkPos(packet.pos);
            if (!claims.isOwner(chunk, player.getUUID())) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.guardianbanner.not_owner"));
                return;
            }

            banner.toggleActive();
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    banner.active() ? "message.guardianbanner.barrier_enabled" : "message.guardianbanner.barrier_disabled"));
            player.serverLevel().sendBlockUpdated(packet.pos, banner.getBlockState(), banner.getBlockState(), 3);
        });
        context.setPacketHandled(true);
    }
}
