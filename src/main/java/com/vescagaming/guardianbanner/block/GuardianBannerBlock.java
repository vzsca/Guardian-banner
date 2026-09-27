package com.vescagaming.guardianbanner.block;

import com.vescagaming.guardianbanner.GuardianBannerMod;
import com.vescagaming.guardianbanner.blockentity.GuardianBannerBlockEntity;
import com.vescagaming.guardianbanner.claim.ClaimData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public final class GuardianBannerBlock extends BaseEntityBlock {
    public GuardianBannerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GuardianBannerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (level.isClientSide) {
            return null;
        }

        return type == com.vescagaming.guardianbanner.blockentity.ModBlockEntities.GUARDIAN_BANNER.get()
                ? (lvl, pos, blockState, blockEntity) ->
                ((GuardianBannerBlockEntity) blockEntity).tickServer()
                : null;
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof GuardianBannerBlockEntity banner)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        if (held.is(GuardianBannerMod.GUARDIAN_SCROLL.get())
                || held.is(net.minecraft.world.item.Items.ENCHANTED_BOOK)) {
            boolean applied = false;
            for (var enchantment : EnchantmentHelper.getEnchantments(held).entrySet()) {
                applied |= banner.applyEnchant(enchantment.getKey(), enchantment.getValue());
            }

            if (applied && !player.getAbilities().instabuild) {
                held.shrink(1);
            }
            return InteractionResult.CONSUME;
        }

        // A manually disabled barrier stays disabled when its GUI is opened.
        // Only a destroyed barrier (0 HP) is automatically repaired on right-click.
        if (!banner.active() && banner.hp() <= 0.0f) {
            banner.reactivate();
        }

        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, banner, buffer -> buffer.writeBlockPos(pos));
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void onPlace(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState oldState,
            boolean moving
    ) {
        super.onPlace(state, level, pos, oldState, moving);

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            ClaimData.get(serverLevel).createCore(
                    pos,
                    serverLevel.getChunkAt(pos).getPos()
            );
        }
    }

    @Override
    public void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean moving
    ) {
        if (!state.is(newState.getBlock())
                && !level.isClientSide
                && level instanceof ServerLevel serverLevel) {
            ClaimData.get(serverLevel).removeCore(pos);
        }

        super.onRemove(state, level, pos, newState, moving);
    }
}
