package com.vescagaming.guardianbanner.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("guardianbanner", "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int messageId;
    private static boolean initialized;

    private ModNetwork() {
    }

    public static void init() {
        if (initialized) {
            return;
        }

        initialized = true;
        CHANNEL.registerMessage(
                messageId++,
                ClaimSyncPacket.class,
                ClaimSyncPacket::encode,
                ClaimSyncPacket::decode,
                ClaimSyncPacket::handle
        );
        CHANNEL.registerMessage(
                messageId++,
                ToggleBarrierPacket.class,
                ToggleBarrierPacket::encode,
                ToggleBarrierPacket::decode,
                ToggleBarrierPacket::handle
        );
    }
}
