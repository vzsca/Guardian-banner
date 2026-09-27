package com.vescagaming.guardianbanner.test;

import com.vescagaming.guardianbanner.GuardianBannerMod;
import com.vescagaming.guardianbanner.blockentity.GuardianBannerBlockEntity;
import com.vescagaming.guardianbanner.claim.ClaimData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** Regression tests for persistence, HP initialization and local claim lookup. */
@GameTestHolder(GuardianBannerMod.MODID)
@PrefixGameTestTemplate(false)
public final class GuardianBannerGameTests {
    private GuardianBannerGameTests() {}

    @GameTest(template = "empty", timeoutTicks = 20, required = true)
    public static void claimDataCreatesAndFindsClaim(GameTestHelper helper) {
        ClaimData data = new ClaimData();
        BlockPos core = helper.absolutePos(new BlockPos(1, 1, 1));
        ChunkPos chunk = new ChunkPos(core);
        UUID owner = UUID.randomUUID();
        data.createCore(core, chunk, owner);

        helper.assertTrue(data.isClaimed(chunk), "Guardian Banner claim was not created");
        helper.assertTrue(data.isOwner(chunk, owner), "Claim owner was not persisted in memory");
        helper.assertTrue(data.claimsWithin(chunk, 1).containsKey(chunk.toLong()), "Local claim lookup missed the core");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20, required = true)
    public static void barrierStartsAtConfiguredMaxHp(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockState state = Blocks.AIR.defaultBlockState();
        GuardianBannerBlockEntity banner = new GuardianBannerBlockEntity(pos, state);
        helper.assertTrue(Math.abs(banner.hp() - banner.maxHp()) < 0.001f,
                "New Guardian Banner HP does not match configured maximum");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20, required = true)
    public static void localClaimLookupDoesNotReturnDistantClaims(GameTestHelper helper) {
        ClaimData data = new ClaimData();
        BlockPos nearCore = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos farCore = nearCore.offset(160, 0, 0);
        data.createCore(nearCore, new ChunkPos(nearCore), UUID.randomUUID());
        data.createCore(farCore, new ChunkPos(farCore), UUID.randomUUID());

        helper.assertTrue(data.claimsWithin(new ChunkPos(nearCore), 1).size() == 1,
                "Nearby claim synchronization lookup included a distant claim");
        helper.succeed();
    }
}
