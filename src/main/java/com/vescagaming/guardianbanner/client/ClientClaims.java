package com.vescagaming.guardianbanner.client;

import com.vescagaming.guardianbanner.network.ClaimSyncPacket;

import java.util.List;

public final class ClientClaims {
    private static List<ClaimSyncPacket.Entry> entries = List.of();

    private ClientClaims() {
    }

    public static void set(List<ClaimSyncPacket.Entry> newEntries) {
        entries = List.copyOf(newEntries);
    }

    public static List<ClaimSyncPacket.Entry> entries() {
        return entries;
    }
}
