package com.vescagaming.guardianbanner.claim;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = "guardianbanner")
public final class GuardianBannerCommands {
    private GuardianBannerCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("guardianbanner")
                        .then(Commands.literal("info")
                                .executes(ctx -> info(ctx.getSource().getPlayerOrException())))
                        .then(Commands.literal("trust")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> trust(
                                                ctx.getSource().getPlayerOrException(),
                                                EntityArgument.getPlayer(ctx, "player"), true))))
                        .then(Commands.literal("untrust")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> trust(
                                                ctx.getSource().getPlayerOrException(),
                                                EntityArgument.getPlayer(ctx, "player"), false))))
        );
    }

    private static int info(ServerPlayer player) {
        ClaimData data = ClaimData.get(player.serverLevel());
        ChunkPos chunk = new ChunkPos(player.blockPosition());
        var core = data.coreAt(chunk);
        if (core == null) {
            player.sendSystemMessage(Component.literal("§cAucune Guardian Banner ne protège ce chunk."));
            return 0;
        }

        player.sendSystemMessage(Component.literal("§6Guardian Banner§r"));
        player.sendSystemMessage(Component.literal("§7Propriétaire: §f" +
                (data.ownerAt(chunk) == null ? "inconnu" : data.ownerAt(chunk).toString())));
        player.sendSystemMessage(Component.literal("§7Protection: §f" +
                (data.isAllowed(chunk, player.getUUID()) ? "autorisée" : "refusée")));
        return 1;
    }

    private static int trust(ServerPlayer owner, ServerPlayer target, boolean add) {
        ClaimData data = ClaimData.get(owner.serverLevel());
        ChunkPos chunk = new ChunkPos(owner.blockPosition());

        if (!data.isOwner(chunk, owner.getUUID())) {
            owner.sendSystemMessage(Component.literal("§cTu dois être propriétaire de la Guardian Banner."));
            return 0;
        }

        boolean changed = add
                ? data.trust(chunk, target.getUUID())
                : data.untrust(chunk, target.getUUID());
        owner.sendSystemMessage(Component.literal(changed
                ? (add ? "§aJoueur ajouté à la zone protégée." : "§eJoueur retiré de la zone protégée.")
                : "§7Aucun changement."));
        return changed ? 1 : 0;
    }
}
