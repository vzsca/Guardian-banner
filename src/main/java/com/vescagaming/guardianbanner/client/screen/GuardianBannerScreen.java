package com.vescagaming.guardianbanner.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vescagaming.guardianbanner.network.ModNetwork;
import com.vescagaming.guardianbanner.network.ToggleBarrierPacket;
import com.vescagaming.guardianbanner.menu.GuardianBannerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class GuardianBannerScreen extends AbstractContainerScreen<GuardianBannerMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");
    private Button toggleButton;

    public GuardianBannerScreen(GuardianBannerMenu menu, net.minecraft.world.entity.player.Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 190;
    }

    @Override
    protected void init() {
        super.init();
        toggleButton = addRenderableWidget(Button.builder(toggleText(), button -> {
            ModNetwork.CHANNEL.sendToServer(new ToggleBarrierPacket(menu.pos()));
            button.active = false;
        }).bounds(leftPos + 8, topPos + 153, 160, 20).build());
        toggleButton.active = true;
    }

    private Component toggleText() {
        return Component.translatable(menu.barrierActive()
                ? "screen.guardianbanner.disable_barrier"
                : "screen.guardianbanner.enable_barrier");
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (toggleButton != null) {
            toggleButton.setMessage(toggleText());
            toggleButton.active = true;
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        graphics.fill(leftPos + 4, topPos + 4, leftPos + 172, topPos + 149, 0xCC10151B);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        var banner = menu.banner();
        if (banner == null) return;
        int white = 0xFFFFFF, muted = 0xD5E6F0;
        graphics.drawString(font, Component.translatable("screen.guardianbanner.title"), 8, 7, 0xE7F7FF, false);
        graphics.drawString(font, Component.translatable("screen.guardianbanner.level", banner.level(), banner.kills()), 8, 22, white, false);

        int barX = 8, barY = 37, barWidth = 160;
        graphics.fill(barX, barY, barX + barWidth, barY + 8, 0xFF30343A);
        float ratio = banner.maxHp() <= 0 ? 0 : Math.max(0, Math.min(1, banner.hp() / banner.maxHp()));
        graphics.fill(barX, barY, barX + Math.round(barWidth * ratio), barY + 8,
                banner.active() ? 0xFF36C98F : 0xFFB63B4B);
        graphics.drawString(font, Component.translatable("screen.guardianbanner.barrier", Math.round(banner.hp()), Math.round(banner.maxHp())), 8, 48, white, false);
        graphics.drawString(font, Component.translatable("screen.guardianbanner.claims", banner.maxExtraClaims() + 1), 8, 62, muted, false);
        graphics.drawString(font, Component.translatable("screen.guardianbanner.enchants", banner.enchantCount(), banner.enchantSlots()), 8, 75, muted, false);
        graphics.drawString(font, Component.translatable(banner.active() ? "screen.guardianbanner.active" : "screen.guardianbanner.inactive"), 8, 89,
                banner.active() ? 0x55FFAA : 0xFF6677, false);
        graphics.drawString(font, Component.translatable(banner.active() ? "screen.guardianbanner.siege_active" : "screen.guardianbanner.siege_paused"), 8, 102,
                banner.active() ? 0xFFB84D : 0x9AA7B3, false);
        graphics.drawString(font, Component.translatable("screen.guardianbanner.stats"), 8, 115, 0xE7F7FF, false);
        graphics.drawString(font, Component.translatable("screen.guardianbanner.attracted", banner.mobsAttracted()), 8, 128, muted, false);
        graphics.drawString(font, Component.translatable("screen.guardianbanner.explosions", banner.explosionsBlocked()), 8, 141, muted, false);
    }
}
